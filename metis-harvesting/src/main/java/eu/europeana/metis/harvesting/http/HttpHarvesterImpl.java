package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.harvesting.ReportingIteration.IterationResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Iterator;
import java.util.Set;
import java.util.stream.Stream;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpHarvesterImpl implements HttpHarvester {

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpHarvesterImpl.class);

  @Override
  public HttpRecordIterator harvestRecords(String archiveUrl, String downloadDirectory)
          throws HarvesterException {

    // Download the archive.
    final Path downloadDirectoryPath = Paths.get(downloadDirectory);
    final Path downloadedFile;
    try {
      downloadedFile = downloadFile(archiveUrl, downloadDirectoryPath);
    } catch (IOException e) {
      throw new HarvesterException("Problem downloading archive " + archiveUrl + ".", e);
    }

    // Extract the archive.
    final Path extractedDirectory = downloadedFile.toAbsolutePath().getParent();
    try {
      CompressedFileExtractor.extractFile(downloadedFile.toAbsolutePath(), extractedDirectory);
    } catch (IOException e) {
      throw new HarvesterException("Problem extracting archive " + archiveUrl + ".", e);
    }

    // correct directory rights
    try {
      correctDirectoryRights(extractedDirectory);
    } catch (IOException e) {
      throw new HarvesterException("Problem correcting directory rights.", e);
    }

    // Return the iterator
    return new FileIterator(extractedDirectory);
  }

  private Path downloadFile(String archiveUrl, Path downloadDirectory) throws IOException {
    final Path directory = Files.createDirectories(downloadDirectory);
    final Path file = directory.resolve(FilenameUtils.getName(archiveUrl));
    final URLConnection conn = new URL(archiveUrl).openConnection();
    try (final InputStream inputStream = conn.getInputStream();
            final OutputStream outputStream = Files.newOutputStream(file)) {
      IOUtils.copyLarge(inputStream, outputStream);
    }
    return file;
  }

  /**
   * Method corrects rights on Linux systems, where created new directory and extracted files have
   * not right copied from parent folder, and they have not any right for others users. Also group
   * is not preserved from parent. It is a problem cause apache server could not reach files cause
   * it typically works as special apache_user. The purpose of this method is to copy rights from
   * parent directory, that should have correctly configured right to passed as parameter directory
   * and any directory or file inside.
   *
   * @param directory directory for which rights will be updated
   * @throws IOException in case of rights update failure
   */
  private static void correctDirectoryRights(Path directory) throws IOException {
    try (Stream<Path> files = Files.walk(directory)) {
      final Set<PosixFilePermission> rights = Files.getPosixFilePermissions(directory.getParent());
      final Iterator<Path> i = files.iterator();
      while (i.hasNext()) {
        Files.setPosixFilePermissions(i.next(), rights);
      }
    } catch (UnsupportedOperationException e) {
      LOGGER.info("Not Posix system. Need not correct rights");
    }
  }

  /**
   * Iterator for harvesting
   */
  private static class FileIterator implements HttpRecordIterator {

    private static final String MAC_TEMP_FILE = ".DS_Store";
    private static final String MAC_TEMP_FOLDER = "__MACOSX";

    private final Path extractedDirectory;

    public FileIterator(Path extractedDirectory) {
      this.extractedDirectory = extractedDirectory;
    }

    @Override
    public void forEach(ReportingIteration<Path> action) throws HarvesterException {
      try {
        forEachInternal(action);
      } catch (IOException e) {
        throw new HarvesterException("Exception while iterating through the extracted files.", e);
      }
    }

    private void forEachInternal(ReportingIteration<Path> action) throws IOException {
      Files.walkFileTree(extractedDirectory, new SimpleFileVisitor<>() {
        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
          if (file.getFileName().toString().equals(MAC_TEMP_FILE)) {
            return FileVisitResult.CONTINUE;
          }
          final String extension = FilenameUtils.getExtension(file.toString());
          if (CompressedFileExtension.contains(extension)) {
            return FileVisitResult.CONTINUE;
          }
          final IterationResult result = action.process(file);
          if (result == null) {
            throw new IllegalArgumentException("Iteration result cannot be null.");
          }
          return IterationResult.TERMINATE == result ? FileVisitResult.TERMINATE
                  : FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
          String dirName = dir.getFileName().toString();
          if (dirName.equals(MAC_TEMP_FOLDER))
            return FileVisitResult.SKIP_SUBTREE;
          return FileVisitResult.CONTINUE;
        }
      });
    }
  }
}