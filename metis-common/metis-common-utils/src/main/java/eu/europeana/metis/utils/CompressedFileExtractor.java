package eu.europeana.metis.utils;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performFunction;
import static eu.europeana.metis.utils.TempFileUtils.createSecureTempFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeroturnaround.zip.ZipUtil;

/**
 * This class contains functionality to extract archives.
 */
public class CompressedFileExtractor {

  private static final Logger LOGGER = LoggerFactory.getLogger(CompressedFileExtractor.class);

  private static final String MAC_TEMP_FOLDER = "__MACOSX";
  private static final String MAC_TEMP_FILE = ".DS_Store";

  public CompressedFileExtractor() {
    // This class is not meant to be instantiated.
  }

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
      case ZIP:
        extractZipFile(compressedFile, destinationFolder);
        break;
      case GZIP:
        extractGzFile(compressedFile, destinationFolder);
        break;
      case TGZIP:
      case TAR_GZ:
        extractTarGzFile(compressedFile, destinationFolder);
        break;
      default:
        throw new IllegalStateException(
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

  List<String> getRecordsFromZipFile(ZipFile zipFile) throws IOException {
    final List<InputStream> streams = getContentFromZipFile(zipFile);
    final List<String> result = new ArrayList<>(streams.size());
    for (InputStream stream : streams) {
      result.add(IOUtils.toString(stream, StandardCharsets.UTF_8.name()));
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

  boolean accept(ZipEntry zipEntry) {
    return !zipEntry.isDirectory() && !zipEntry.getName().startsWith(MAC_TEMP_FOLDER)
        && !zipEntry.getName().endsWith(MAC_TEMP_FILE);
  }


  private static void extractZipFile(final Path compressedFile, final Path destinationFolder) throws IOException {
    final List<Path> nestedCompressedFiles = new ArrayList<>();
    ZipUtil.unpack(compressedFile.toFile(), destinationFolder.toFile(), name -> {
      if (CompressedFileExtension.hasCompressedFileExtension(name)) {
        nestedCompressedFiles.add(destinationFolder.resolve(name));
      }
      return name;
    });
    for (Path nestedCompressedFile : nestedCompressedFiles) {
      extractFile(nestedCompressedFile, nestedCompressedFile.getParent());
    }
  }

  private static void extractTarGzFile(final Path compressedFile, final Path destinationFolder)
      throws IOException {
    final Archiver archiver = ArchiverFactory
        .createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
    archiver.extract(compressedFile.toFile(), destinationFolder.toFile());
    final Path newDestination = CompressedFileExtension
        .removeExtension(destinationFolder.resolve(compressedFile.getFileName()));
    final Set<Path> nestedCompressedFiles;
    try (Stream<Path> nestedFilesStream = Files.walk(newDestination)) {
      nestedCompressedFiles = performFunction(nestedFilesStream, stream -> stream
          .filter(CompressedFileExtension::hasCompressedFileExtension)
          .collect(Collectors.toSet()));
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
}
