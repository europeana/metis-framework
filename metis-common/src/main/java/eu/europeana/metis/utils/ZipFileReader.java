package eu.europeana.metis.utils;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class provides the functionality of reading zip files.
 * 
 * @author jochen
 *
 */
public class ZipFileReader {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZipFileReader.class);

  private static final String MAC_TEMP_FOLDER = "__MACOSX";
  private static final String MAC_TEMP_FILE = ".DS_Store";

  /**
   * Constructor.
   */
  public ZipFileReader() {
    // Nothing to do.
  }

  /**
   * This method extracts all files from a ZIP file and returns them as strings. This method only
   * considers files in the main directory. This method creates (and then removes) a temporary file.
   * 
   * @param providedZipFile Input stream containing the zip file. This method is not responsible for
   *        closing the stream.
   * @return A list of records.
   * @throws IOException In case of problems with the temporary file or with reading the zip file.
   */
  public List<String> getRecordsFromZipFile(InputStream providedZipFile) throws IOException {

    // Create temporary file.
    final String prefix = UUID.randomUUID().toString();
    final File tempFile = File.createTempFile(prefix, ".zip");
    FileUtils.copyInputStreamToFile(providedZipFile, tempFile);
    LOGGER.info("Temp file: {} created.", tempFile);

    // Open temporary zip file, read it and delete it.
    try (final ZipFile zipFile = new ZipFile(tempFile, ZipFile.OPEN_READ | ZipFile.OPEN_DELETE)) {
      return getRecordsFromZipFile(zipFile);
    }
  }

  List<String> getRecordsFromZipFile(ZipFile zipFile) throws IOException {
    final List<String> result = new ArrayList<>();
    final Iterator<? extends ZipEntry> entries = zipFile.stream().iterator();
    while (entries.hasNext()) {
      final ZipEntry zipEntry = entries.next();
      if (!accept(zipEntry)) {
        continue;
      }
      result.add(IOUtils.toString(zipFile.getInputStream(zipEntry), StandardCharsets.UTF_8.name()));
    }
    return result;
  }

  boolean accept(ZipEntry zipEntry) {
    return !zipEntry.isDirectory() && !zipEntry.getName().startsWith(MAC_TEMP_FOLDER)
        && !zipEntry.getName().endsWith(MAC_TEMP_FILE);
  }
}
