package eu.europeana.metis.data.checker.service;

import eu.europeana.metis.data.checker.common.exception.ZipFileException;
import eu.europeana.metis.utils.ZipFileReader;
import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by erikkonijnenburg on 27/07/2017.
 */
@Service
public class ZipService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZipService.class);

  public List<String> readFileToStringList(MultipartFile providedZipFile) throws ZipFileException {
    final List<String> result;
    try {
      result = new ZipFileReader().getRecordsFromZipFile(providedZipFile.getInputStream());
    } catch (IOException ex) {
      LOGGER.error("Error reading from zipfile. ", ex);
      throw new ZipFileException("Error reading from zipfile.", ex);
    }
    if (result.isEmpty()) {
      throw new ZipFileException(
          "Error reading from zipfile: zipfile contains no suitable records.");
    }
    return result;
  }
}
