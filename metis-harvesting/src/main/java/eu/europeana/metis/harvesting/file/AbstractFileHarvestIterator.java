package eu.europeana.metis.harvesting.file;

import eu.europeana.metis.harvesting.FullRecord;
import eu.europeana.metis.harvesting.FullRecordImpl;
import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.harvesting.ReportingIteration.IterationResult;
import eu.europeana.metis.utils.CompressedFileExtension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iterator for harvesting
 */
public abstract class AbstractFileHarvestIterator<R> implements HarvestingIterator<R, Path> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private static final String MAC_TEMP_FILE = ".DS_Store";
  private static final String MAC_TEMP_FOLDER = "__MACOSX";
  private final Path extractedDirectory;

  protected AbstractFileHarvestIterator(Path extractedDirectory) {
    Objects.requireNonNull(extractedDirectory, "Extracted directory is null. This should not happen.");
    this.extractedDirectory = extractedDirectory;
  }

  public Path getExtractedDirectory() {
    return extractedDirectory;
  }

  @Override
  public void close() {
    try {
      FileUtils.deleteDirectory(extractedDirectory.toFile());
    } catch (IOException e) {
      LOGGER.warn("Could not delete directory.", e);
    }
  }

  /**
   * Iterate through the record paths while applying a filter (potentially skipping some records).
   *
   * @param action The iteration to perform. It needs to return a result.
   * @param filter The filter to apply (only records that return true will be sent to the action).
   * @throws HarvesterException In case there was a problem while harvesting.
   */
  public void forEachPathFiltered(ReportingIteration<Path> action, Predicate<Path> filter)
      throws HarvesterException {
    try {
      Files.walkFileTree(extractedDirectory, new FileIteration(action, filter));
    } catch (IOException e) {
      throw new HarvesterException("Exception while iterating through the extracted files.", e);
    }
  }

  /**
   * Iterate through the {@link FullRecord} while applying a filter (potentially skipping some records).
   *
   * @param action The iteration to perform. It needs to return a result.
   * @param filter The filter to apply (only records that return true will be sent to the action).
   * @throws HarvesterException In case there was a problem while harvesting.
   */
  public void forEachFileFiltered(ReportingIteration<FullRecord> action, Predicate<Path> filter)
      throws HarvesterException {
    forEachPathFiltered(path -> {
      FullRecord fullRecord = getFullRecord(path);
      return action.process(fullRecord);
    }, filter);
  }

  protected FullRecord getFullRecord(Path path) throws IOException {
    FullRecord fullRecord;
    try (InputStream content = Files.newInputStream(path)) {
      fullRecord = new FullRecordImpl(getExtractedDirectory().relativize(path).toString(),
          new ByteArrayInputStream(IOUtils.toByteArray(content)));
    } catch (RuntimeException e) {
      throw new IOException("Could not process path " + path + ".", e);
    }
    return fullRecord;
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

  protected Stream<Path> walkFilteredFiles() throws IOException {
    return Files.walk(extractedDirectory)
                .filter(Files::isRegularFile)
                .filter(AbstractFileHarvestIterator::isAcceptableFile);
  }

  private static boolean isAcceptableFile(Path path) {
    String fileName = path.getFileName().toString();
    if (MAC_TEMP_FILE.equals(fileName)) {
      return false;
    }
    if (path.toString().contains(MAC_TEMP_FOLDER)) {
      return false;
    }
    if (CompressedFileExtension.forPath(path) != null) {
      return false;
    }
    return Files.isRegularFile(path);
  }

  protected static boolean isSkippableDirectory(Path dir) {
    final Path dirName = dir.getFileName();
    return dirName != null && MAC_TEMP_FOLDER.equals(dirName.toString());
  }

  private static class FileIteration extends SimpleFileVisitor<Path> {

    private final ReportingIteration<Path> action;
    private final Predicate<Path> filter;

    public FileIteration(ReportingIteration<Path> action, Predicate<Path> filter) {
      this.action = action;
      this.filter = filter;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
      if (isSkippableDirectory(dir)) {
        return FileVisitResult.SKIP_SUBTREE;
      }
      return FileVisitResult.CONTINUE;
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
      if (!filter.test(file)) {
        return FileVisitResult.CONTINUE;
      }

      if (!AbstractFileHarvestIterator.isAcceptableFile(file)) {
        return FileVisitResult.CONTINUE;
      }
      final IterationResult result = action.process(file);
      if (result == null) {
        throw new IllegalArgumentException("Iteration result cannot be null.");
      }
      return result == IterationResult.TERMINATE ? FileVisitResult.TERMINATE
          : FileVisitResult.CONTINUE;
    }
  }
}
