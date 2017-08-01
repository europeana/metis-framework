package eu.europeana.metis.preview.service;

import eu.europeana.metis.preview.common.exception.ZipFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import net.lingala.zip4j.core.ZipFile;
import net.lingala.zip4j.exception.ZipException;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

/**
 * Created by erikkonijnenburg on 27/07/2017.
 */
@Service
public class ZipService {

  private final Logger LOGGER = LoggerFactory.getLogger(ZipService.class);

  public List<String> readFileToStringList(InputStream inputStream) throws ZipFileException {

    //TODO use a different zip library based on streams so we don't have to create files
    //and delete them afterward
    List<String> xmls = new ArrayList<>();
    try {
      String prefix = String.valueOf(new Date().getTime());
      File tempFile = File.createTempFile(prefix, ".zip");
      FileUtils.copyInputStreamToFile(inputStream, tempFile);
      LOGGER.info("Temp file: " + tempFile + " created.");

      ZipFile zipFile = new ZipFile(tempFile);
      File unzippedDirectory = new File(tempFile.getParent(), prefix + "-unzipped");
      zipFile.extractAll(unzippedDirectory.getAbsolutePath());
      LOGGER.info("Unzipped contents into: " + unzippedDirectory);
      FileUtils.deleteQuietly(tempFile);
      File[] files = unzippedDirectory.listFiles();

      for (File input : files) {
        xmls.add(IOUtils.toString(new FileInputStream(input)));
      }
      FileUtils.deleteQuietly(unzippedDirectory);
    }
    catch (IOException ex) {
      LOGGER.error("Error reading from zipfile. ", ex);
      throw new ZipFileException("Error reading from zipfile. Details: "+ ex.getMessage());
    }
    catch (ZipException ex) {
      LOGGER.error("Error unzipping file. ", ex);
      throw new ZipFileException("Error unzipping file. Details: " + ex.getMessage());
    }
    return xmls;
  }
}
