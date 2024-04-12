package eu.europeana.metis.utils;

import static eu.europeana.metis.utils.TempFileUtils.createSecureTempFile;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.ArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class contains functionality to extract archives.
 */
public class CompressedFileHandler {

  private static final Logger LOGGER = LoggerFactory.getLogger(CompressedFileHandler.class);

  private static final String MAC_TEMP_FOLDER = "__MACOSX";
  private static final String MAC_TEMP_FILE = ".DS_Store";
  public static final String FILE_NAME_BANNED_CHARACTERS = "% $:?&#<>|*," + Character.MIN_VALUE;


  /**
   * Extract a file.
   *
   * @param compressedFile The compressed file.
   * @param destinationFolder The destination folder.
   * @throws IOException If there was a problem with the extraction.
   */
  public static void extractFile(final Path compressedFile, final Path destinationFolder)
      throws IOException {
    final CompressedFileExtension compressingExtension = CompressedFileExtension
        .forPath(compressedFile);
    if (compressingExtension == null) {
      throw new IOException("Can't process archive of this type: " + compressedFile);
    }
    switch (compressingExtension) {
      case ZIP -> extractZipFile(compressedFile, destinationFolder);
      case GZIP -> extractGzFile(compressedFile, destinationFolder);
      case TAR -> extractTarFile(compressedFile, destinationFolder);
      case TGZIP, TAR_GZ -> extractTarGzFile(compressedFile, destinationFolder);
      default -> throw new IllegalStateException(
          "Shouldn't be here. Extension found: " + compressingExtension.name());
    }
  }

  /**
   * This method extracts all files from a ZIP file and returns them as byte arrays. This method only considers files in the main
   * directory. This method creates (and then removes) a temporary file.
   *
   * @param providedZipFile Input stream containing the zip file. This method is not responsible for closing the stream.
   * @return A list of records.
   * @throws IOException In case of problems with the temporary file or with reading the zip file.
   */
  public List<ByteArrayInputStream> getContentFromZipFile(InputStream providedZipFile)
      throws IOException {
    try (final ZipFile zipFile = createInMemoryZipFileObject(providedZipFile)) {
      final List<InputStream> streams = getContentFromZipFile(zipFile);
      final List<ByteArrayInputStream> result = new ArrayList<>(streams.size());
      for (InputStream stream : streams) {
        result.add(new ByteArrayInputStream(IOUtils.toByteArray(stream)));
      }
      return result;
    }
  }

  private ZipFile createInMemoryZipFileObject(InputStream content) throws IOException {
    final File tempFile = createSecureTempFile(content.getClass().getSimpleName(), ".zip").toFile();
    FileUtils.copyInputStreamToFile(content, tempFile);
    LOGGER.info("Temp file: {} created.", tempFile);
    return new ZipFile(tempFile, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE);
  }

  public List<String> getRecordsFromZipFile(ZipFile zipFile) throws IOException {
    final List<InputStream> streams = getContentFromZipFile(zipFile);
    final List<String> result = new ArrayList<>(streams.size());
    for (InputStream stream : streams) {
      result.add(IOUtils.toString(stream, StandardCharsets.UTF_8));
    }
    return result;
  }

  private List<InputStream> getContentFromZipFile(ZipFile zipFile) throws IOException {
    final List<InputStream> result = new ArrayList<>();
    final Iterator<? extends ZipEntry> entries = zipFile.stream().iterator();
    while (entries.hasNext()) {
      final ZipEntry zipEntry = entries.next();
      if (accept(zipEntry)) {
        result.add(zipFile.getInputStream(zipEntry));
      }
    }
    return result;
  }

  public boolean accept(ZipEntry zipEntry) {
    return !zipEntry.isDirectory() && !zipEntry.getName().startsWith(MAC_TEMP_FOLDER)
        && !zipEntry.getName().endsWith(MAC_TEMP_FILE);
  }


  private static void extractZipFile(final Path compressedFile, final Path destinationFolder) throws IOException {
    final List<Path> nestedCompressedFiles = new ArrayList<>();
    try (ZipArchiveInputStream is = new ZipArchiveInputStream(Files.newInputStream(compressedFile))) {
      ZipArchiveEntry entry;
      while ((entry = is.getNextZipEntry()) != null) {
        final String entryName = replaceBannedCharacters(entry.getName());
        // create a new path, protect against malicious zip files
        final Path newPath = zipSlipVulnerabilityProtect(entryName, destinationFolder);
        if (CompressedFileExtension.hasCompressedFileExtension(entry.getName())) {
          nestedCompressedFiles.add(destinationFolder.resolve(entry.getName()));
        }
        extract(is, entry, newPath);
      }
    }
    for (Path nestedCompressedFile : nestedCompressedFiles) {
      extractFile(nestedCompressedFile, nestedCompressedFile.getParent());
    }
  }

  private static void extractTarGzFile(final Path compressedFile, final Path destinationFolder) throws IOException {

    Set<Path> nestedCompressedFiles = new HashSet<>();

    try (InputStream fi = Files.newInputStream(compressedFile);
        BufferedInputStream bi = new BufferedInputStream(fi);
        GzipCompressorInputStream gzi = new GzipCompressorInputStream(bi);
        TarArchiveInputStream ti = new TarArchiveInputStream(gzi)) {

      ArchiveEntry entry;
      while ((entry = ti.getNextEntry()) != null) {
        final String entryName = replaceBannedCharacters(entry.getName());
        // create a new path, protect against malicious zip files
        final Path newPath = zipSlipVulnerabilityProtect(entryName, destinationFolder);
        if (CompressedFileExtension.hasCompressedFileExtension(entry.getName())) {
          nestedCompressedFiles.add(destinationFolder.resolve(entry.getName()));
        }
        extract(ti, entry, newPath);
      }
    }

    for (Path file : nestedCompressedFiles) {
      extractFile(file, file.getParent());
    }
  }

  private static void extractGzFile(final Path compressedFile, final Path destinationFolder)
      throws IOException {
    // Note: .gz files just contain one file.
    final Path destination = CompressedFileExtension
        .removeExtension(destinationFolder.resolve(compressedFile.getFileName()));
    try (final GzipCompressorInputStream inputStream = new GzipCompressorInputStream(
        Files.newInputStream(compressedFile));
        final OutputStream outputStream = Files.newOutputStream(destination)) {
      IOUtils.copy(inputStream, outputStream);
    }
  }

  private static void extractTarFile(final Path compressedFile, final Path destinationFolder)
      throws IOException {
    final List<Path> nestedCompressedFiles = new ArrayList<>();
    try (TarArchiveInputStream is = new TarArchiveInputStream(Files.newInputStream(compressedFile))) {
      TarArchiveEntry entry;
      while ((entry = is.getNextTarEntry()) != null) {
        final String entryName = replaceBannedCharacters(entry.getName());
        // create a new path, protect against malicious tar files
        final Path newPath = zipSlipVulnerabilityProtect(entryName, destinationFolder);
        if (CompressedFileExtension.hasCompressedFileExtension(entry.getName())) {
          nestedCompressedFiles.add(destinationFolder.resolve(entry.getName()));
        }
        extract(is, entry, newPath);
      }
    }
    for (Path nestedCompressedFile : nestedCompressedFiles) {
      extractFile(nestedCompressedFile, nestedCompressedFile.getParent());
    }
  }

  private static String replaceBannedCharacters(String entryName) {
    return entryName.replaceAll("[" + FILE_NAME_BANNED_CHARACTERS + "]", "_");
  }

  private static void extract(ArchiveInputStream is, ArchiveEntry entry, Path newPath) throws IOException {

    if (entry.isDirectory()) {
      Files.createDirectories(newPath);
    } else {
      // check parent folder
      Path parent = newPath.getParent();
      if (parent != null && Files.notExists(parent)) {
        Files.createDirectories(parent);
      }
      Files.copy(is, newPath, StandardCopyOption.REPLACE_EXISTING);
    }
  }

  private static Path zipSlipVulnerabilityProtect(String entryName, Path targetDir)
      throws IOException {
    // https://snyk.io/research/zip-slip-vulnerability
    Path targetDirResolved = targetDir.resolve(entryName);
    // make sure normalized file still has targetDir as its prefix else throw exception
    Path normalizePath = targetDirResolved.normalize();
    if (!normalizePath.startsWith(targetDir)) {
      throw new IOException("Entry is outside of the target dir: " + entryName);
    }
    return normalizePath;
  }
}
