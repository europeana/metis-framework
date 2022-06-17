package eu.europeana.metis.harvesting.http;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performFunction;
import static eu.europeana.metis.utils.TempFileUtils.createSecureTempDirectoryAndFile;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.harvesting.ReportingIteration.IterationResult;
import java.io.ByteArrayInputStream;
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
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * An implementation of the {@link HttpHarvester} functionality.
 */
public class HttpHarvesterImpl implements HttpHarvester {

  private static final Set<String> SUPPORTED_PROTOCOLS = Set.of("http", "https", "file");

  private static final Logger LOGGER = LoggerFactory.getLogger(HttpHarvesterImpl.class);

  @Override
  public void harvestRecords(InputStream inputStream, CompressedFileExtension compressedFileType,
      Consumer<ArchiveEntry> action) throws HarvesterException {

    // Now perform the harvesting - go by each file.
    final HttpRecordIterator iterator = createTemporaryHttpHarvestIterator(inputStream, compressedFileType);
    List<Pair<Path, Exception>> exception = new ArrayList<>(1);
    iterator.forEach(path -> {
      try (InputStream content = Files.newInputStream(path)) {
        action.accept(new ArchiveEntryImpl(path.getFileName().toString(),
            new ByteArrayInputStream(IOUtils.toByteArray(content))));
        return IterationResult.CONTINUE;
      } catch (IOException | RuntimeException e) {
        exception.add(new ImmutablePair<>(path, e));
        return IterationResult.TERMINATE;
      }
    });

    iterator.deleteIteratorContent();

    if (!exception.isEmpty()) {
      throw new HarvesterException("Could not process path " + exception.get(0).getKey() + ".",
          exception.get(0).getValue());
    }
  }

  @Override
  public HttpRecordIterator harvestRecords(String archiveUrl, String downloadDirectory)
      throws HarvesterException {

    // Download the archive. Note that we allow any directory here (even on other file systems),
    // the calling code is responsible for providing this parameter and should do so properly.
    @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN") final Path downloadDirectoryPath = Paths.get(
        downloadDirectory);
    final Path downloadedFile;
    try {
      downloadedFile = downloadFile(archiveUrl, downloadDirectoryPath);
    } catch (IOException e) {
      throw new HarvesterException("Problem downloading archive " + archiveUrl + ".", e);
    }

    // Perform the harvesting
    return harvestRecords(downloadedFile);
  }

  @Override
  public HttpRecordIterator createTemporaryHttpHarvestIterator(InputStream input, CompressedFileExtension compressedFileType)
      throws HarvesterException {
    try {
      final Path tempFile = createSecureTempDirectoryAndFile(HttpHarvesterImpl.class.getSimpleName(),
          HttpHarvesterImpl.class.getSimpleName(), compressedFileType.getExtension());
      copyInputStreamToFile(input, tempFile.toFile());
      return harvestRecords(tempFile);
    } catch (IOException e) {
      throw new HarvesterException("Problem saving archive.", e);
    }

  }

  private HttpRecordIterator harvestRecords(Path archiveFile) throws HarvesterException {

    // Extract the archive.
    final Path extractedDirectory = archiveFile.toAbsolutePath().getParent();
    if (extractedDirectory == null) {
      throw new IllegalStateException("Downloaded file should have a parent.");
    }
    try {
      CompressedFileExtractor.extractFile(archiveFile.toAbsolutePath(), extractedDirectory);
    } catch (IOException e) {
      throw new HarvesterException("Problem extracting archive.", e);
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
    final URL archiveUrl = new URL(archiveUrlString);
    if (!SUPPORTED_PROTOCOLS.contains(archiveUrl.getProtocol())) {
      throw new IOException("This functionality does not support this protocol ("
          + archiveUrl.getProtocol() + ").");
    }
    final Path directory = Files.createDirectories(downloadDirectory);
    final Path file = directory.resolve(FilenameUtils.getName(archiveUrlString));
    // Note: we allow any download URL for http harvesting. This is the functionality we support.
    @SuppressWarnings("findsecbugs:URLCONNECTION_SSRF_FD") final URLConnection urlConnection = archiveUrl.openConnection();
    try (final InputStream inputStream = urlConnection.getInputStream();
        final OutputStream outputStream = Files.newOutputStream(file)) {
      IOUtils.copyLarge(inputStream, outputStream);
    }
    return file;
  }

  /**
   * Method corrects rights on Linux systems, where created new directory and extracted files have not right copied from parent
   * folder, and they have not any right for others users. Also group is not preserved from parent. It is a problem cause apache
   * server could not reach files cause it typically works as special apache_user. The purpose of this method is to copy rights
   * from parent directory, that should have correctly configured right to passed as parameter directory and any directory or file
   * inside.
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
    public void deleteIteratorContent() {
      if (extractedDirectory != null) {
        try {
          FileUtils.deleteDirectory(extractedDirectory.toFile());
        } catch (IOException e) {
          LOGGER.warn("Could not delete directory.", e);
        }
      } else {
        LOGGER.warn("Extracted directory undefined, nothing removed.");
      }
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

  private static class ArchiveEntryImpl implements ArchiveEntry {

    final String entryName;
    final ByteArrayInputStream entryContent;

    public ArchiveEntryImpl(String entryName, ByteArrayInputStream entryContent) {
      this.entryName = entryName;
      this.entryContent = entryContent;
    }

    @Override
    public String getEntryName() {
      return entryName;
    }

    @Override
    public ByteArrayInputStream getEntryContent() {
      return entryContent;
    }
  }
}
