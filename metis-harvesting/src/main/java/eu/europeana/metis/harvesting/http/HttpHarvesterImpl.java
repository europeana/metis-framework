package eu.europeana.metis.harvesting.http;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performFunction;

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

/**
 * An implementation of the {@link HttpHarvester} functionality.
 */
public class HttpHarvesterImpl implements HttpHarvester {

  private static final Set<String> SUPPORTED_PROTOCOLS = Set.of("http", "https", "file");

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpHarvesterImpl.class);

  @Override
  public HttpRecordIterator harvestRecords(String archiveUrl, String downloadDirectory)
          throws HarvesterException {

    // Download the archive. Note that we allow any directory here (even on other file systems),
    // the calling code is responsible for providing this parameter and should do so properly.
    @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN")
    final Path downloadDirectoryPath = Paths.get(downloadDirectory);
    final Path downloadedFile;
    try {
      downloadedFile = downloadFile(archiveUrl, downloadDirectoryPath);
    } catch (IOException e) {
      throw new HarvesterException("Problem downloading archive " + archiveUrl + ".", e);
    }

    // Extract the archive.
    final Path extractedDirectory = downloadedFile.toAbsolutePath().getParent();
    if (extractedDirectory == null) {
      throw new IllegalStateException("Downloaded file should have a parent.");
    }
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

  private Path downloadFile(String archiveUrlString, Path downloadDirectory) throws IOException {
    final Path directory = Files.createDirectories(downloadDirectory);
    final Path file = directory.resolve(FilenameUtils.getName(archiveUrlString));
    final URL archiveUrl = new URL(archiveUrlString);
    if (!SUPPORTED_PROTOCOLS.contains(archiveUrl.getProtocol())) {
      throw new IOException("This functionality does not support this protocol ("
              + archiveUrl.getProtocol() + ").");
    }
    // Note: we allow any download URL for http harvesting. This is the functionality we support.
    @SuppressWarnings("findsecbugs:URLCONNECTION_SSRF_FD")
    final URLConnection conn = archiveUrl.openConnection();
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
    if (directory.getParent() == null) {
      LOGGER.info("No containing parent directory - no need to correct rights.");
      return;
    }
    try (Stream<Path> files = Files.walk(directory)) {
      final Set<PosixFilePermission> rights = Files.getPosixFilePermissions(directory.getParent());
      final Iterator<Path> i = performFunction(files, Stream::iterator);
      while (i.hasNext()) {
        Files.setPosixFilePermissions(i.next(), rights);
      }
    } catch (UnsupportedOperationException e) {
      LOGGER.info("Not a Posix system - no need to correct rights");
      LOGGER.debug("Exception ignored.", e);
    }
  }

  /**
   * Iterator for harvesting
   */
  private static class FileIterator implements HttpRecordIterator {

    private final Path extractedDirectory;

    public FileIterator(Path extractedDirectory) {
      this.extractedDirectory = extractedDirectory;
    }

    @Override
    public void forEach(ReportingIteration<Path> action) throws HarvesterException {
      try {
        Files.walkFileTree(extractedDirectory, new FileIteration(action));
      } catch (IOException e) {
        throw new HarvesterException("Exception while iterating through the extracted files.", e);
      }
    }
  }

  private static class FileIteration extends SimpleFileVisitor<Path> {

    private static final String MAC_TEMP_FILE = ".DS_Store";
    private static final String MAC_TEMP_FOLDER = "__MACOSX";

    private final ReportingIteration<Path> action;

    public FileIteration(ReportingIteration<Path> action) {
      this.action = action;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      final Path fileName = file.getFileName();
      if (fileName != null && MAC_TEMP_FILE.equals(fileName.toString())) {
        return FileVisitResult.CONTINUE;
      }
      if (CompressedFileExtension.forPath(file) != null) {
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
      final Path dirName = dir.getFileName();
      if (dirName != null && MAC_TEMP_FOLDER.equals(dirName.toString())) {
        return FileVisitResult.SKIP_SUBTREE;
      }
      return FileVisitResult.CONTINUE;
    }
  }
}
