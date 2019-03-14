package eu.europeana.validation.service;

import eu.europeana.validation.model.Schema;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Files;
import java.util.Enumeration;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Provides schemas based on url of predefined name (EDM-INTERNAL on EDM-EXTERNAL). Per instance of
 * this class there is a UUID generated that is used to create the directory path of where the
 * schemas will be stored. This is done so, to avoid file system collisions of processes that run
 * the exact same code and are independent from each other.
 * <p>
 * Created by pwozniak on 12/20/17
 * <p>
 */
public class SchemaProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaProvider.class);

  public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
  private static final String ZIP_FILE_NAME = "zip.zip";
  public final String UUID_FOR_SCHEMA_PROVIDER = UUID.randomUUID().toString();
  private String schemasRootDirectory;

  private final PredefinedSchemas predefinedSchemasLocations;

  /**
   * Creates {@link SchemaProvider} for given {@link PredefinedSchemas} object. The {@link
   * #schemasRootDirectory} is also calculated which includes the UUID of this {@link
   * SchemaProvider}.
   *
   * @param predefinedSchemasLocations the wrapper class with all the schema locations
   */
  public SchemaProvider(PredefinedSchemas predefinedSchemasLocations) {
    if (TMP_DIR.endsWith(File.separator)) {
      schemasRootDirectory = TMP_DIR + UUID_FOR_SCHEMA_PROVIDER + File.separator;
    } else {
      schemasRootDirectory = TMP_DIR + File.separator + UUID_FOR_SCHEMA_PROVIDER + File.separator;
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
  public synchronized Schema getSchema(String zipUrl, String rootFileLocation,
      String schematronLocation) throws SchemaProviderException {

    final String schemasDirectoryName = prepareDirectoryName(zipUrl);
    File downloadedFile = downloadZipIfNeeded(zipUrl, schemasDirectoryName);
    unzipArchiveIfNeeded(downloadedFile, rootFileLocation);
    return prepareSchema(schemasDirectoryName, downloadedFile.getParentFile(), rootFileLocation,
        schematronLocation);
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

    File schemasLocation = new File(schemasRootDirectory, destinationDir);

    if (new File(schemasLocation, ZIP_FILE_NAME).exists()) {
      LOGGER.debug("Zip file will not be downloaded, already exists in temp directory");
      return new File(schemasLocation, ZIP_FILE_NAME);
    }

    try {
      FileUtils.deleteDirectory(schemasLocation);
    } catch (IOException e) {
      throw new SchemaProviderException("Unable to clean schemaDirecory", e);
    }

    if (!schemasLocation.mkdirs()) {
      throw new SchemaProviderException("Unable to create schemaDirecory");
    }

    final File destinationFile = new File(schemasLocation, ZIP_FILE_NAME);
    try (InputStream urlLocation = new URL(zipLocation).openStream();
        OutputStream fos = Files.newOutputStream(destinationFile.toPath())
    ) {
      IOUtils.copy(urlLocation, fos);
      return destinationFile;
    } catch (IOException e) {
      throw new SchemaProviderException("Unable to store schema file", e);
    }
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
        if (entry.isDirectory()) {
          new File(downloadedFile.getAbsolutePath()).mkdir();
        } else {
          InputStream zipStream = zip.getInputStream(entry);
          FileUtils.copyInputStreamToFile(zipStream,
              new File(downloadedFile.getParent(), entry.getName()));
        }
      }
    } catch (IOException e) {
      throw new SchemaProviderException("Exception while unzipping file", e);
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
    File rootFile = new File(unzippedSchemaLocation, rootFileLocation);
    return rootFile.exists();
  }
}
