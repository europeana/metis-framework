package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.FullRecord;
import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.harvesting.ReportingIteration.IterationResult;
import eu.europeana.metis.utils.CompressedFileExtension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.invoke.MethodHandles;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.time.Instant;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Iterator for harvesting
 */
abstract class AbstractHttpHarvestIterator<R> implements HarvestingIterator<R, Path> {

  private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
  private final Path extractedDirectory;

  protected AbstractHttpHarvestIterator(Path extractedDirectory) {
    if (extractedDirectory == null) {
      throw new IllegalStateException("Extracted directory is null. This should not happen.");
    }
    this.extractedDirectory = extractedDirectory;
  }

  protected String getExtractedDirectory() {
    return extractedDirectory.toString();
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
      try (InputStream content = Files.newInputStream(path)) {
        return action.process(new FullRecordImpl(extractedDirectory.relativize(path).toString(),
            new ByteArrayInputStream(IOUtils.toByteArray(content))));
      } catch (RuntimeException e) {
        throw new IOException("Could not process path " + path + ".", e);
      }
    }, filter);
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
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
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
      return result == IterationResult.TERMINATE ? FileVisitResult.TERMINATE
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

  record FullRecordImpl(String relativeFilePath, ByteArrayInputStream entryContent) implements FullRecord {

    @Override
    public String getHarvestingIdentifier() {
      return relativeFilePath;
    }

    @Override
    public void writeContent(OutputStream outputStream) throws IOException {
      IOUtils.copy(entryContent, outputStream);
    }

    @Override
    public ByteArrayInputStream getContent() {
      return entryContent;
    }

    @Override
    public boolean isDeleted() {
      return false;
    }

    @Override
    public Instant getTimeStamp() {
      return null;
    }
  }
}




