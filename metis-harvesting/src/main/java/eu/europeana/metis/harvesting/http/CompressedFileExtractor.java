package eu.europeana.metis.harvesting.http;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performFunction;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.IOUtils;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.zeroturnaround.zip.ZipUtil;

/**
 * This class contains functionality to extract archives.
 */
final class CompressedFileExtractor {

  private CompressedFileExtractor() {
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
