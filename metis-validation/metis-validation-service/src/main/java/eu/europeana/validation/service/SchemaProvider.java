package eu.europeana.validation.service;

import eu.europeana.validation.model.Schema;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides schemas based on url of predefined name (EDM-INTERNAL on EDM-EXTERNAL). Per instance of
 * this class there is a UUID generated that is used to create the directory path of where the
 * schemas will be stored. This is done so, to avoid file system collisions of processes that run
 * the exact same code and are independent from each other.
 * <p>
 * The {@link #TMP_DIR} field is set through a system variable named "java.io.tmpdir". This value
 * should be sanitized and controlled otherwise this class can become unsecure.
 * </p>
 * <p>
 * Created by pwozniak on 12/20/17
 * <p>
 */
public class SchemaProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaProvider.class);

  private static final String TMP_DIR = System.getProperty("java.io.tmpdir");
  private static final String ZIP_FILE_NAME = "zip.zip";
  private final String schemasRootDirectory;

  private final HttpClient httpClient = HttpClient.newBuilder().build();

  private final PredefinedSchemas predefinedSchemasLocations;

  /**
   * Creates {@link SchemaProvider} for given {@link PredefinedSchemas} object. The {@link
   * #schemasRootDirectory} is also calculated which includes the UUID of this {@link
   * SchemaProvider}.
   *
   * @param predefinedSchemasLocations the wrapper class with all the schema locations
   */
  public SchemaProvider(PredefinedSchemas predefinedSchemasLocations) {
    String uuidForSchemaProvider = UUID.randomUUID().toString();
    if (TMP_DIR.endsWith(File.separator)) {
      schemasRootDirectory = TMP_DIR + uuidForSchemaProvider + File.separator;
    } else {
      schemasRootDirectory = TMP_DIR + File.separator + uuidForSchemaProvider + File.separator;
    }

    LOGGER.info("Creating schema manager. Files will be stored in: {}", schemasRootDirectory);
    this.predefinedSchemasLocations = predefinedSchemasLocations;
  }

  /**
   * Retrieves schema object from given (remote) location.
   *
   * @param zipUrl place where (remote) zip file is located. Accepts url to file.
   * @param rootFileLocation indicates where root xsd file is located inside zip. The caller is
   * responsible to provide a valid path to that file
   * @param schematronLocation place where schematron file is located
   * @return schema object
   * @throws SchemaProviderException any exception that can occur during retrieving schema files
   */
  public Schema getSchema(String zipUrl, String rootFileLocation,
      String schematronLocation) throws SchemaProviderException {
    synchronized (this) {

      final String schemasDirectoryName = prepareDirectoryName(zipUrl);
      File downloadedFile = downloadZipIfNeeded(zipUrl, schemasDirectoryName);
      unzipArchiveIfNeeded(downloadedFile, rootFileLocation);
      return prepareSchema(schemasDirectoryName, downloadedFile.getParentFile(), rootFileLocation,
          schematronLocation);
    }
  }

  /**
   * Creates instance of {@link Schema} class based on provided type of schema
   *
   * @param schemaName the schema name (that will be taken from properties file)
   * @return the instance
   * @throws SchemaProviderException any exception that can occur during retrieving schema files
   */
  public Schema getSchema(String schemaName) throws SchemaProviderException {
    if (isPredefined(schemaName)) {
      return getSchema(
          predefinedSchemasLocations.get(schemaName).getLocation(),
          predefinedSchemasLocations.get(schemaName).getRootFileLocation(),
          predefinedSchemasLocations.get(schemaName).getSchematronFileLocation());
    } else {
      throw new SchemaProviderException("XSD root file not provided");
    }
  }

  public String getSchemasDirectory() {
    return schemasRootDirectory;
  }

  private String prepareDirectoryName(String name) throws SchemaProviderException {
    URL url;
    try {
      url = new URL(name);
      String host = url.getHost();
      String file = url.getFile();
      return host + "_" + StringUtils
          .substringAfter(StringUtils.substringBeforeLast(file, "."), "/");
    } catch (MalformedURLException e) {
      throw new SchemaProviderException(e);
    }
  }

  /**
   * Checks if given schema name is on the {@link PredefinedSchemas} list.
   *
   * @param name schema name
   * @return whether the name is on the list.
   */
  public boolean isPredefined(String name) {
    return predefinedSchemasLocations.contains(name);
  }

  /**
   * Downloads zip file to specified directory
   *
   * @param zipLocation location of the file to be downloaded
   * @param destinationDir place where downloaded file will be saved
   * @return newly created file
   */
  private File downloadZipIfNeeded(String zipLocation, String destinationDir)
      throws SchemaProviderException {

    HttpRequest httpRequest = HttpRequest.newBuilder()
        .GET()
        .uri(URI.create(zipLocation))
        .build();

    File schemasLocation = new File(schemasRootDirectory, destinationDir);

    if (new File(schemasLocation, ZIP_FILE_NAME).exists()) {
      LOGGER.debug("Zip file will not be downloaded, already exists in temp directory");
      return new File(schemasLocation, ZIP_FILE_NAME);
    }

    //If the zip file does not exist it means the directory would not exist either
    if (!schemasLocation.mkdirs() && !schemasLocation.exists()) {
      throw new SchemaProviderException("Unable to create schemaDirecory");
    }

    File destinationFile = new File(schemasLocation, ZIP_FILE_NAME);
    final HttpResponse<Path> httpResponse;

    try {
      httpResponse = httpClient.send(httpRequest, BodyHandlers.ofFile(Paths.get(destinationFile.toURI())));
      destinationFile = httpResponse.body().toFile();
    } catch (IOException e) {
      LOGGER.info("There was some trouble sending a request to {}", schemasLocation);
    } catch (InterruptedException e){
      Thread.currentThread().interrupt();
      LOGGER.info("The thread was interrupted");
    }

    return destinationFile;
  }

  private void unzipArchiveIfNeeded(File downloadedFile, String rootFileLocation)
      throws SchemaProviderException {

    if (rootFileExists(new File(downloadedFile.getParent()), rootFileLocation)) {
      LOGGER.debug("Archive will not be unzipped.");
    } else {
      unzipArchive(downloadedFile);
    }
  }

  private void unzipArchive(File downloadedFile) throws SchemaProviderException {
    try (ZipFile zip = new ZipFile(downloadedFile)) {
      Enumeration<? extends ZipEntry> entries = zip.entries();
      while (entries.hasMoreElements()) {
        ZipEntry entry = entries.nextElement();
        handleZipEntry(downloadedFile, zip, entry);
      }
    } catch (IOException e) {
      throw new SchemaProviderException("Exception while unzipping file", e);
    }
  }

  // We chose where to store the downloaded files.
  @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN")
  private void handleZipEntry(File downloadedFile, ZipFile zip, ZipEntry entry)
      throws SchemaProviderException, IOException {
    if (entry.isDirectory()) {
      final boolean couldCreateDir = new File(downloadedFile.getParent(), entry.getName()).mkdir();
      if (!couldCreateDir) {
        throw new SchemaProviderException("Could not create directory");
      }
    } else {
      InputStream zipStream = zip.getInputStream(entry);
      FileUtils.copyInputStreamToFile(zipStream,
          new File(downloadedFile.getParent(), entry.getName()));
    }
  }

  private Schema prepareSchema(String schemaName, File unzippedSchemaLocation,
      String rootFileLocation, String schematronLocation) throws SchemaProviderException {

    Schema schema = new Schema();
    schema.setName(schemaName);
    if (rootFileExists(unzippedSchemaLocation, rootFileLocation)) {
      schema.setPath(unzippedSchemaLocation.getAbsolutePath() + File.separator + rootFileLocation);
      if (schematronLocation != null) {
        schema.setSchematronPath(
            unzippedSchemaLocation.getAbsolutePath() + File.separator + schematronLocation);
      }
      return schema;
    } else {
      throw new SchemaProviderException("Provided root xsd file does not exist in archive");
    }
  }

  private boolean rootFileExists(File unzippedSchemaLocation, String rootFileLocation) {
    // The parameter unzippedSchemaLocation is a location chosen by the software and the new File
    // is handling the lookup for child for rootFileLocation parameter, therefore we can trust that this is safe
    @SuppressWarnings("findsecbugs:PATH_TRAVERSAL_IN")
    File rootFile = new File(unzippedSchemaLocation, rootFileLocation);
    return rootFile.exists();
  }

  /**
   * Delete {@link #schemasRootDirectory} that was generated for this instance at the beginning.
   *
   * @throws IOException if an exception happened while trying to delete the directory
   */
  public void cleanUp() throws IOException {
    FileUtils.deleteDirectory(new File(schemasRootDirectory));
  }
}
