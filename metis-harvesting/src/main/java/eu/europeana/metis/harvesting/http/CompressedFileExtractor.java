package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.commons.compress.compressors.gzip.GzipCompressorInputStream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.rauschig.jarchivelib.ArchiveFormat;
import org.rauschig.jarchivelib.Archiver;
import org.rauschig.jarchivelib.ArchiverFactory;
import org.rauschig.jarchivelib.CompressionType;
import org.zeroturnaround.zip.ZipUtil;

final class CompressedFileExtractor {

  public static final String TAR = ".tar";

  private CompressedFileExtractor() {
    // This class is not meant to be instantiated.
  }

  public static void extractFile(final Path compressedFile, final Path destinationFolder)
          throws HarvesterException, IOException {
    final String extension = FilenameUtils.getExtension(compressedFile.toString());
    final CompressedFileExtension compressingExtension = CompressedFileExtension
            .forExtension(extension);
    if (compressingExtension == null) {
      throw new HarvesterException("Can't process archive of this type: " + extension);
    }
    switch (compressingExtension) {
      case ZIP:
        extractZipFile(compressedFile, destinationFolder);
        break;
      case GZIP:
      case TGZIP:
        extractGzFile(compressedFile, destinationFolder);
        break;
      default:
        throw new IllegalStateException(
                "Shouldn't be here. Extension found: " + compressingExtension.name());
    }
  }

  private static void extractZipFile(final Path compressedFile,
          final Path destinationFolder) throws IOException, HarvesterException {
    final List<Path> zipFiles = new ArrayList<>();
    ZipUtil.unpack(compressedFile.toFile(), destinationFolder.toFile(), name -> {
      if (CompressedFileExtension.contains(FilenameUtils.getExtension(name))) {
        zipFiles.add(destinationFolder.resolve(name));
      }
      return name;
    });
    for (Path nestedCompressedFile : zipFiles) {
      extractFile(nestedCompressedFile,
              Path.of(FilenameUtils.removeExtension(nestedCompressedFile.toString())));
    }
  }

  private static void extractGzFile(final Path compressedFile, final Path destinationFolder)
          throws IOException, HarvesterException {
    if (FilenameUtils.getName(compressedFile.toString()).contains(TAR) || (FilenameUtils
            .getExtension(compressedFile.toString())).equals(
            CompressedFileExtension.TGZIP.getExtension())) {
      final Path newDestination = extractTarGzipArchive(compressedFile, destinationFolder);
      final Iterator<File> files = FileUtils
              .iterateFiles(newDestination.toFile(), CompressedFileExtension.getExtensionValues(),
                      true);
      while (files.hasNext()) {
        final File file = files.next();
        extractFile(Path.of(file.getAbsolutePath()), Path.of(file.getParent() + File.separator));
      }
    } else {
      try (final GzipCompressorInputStream inputStream = new GzipCompressorInputStream(
              Files.newInputStream(compressedFile));
              final OutputStream outputStream = Files.newOutputStream(
                      Path.of(FilenameUtils.removeExtension(compressedFile.toString())))) {
        IOUtils.copy(inputStream, outputStream);
      }
    }
  }

  private static Path extractTarGzipArchive(Path compressedFile, Path destination)
          throws IOException {
    final Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
    archiver.extract(compressedFile.toFile(), destination.toFile());
    return destination.resolve(getFileName(compressedFile.toString()));
  }

  private static String getFileName(String fileLocation) {
    return FilenameUtils
            .getName(FilenameUtils.removeExtension(FilenameUtils.removeExtension(fileLocation)));
  }
}
