package eu.europeana.metis.harvesting.http;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performFunction;
import static eu.europeana.metis.utils.TempFileUtils.createSecureTempDirectoryAndFile;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

import eu.europeana.metis.harvesting.FullRecordHarvestingIterator;
import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.harvesting.ReportingIteration.IterationResult;
import eu.europeana.metis.utils.CompressedFileExtension;
import eu.europeana.metis.utils.CompressedFileHandler;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.URISyntaxException;
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
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.function.Predicate;
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
    try (final HarvestingIterator<ArchiveEntry, Path> iterator = createFullRecordHarvestIterator(inputStream, compressedFileType)) {
      iterator.forEach(file -> {
        action.accept(file);
        return IterationResult.CONTINUE;
      });
    } catch (IOException e) {
      throw new HarvesterException("Could not clean up.", e);
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
    } catch (IOException | URISyntaxException e) {
      throw new HarvesterException("Problem downloading archive " + archiveUrl + ".", e);
    }

    // Perform the harvesting
    return new PathIterator(extractArchive(downloadedFile));
  }

  @Override
  public FullRecordHarvestingIterator<ArchiveEntry, Path> createFullRecordHarvestIterator(InputStream input,
      CompressedFileExtension compressedFileType) throws HarvesterException {
    return new RecordIterator(extractArchiveSecurely(input, compressedFileType));
  }

  @Override
  public HttpRecordIterator createTemporaryHttpHarvestIterator(InputStream input,
      CompressedFileExtension compressedFileType) throws HarvesterException {
    return new PathIterator(extractArchiveSecurely(input, compressedFileType));
  }

  private Path extractArchiveSecurely(InputStream input,
      CompressedFileExtension compressedFileType) throws HarvesterException {
    try {
      final Path tempFile = createSecureTempDirectoryAndFile(HttpHarvesterImpl.class.getSimpleName(),
          HttpHarvesterImpl.class.getSimpleName(), compressedFileType.getExtension());
      copyInputStreamToFile(input, tempFile.toFile());
      return extractArchive(tempFile);
    } catch (IOException e) {
      throw new HarvesterException("Problem saving archive.", e);
    }
  }

  private Path extractArchive(Path archiveFile) throws HarvesterException {

    // Extract the archive.
    final Path extractedDirectory = archiveFile.toAbsolutePath().getParent();
    if (extractedDirectory == null) {
      throw new IllegalStateException("Downloaded file should have a parent.");
    }
    try {
      CompressedFileHandler.extractFile(archiveFile.toAbsolutePath(), extractedDirectory);
    } catch (IOException e) {
      throw new HarvesterException("Problem extracting archive.", e);
    }

    // correct directory rights
    try {
      correctDirectoryRights(extractedDirectory);
    } catch (IOException e) {
      throw new HarvesterException("Problem correcting directory rights.", e);
    }

    // Return the extracted directory
    return extractedDirectory;
  }

  private Path downloadFile(String archiveUrlString, Path downloadDirectory) throws IOException, URISyntaxException {
    final URL archiveUrl = new URI(archiveUrlString).toURL();
    if (!SUPPORTED_PROTOCOLS.contains(archiveUrl.getProtocol())) {
      throw new IOException("This functionality does not support this protocol ("
          + archiveUrl.getProtocol() + ").");
    }
    final Path directory = Files.createDirectories(downloadDirectory);
    final Path file = directory.resolve(FilenameUtils.getName(archiveUrlString));
    // Note: we allow any download URL for http harvesting. This is the functionality we support.
    @SuppressWarnings("findsecbugs:URLCONNECTION_SSRF_FD")
    final URLConnection urlConnection = archiveUrl.openConnection();
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

  private static class RecordIterator extends HttpHarvestIterator<ArchiveEntry>
      implements FullRecordHarvestingIterator<ArchiveEntry, Path> {

    public RecordIterator(Path extractedDirectory) {
      super(extractedDirectory);
    }

    @Override
    public void forEachFiltered(ReportingIteration<ArchiveEntry> action, Predicate<Path> filter)
        throws HarvesterException {
      forEachFileFiltered(action, filter);
    }
  }

  private static class PathIterator extends HttpHarvestIterator<Path> implements HttpRecordIterator {

    public PathIterator(Path extractedDirectory) {
      super(extractedDirectory);
    }

    @Override
    public void forEachFiltered(ReportingIteration<Path> action, Predicate<Path> filter)
        throws HarvesterException {
      forEachPathFiltered(action, filter);
    }

    @Override
    public String getExtractedDirectory() {
      return super.getExtractedDirectory();
    }

    @Override
    public void deleteIteratorContent() {
      this.close();
    }
  }

  /**
   * Iterator for harvesting
   */
  private static abstract class HttpHarvestIterator<R> implements HarvestingIterator<R, Path> {

    private final Path extractedDirectory;

    public HttpHarvestIterator(Path extractedDirectory) {
      this.extractedDirectory = extractedDirectory;
    }

    protected String getExtractedDirectory() {
      return extractedDirectory != null ? extractedDirectory.toString() : "";
    }

    @Override
    public void close() {
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

    public void forEachPathFiltered(ReportingIteration<Path> action, Predicate<Path> filter)
        throws HarvesterException {
      try {
        Files.walkFileTree(extractedDirectory, new FileIteration(action, filter));
      } catch (IOException e) {
        throw new HarvesterException("Exception while iterating through the extracted files.", e);
      }
    }

    public void forEachFileFiltered(ReportingIteration<ArchiveEntry> action, Predicate<Path> filter)
        throws HarvesterException {
      List<Pair<Path, Exception>> exception = new ArrayList<>(1);
      forEachPathFiltered(path -> {
        try (InputStream content = Files.newInputStream(path)) {
          return action.process(new ArchiveEntryImpl(path.getFileName().toString(),
              new ByteArrayInputStream(IOUtils.toByteArray(content))));
        } catch (IOException | RuntimeException e) {
          exception.add(new ImmutablePair<>(path, e));
          return IterationResult.TERMINATE;
        }
      }, filter);
      if (!exception.isEmpty()) {
        throw new HarvesterException("Could not process path " + exception.get(0).getKey() + ".",
            exception.get(0).getValue());
      }
    }

    @Override
    public void forEachNonDeleted(ReportingIteration<R> action) throws HarvesterException {
      forEach(action);
    }

    @Override
    public Integer countRecords() throws HarvesterException {
      // Go by each path only: no need to inspect the full file.
      final AtomicInteger counter = new AtomicInteger(0);
      forEachPathFiltered(path -> {
        counter.incrementAndGet();
        return IterationResult.CONTINUE;
      }, path -> true);
      return counter.get();
    }
  }

  private static class FileIteration extends SimpleFileVisitor<Path> {

    private static final String MAC_TEMP_FILE = ".DS_Store";
    private static final String MAC_TEMP_FOLDER = "__MACOSX";

    private final ReportingIteration<Path> action;
    private final Predicate<Path> filter;

    public FileIteration(ReportingIteration<Path> action, Predicate<Path> filter) {
      this.action = action;
      this.filter = filter;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
      if (!filter.test(file)) {
        return FileVisitResult.CONTINUE;
      }
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

  private record ArchiveEntryImpl(String entryName, ByteArrayInputStream entryContent)
      implements ArchiveEntry {

    @Override
    public String getHarvestingIdentifier() {
      return entryName;
    }

    @Override
    public ByteArrayInputStream getContent() {
      return entryContent;
    }

    @Override
    @Deprecated
    public ByteArrayInputStream getEntryContent() {
      return getContent();
    }
  }
}
