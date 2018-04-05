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
    	String prefix = String.valueOf(new Date().getTime());
    	File tempFile = File.createTempFile(prefix, ".zip");
    	FileUtils.copyInputStreamToFile(file.getInputStream(), tempFile);
    	LOGGER.info("Temp file: {} created.", tempFile);

    	ZipFile zipFile = new ZipFile(tempFile);
    	File unzippedDirectory = new File(tempFile.getParent(), prefix + "-unzipped");
    	zipFile.extractAll(unzippedDirectory.getAbsolutePath());
    	LOGGER.info("Unzipped contents into: {}", unzippedDirectory);        

    	FileUtils.deleteQuietly(tempFile);
    	File[] files = unzippedDirectory.listFiles();

    	for (File input : files) {
    		if(!input.isDirectory()) {
    			FileInputStream stream = new FileInputStream(input);
    			records.add(IOUtils.toString(stream));
    			stream.close();
    		}
    	}

    	FileUtils.deleteQuietly(unzippedDirectory);
    } catch (IOException | ZipException ex) {
    	LOGGER.error("Error reading from zipfile. ", ex);
    	throw new ZipFileException("Error reading from zipfile. Details: " + ex.getMessage());
    }
    
    return records;
  }
}
