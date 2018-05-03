package eu.europeana.metis.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;

/**
 * This class provides utility methods for managing zip files.
 * 
 * @author jochen
 *
 */
public final class ZipFileUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZipFileUtils.class);
  private static final String UNZIPPED_SUFFIX = "-unzipped";

  private ZipFileUtils() {}

  /**
   * This method extracts all files from a ZIP file and returns them as strings. This method only
   * considers files in the main directory. This method creates (and then removes) temporary files.
   * 
   * @param providedZipFile The zip file.
   * @return A list of records.
   * @throws IOException In case of problems managing the temporary files.
   * @throws ZipException In case of problems with the extraction of the zip file.
   */
  public static List<String> getRecordsFromZipFile(MultipartFile providedZipFile)
      throws IOException, ZipException {

    String prefix = String.valueOf(new Date().getTime());
    File tempFile = File.createTempFile(prefix, ".zip");
    FileUtils.copyInputStreamToFile(providedZipFile.getInputStream(), tempFile);
    LOGGER.info("Temp file: {} created.", tempFile);

    ZipFile zipFile = new ZipFile(tempFile);
    File unzippedDirectory = new File(tempFile.getParent(), prefix + UNZIPPED_SUFFIX);
    zipFile.extractAll(unzippedDirectory.getAbsolutePath());
    LOGGER.info("Unzipped contents into: {}", unzippedDirectory);

    FileUtils.deleteQuietly(tempFile);
    File[] files = unzippedDirectory.listFiles();
    if (files == null) {
      throw new IOException("Zipped directory returned null files");
    }

    final List<String> records = new ArrayList<>();
    for (File input : files) {
      if (!input.isDirectory()) {
        InputStream stream = Files.newInputStream(input.toPath());
        records.add(IOUtils.toString(stream, "UTF-8"));
        stream.close();
      }
    }

    FileUtils.deleteQuietly(unzippedDirectory);

    return records;
  }
}
