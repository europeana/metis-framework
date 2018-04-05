package eu.europeana.metis.preview.service;

import eu.europeana.metis.preview.common.exception.ZipFileException;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
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
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by erikkonijnenburg on 27/07/2017.
 */
@Service
public class ZipService {

  private static final Logger LOGGER = LoggerFactory.getLogger(ZipService.class);

  public List<String> readFileToStringList(MultipartFile file) throws ZipFileException {

    List<String> records = new ArrayList<>();
    
    try {
    	String fileName = "/tmp/" + file.getName() + "/" + new Date().getTime();
        FileUtils.copyInputStreamToFile(file.getInputStream(), new File(fileName + ".zip"));
        LOGGER.info("Temp file: {} created.", fileName + ".zip");

        ZipFile zipFile = new ZipFile(fileName + ".zip");
        zipFile.extractAll(fileName);
        LOGGER.info("Unzipped contents into: {}", fileName);
        
        FileUtils.deleteQuietly(new File(fileName + ".zip"));
        File[] files = new File(fileName).listFiles();
        
        for (File input : files) {
            if(!input.isDirectory()){
                FileInputStream stream = new FileInputStream(input);
                records.add(IOUtils.toString(stream));
                stream.close();
            }
        }
    } catch (IOException | ZipException ex) {
      LOGGER.error("Error reading from zipfile. ", ex);
      throw new ZipFileException("Error reading from zipfile. Details: " + ex.getMessage());
    }
    
    return records;
  }
}
