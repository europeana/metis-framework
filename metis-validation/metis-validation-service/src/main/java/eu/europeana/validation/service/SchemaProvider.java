package eu.europeana.validation.service;

import eu.europeana.validation.model.Schema;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.channels.Channels;
import java.nio.channels.ReadableByteChannel;
import java.util.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Provides schemas based on url of predefined name (EDM-Internal on EDM-External)
 * <p>
 * <p>
 * Created by pwozniak on 12/20/17
 * <p>
 */
public class SchemaProvider {

    private static final Logger LOGGER = LoggerFactory.getLogger(SchemaProvider.class);

    public static final String TMP_DIR = System.getProperty("java.io.tmpdir");
    public static String SCHEMAS_DIR;

    private final Map<String, String> predefinedSchemasLocations;
    private static final String STARTING_SCHEMATRON_FILE_NAME = File.separator + "schematron-internal.xsl";
    private static final String XSD_ENTRY_FILE_NAME = File.separator + "MAIN.xsd";

    public SchemaProvider(Map<String, String> predefinedSchemasLocations) {
        if (TMP_DIR.endsWith(File.separator)) {
            SCHEMAS_DIR = TMP_DIR + "schemas" + File.separator;
        } else {
            SCHEMAS_DIR = TMP_DIR + File.separator + "schemas" + File.separator;
        }

        LOGGER.info("Creating schema manager");
        LOGGER.info("Files will be stored in: {}", SCHEMAS_DIR);
        this.predefinedSchemasLocations = predefinedSchemasLocations;
    }

    /**
     * Retrieves schema object from given (remote) location.
     *
     * @param fileLocation     place where (remote) zip file is located.
     *                         Accepts url to file or one of the predefined values (EDM-INTERNAL or EDM-EXTERNAL)
     * @param rootFileLocation indicates where root xsd file is located inside zip.
     *                         The caller is responsible to provide propper path (for example propper slashes)
     * @return schema object
     * @throws SchemaProviderException
     */
    public Schema getSchema(String fileLocation, String rootFileLocation) throws SchemaProviderException {
        if (isPredefined(fileLocation)) {
            File downloadedFile = downloadZipIfNeeded(predefinedSchemasLocations.get(fileLocation.toLowerCase()), fileLocation.toLowerCase());
            unzipArchive(downloadedFile);
            return prepareSchema(fileLocation, downloadedFile.getParentFile(), rootFileLocation);
        } else {
            File downloadedFile = downloadZipIfNeeded(fileLocation, prepareDirectoryName(fileLocation));
            unzipArchive(downloadedFile);
            return prepareSchema(prepareDirectoryName(fileLocation), downloadedFile.getParentFile(), rootFileLocation);
        }
    }

    private String prepareDirectoryName(String name) throws SchemaProviderException {
        URL url = null;
        try {
            url = new URL(name);
            String host = url.getHost();
            String file = url.getFile();
            return host + "_" + StringUtils.substringAfter(StringUtils.substringBeforeLast(file, "."), "/");
        } catch (MalformedURLException e) {
            throw new SchemaProviderException(e);
        }
    }

    private boolean isPredefined(String name) {
        return predefinedSchemasLocations.containsKey(name.toLowerCase());
    }

    /**
     * Downloads zip file to specified directory
     *
     * @param zipLocation    location of the file to be downloaded
     * @param destinationDir place where downloaded file will be saved
     * @return newly created file
     */
    private File downloadZipIfNeeded(String zipLocation, String destinationDir) throws SchemaProviderException {

        ReadableByteChannel rbc = null;
        FileOutputStream fos = null;
        try {
            File schemasLocation = new File(SCHEMAS_DIR, destinationDir);
            if (new File(schemasLocation, "zip.zip").exists()) {
                LOGGER.info("Zip file will not be downloaded, already exists in temp directory");
                return new File(schemasLocation, "zip.zip");
            }
            URL url = new URL(zipLocation);
            rbc = Channels.newChannel(url.openStream());
            FileUtils.deleteDirectory(schemasLocation);
            schemasLocation.mkdirs();
            File destinationFile = new File(schemasLocation, "zip.zip");
            fos = new FileOutputStream(destinationFile);
            fos.getChannel().transferFrom(rbc, 0, Long.MAX_VALUE);
            return destinationFile;
        } catch (IOException e) {
            throw new SchemaProviderException("Unable to store schema file", e);
        } finally {
            try {
                if (rbc != null) {
                    rbc.close();
                }
                if (fos != null) {
                    fos.close();
                }
            } catch (IOException ignored) {
            }
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

    private Schema prepareSchema(String schemaName, File unzippedSchemaLocation, String rootFileLocation) throws SchemaProviderException {

        Schema schema = new Schema();
        schema.setName(schemaName);
        if (rootFileExists(unzippedSchemaLocation, rootFileLocation)) {
            schema.setPath(unzippedSchemaLocation.getAbsolutePath() + File.separator + rootFileLocation);
            return schema;
        } else {
            throw new SchemaProviderException("Provided root xsd file name does not exist in archive");
        }
    }

    private boolean rootFileExists(File unzippedSchemaLocation, String rootFileLocation) {
        File rootFile = new File(unzippedSchemaLocation, rootFileLocation);
        if (rootFile.exists()) {
            return true;
        } else {
            return false;
        }
    }
}