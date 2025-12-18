package eu.europeana.metis.harvesting.file;

import eu.europeana.metis.harvesting.FullRecord;
import eu.europeana.metis.harvesting.FullRecordImpl;
import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvesterRuntimeException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.utils.CompressedFileExtension;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Predicate;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * Abstract base class for iterators over file records in an extracted directory.
 *
 * @param <R> The type of record handled by the iterator.
 */
// todo:https://europeana.atlassian.net/browse/MET-6889. Perhaps the deletion of the directory should not be performed here and this class should become a true iterable/iterator over a stream?
@Slf4j
public abstract class AbstractFileHarvestIterator<R> implements HarvestingIterator<R, Path> {

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
      log.warn("Could not delete directory.", e);
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
    try (Stream<Path> pathStream = walkFilteredPathsStream()) {
      final Iterator<Path> iterator = pathStream.filter(filter).iterator();
      while (iterator.hasNext()) {
        Path path = iterator.next();
        if (!action.process(path)) {
          break;
        }
      }
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
      return true;
    }, path -> true);
    return counter.get();
  }

  private Stream<Path> walkFilteredPathsStream() throws IOException {
    return Files.walk(extractedDirectory).filter(AbstractFileHarvestIterator::isAcceptablePath);
  }

  protected Stream<Path> openPathStreamOrThrow() {
    try {
      return walkFilteredPathsStream();
    } catch (IOException e) {
      throw new HarvesterRuntimeException("Error walking directory: " + getExtractedDirectory(), e);
    }
  }

  protected <X> CloseableIterator<X> closeableIteratorFor(Stream<X> stream) {
    return new CloseableIterator<>() {
      private final Iterator<X> iterator = stream.iterator();
      private boolean closed;

      @Override
      public boolean hasNext() {
        return iterator.hasNext();
      }

      @Override
      public X next() {
        return iterator.next();
      }

      @Override
      public void close() {
        if (!closed) {
          closed = true;
          stream.close();
        }
      }
    };
  }

  private static boolean isAcceptablePath(Path path) {
    String pathString = path.toString();
    if (pathString.contains(MAC_TEMP_FILE) || pathString.contains(MAC_TEMP_FOLDER)) {
      return false;
    }
    if (CompressedFileExtension.forPath(path) != null) {
      return false;
    }
    return Files.isRegularFile(path);
  }
}
