package eu.europeana.validation.service;

import eu.europeana.validation.model.Schema;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Provides schemas based on url of predefined name (EDM-Internal on EDM-External).
 * <p>
 * Created by pwozniak on 12/20/17
 * <p>
 */
public class SchemaProvider {

  private static final Logger LOGGER = LoggerFactory.getLogger(SchemaProvider.class);

  public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
  private static final String ZIP_FILE_NAME = "zip.zip";
  private String schemasDir;

  private final PredefinedSchemas predefinedSchemasLocations;

  /**
   * Creates {@link SchemaProvider} for given {@link PredefinedSchemas} object.
   */
  public SchemaProvider(PredefinedSchemas predefinedSchemasLocations) {
    if (TMP_DIR.endsWith(File.separator)) {
      schemasDir = TMP_DIR + "schemas" + File.separator;
    } else {
      schemasDir = TMP_DIR + File.separator + "schemas" + File.separator;
    }

    LOGGER.info("Creating schema manager. Files will be stored in: {}", schemasDir);
    this.predefinedSchemasLocations = predefinedSchemasLocations;
  }

  /**
   * Retrieves schema object from given (remote) location.
   *
   * @param fileLocation place where (remote) zip file is located.
   * Accepts url to file or one of the predefined values (EDM-INTERNAL or EDM-EXTERNAL)
   * @param rootFileLocation indicates where root xsd file is located inside zip.
   * The caller is responsible to provide propper path (for example propper slashes)
   * @param schematronLocation place where schematron file is located
   * @return schema object
   */
  public synchronized Schema getSchema(String fileLocation, String rootFileLocation,
      String schematronLocation) throws SchemaProviderException {

    File downloadedFile = downloadZipIfNeeded(fileLocation, prepareDirectoryName(fileLocation));
    unzipArchiveIfNeeded(downloadedFile, rootFileLocation);
    return prepareSchema(prepareDirectoryName(fileLocation), downloadedFile.getParentFile(),
        rootFileLocation, schematronLocation);
  }

  /**
   * Creates intance of {@link Schema} class based on provided type of schema
   *
   * @param fileLocation location of schema files. Can be url to zip or predefined value (that will be taken from properties file)
   * @return the instance
   */
  public Schema getSchema(String fileLocation) throws SchemaProviderException {
    if (isPredefined(fileLocation)) {
      return getSchema(
          predefinedSchemasLocations.get(fileLocation).getLocation(),
          predefinedSchemasLocations.get(fileLocation).getRootFileLocation(),
          predefinedSchemasLocations.get(fileLocation).getSchematronFileLocation());
    } else {
      throw new SchemaProviderException("XSD root file not provided");
    }
  }

  public String getSchemasDirectory() {
    return schemasDir;
  }

  private String prepareDirectoryName(String name) throws SchemaProviderException {
    URL url = null;
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

    File schemasLocation = new File(schemasDir, destinationDir);

    if (new File(schemasLocation, ZIP_FILE_NAME).exists()) {
      LOGGER.debug("Zip file will not be downloaded, already exists in temp directory");
      return new File(schemasLocation, ZIP_FILE_NAME);
    }

    try {
      FileUtils.deleteDirectory(schemasLocation);
    } catch (IOException e) {
      throw new SchemaProviderException("Unable to clean schemaDirecory", e);
    }

    if(!schemasLocation.mkdirs())
      throw new SchemaProviderException("Unable to create schemaDirecory");

    try (InputStream urlLocation = new URL(zipLocation).openStream();
        ReadableByteChannel rbc = Channels.newChannel(urlLocation);
        FileOutputStream fos = new FileOutputStream(new File(schemasLocation, ZIP_FILE_NAME), false)) {

      File destinationFile = new File(schemasLocation, ZIP_FILE_NAME);
      fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
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

  private Schema prepareSchema(
      String schemaName,
      File unzippedSchemaLocation,
      String rootFileLocation,
      String schematronLocation) throws SchemaProviderException {

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
