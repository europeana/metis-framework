package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.HarvesterException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
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

  public static void extractFile(final String compressedFile, final String destinationFolder)
          throws HarvesterException, IOException {
    final String extension = FilenameUtils.getExtension(compressedFile);
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

  private static void extractZipFile(final String compressedFilePath,
          final String destinationFolder) throws IOException, HarvesterException {
    final List<String> zipFiles = new ArrayList<>();
    ZipUtil.unpack(new File(compressedFilePath), new File(destinationFolder), name -> {
      if (CompressedFileExtension.contains(FilenameUtils.getExtension(name))) {
        zipFiles.add(destinationFolder + name);
      }
      return name;
    });
    for (String nestedCompressedFile : zipFiles) {
      extractFile(nestedCompressedFile,
              FilenameUtils.removeExtension(nestedCompressedFile) + File.separator);
    }
  }

  private static void extractGzFile(final String compressedFile, final String destinationFolder)
          throws IOException, HarvesterException {
    final File destination = new File(destinationFolder);
    if (FilenameUtils.getName(compressedFile).contains(TAR) || (FilenameUtils
            .getExtension(compressedFile)).equals(
            CompressedFileExtension.TGZIP.getExtension())) {
      final File newDestination = extractTarGzipArchive(compressedFile, destination);
      final Iterator<File> files = FileUtils
              .iterateFiles(newDestination, CompressedFileExtension.getExtensionValues(), true);
      while (files.hasNext()) {
        final File file = files.next();
        extractFile(file.getAbsolutePath(), file.getParent() + File.separator);
      }
    } else {
      try (final GzipCompressorInputStream inputStream = new GzipCompressorInputStream(
              new FileInputStream(compressedFile));
              final FileOutputStream fileOutputStream = new FileOutputStream(
                      new File(FilenameUtils.removeExtension(compressedFile)))) {
        IOUtils.copy(inputStream, fileOutputStream);
      }
    }
  }

  private static File extractTarGzipArchive(String compressedFile, File destination)
          throws IOException {
    final Archiver archiver = ArchiverFactory.createArchiver(ArchiveFormat.TAR, CompressionType.GZIP);
    archiver.extract(new File(compressedFile), destination);
    return new File(destination.getPath() + File.separator + getFileName(compressedFile));
  }

  private static String getFileName(String fileLocation) {
    return FilenameUtils
            .getName(FilenameUtils.removeExtension(FilenameUtils.removeExtension(fileLocation)));
  }
}
