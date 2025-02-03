package eu.europeana.metis.harvesting.http;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performFunction;
import static eu.europeana.metis.utils.TempFileUtils.createSecureTempDirectoryAndFile;
import static org.apache.commons.io.FileUtils.copyInputStreamToFile;

import eu.europeana.metis.harvesting.FullRecord;
import eu.europeana.metis.harvesting.FullRecordHarvestingIterator;
import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvestingIterator;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.utils.CompressedFileExtension;
import eu.europeana.metis.utils.CompressedFileHandler;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.util.Iterator;
import java.util.Set;
import java.util.function.Predicate;
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
  public void harvestFullRecords(InputStream inputStream,
      CompressedFileExtension compressedFileType, ReportingIteration<FullRecord> action)
      throws HarvesterException {
    try (final HarvestingIterator<FullRecord, Path> iterator = createFullRecordHarvestIterator(inputStream, compressedFileType)) {
      iterator.forEach(action);
    } catch (IOException e) {
      throw new HarvesterException("Could not clean up.", e);
    }
  }

  @Override
  public HarvestingIterator<Path, Path> harvestRecords(String archiveUrl, String downloadDirectory)
      throws HarvesterException {

    // Download the archive. Note that we allow any directory here (even on other file systems),
    // the calling code is responsible for providing this parameter and should do so properly.
    @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN") final Path downloadDirectoryPath = Paths.get(
        downloadDirectory);
    final Path downloadedFile = downloadFile(archiveUrl, downloadDirectoryPath);

    // Perform the harvesting
    return new PathIterator(extractArchive(downloadedFile));
  }

  @Override
  public FullRecordHarvestingIterator<FullRecord, Path> createFullRecordHarvestIterator(InputStream input,
      CompressedFileExtension compressedFileType) throws HarvesterException {
    return new RecordIterator(extractArchiveSecurely(input, compressedFileType));
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
    final Path extractedDirectory = archiveFile.toAbsolutePath().getParent();
    extractArchive(archiveFile, extractedDirectory);
    // Return the extracted directory
    return extractedDirectory;
  }

  @Override
  public void extractArchive(Path archiveFile, Path extractedDirectory) throws HarvesterException{
    // Extract the archive.
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
  }

  @Override
  public Path downloadFile(String archiveUrlString, Path downloadDirectory) throws HarvesterException {
    try{
      final URL archiveUrl = createUrl(archiveUrlString);
      final Path directory = Files.createDirectories(downloadDirectory);
      return downloadFileToExistingDirectory(archiveUrlString, directory, archiveUrl);
    } catch (IOException | URISyntaxException e) {
      throw new HarvesterException("Problem downloading archive " + archiveUrlString + ".", e);
    }
  }

  private static URL createUrl(String archiveUrlString) throws URISyntaxException, IOException {
    final URL archiveUrl;
    try {
      archiveUrl = new URI(archiveUrlString).toURL();
    } catch (IllegalArgumentException e) {
      throw new MalformedURLException(e.getMessage());
    }
    if (!SUPPORTED_PROTOCOLS.contains(archiveUrl.getProtocol())) {
      throw new IOException("This functionality does not support this protocol ("
          + archiveUrl.getProtocol() + ").");
    }
    return archiveUrl;
  }

  private static Path downloadFileToExistingDirectory(String archiveUrlString, Path directory, URL archiveUrl) throws IOException {
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

  private static class RecordIterator extends AbstractHttpHarvestIterator<FullRecord>
      implements FullRecordHarvestingIterator<FullRecord, Path> {

    public RecordIterator(Path extractedDirectory) {
      super(extractedDirectory);
    }

    @Override
    public void forEachFiltered(ReportingIteration<FullRecord> action, Predicate<Path> filter)
        throws HarvesterException {
      forEachFileFiltered(action, filter);
    }
  }

}
