package eu.europeana.metis.preview.service;

import java.io.IOException;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import eu.europeana.metis.preview.common.exception.ZipFileException;
import eu.europeana.metis.utils.ZipFileUtils;
import net.lingala.zip4j.exception.ZipException;

/**
 * Created by erikkonijnenburg on 27/07/2017.
 */
@Service
public class ZipService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZipService.class);

  public List<String> readFileToStringList(MultipartFile providedZipFile) throws ZipFileException {
    try {
      return ZipFileUtils.getRecordsFromZipFile(providedZipFile);
    } catch (IOException | ZipException ex) {
      LOGGER.error("Error reading from zipfile. ", ex);
      throw new ZipFileException("Error reading from zipfile.", ex);
    }
  }
}
