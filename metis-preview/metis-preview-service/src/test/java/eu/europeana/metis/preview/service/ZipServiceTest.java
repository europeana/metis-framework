package eu.europeana.metis.preview.service;

import static org.junit.Assert.*;

import eu.europeana.metis.preview.common.exception.ZipFileException;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.List;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.commons.CommonsMultipartFile;

import com.google.common.io.Files;

/**
 * Created by erikkonijnenburg on 27/07/2017.
 */
public class ZipServiceTest {

  @Test
  public void readFileToStringList_withOkzipfile_returnsListOfStrings() throws Exception {
      ZipService service = new ZipService();
      
      File file = new File("src/test/resources/ValidExternalOk.zip");
      InputStream inputStream = new FileInputStream(file);
      
      
      MultipartFile resource = new MockMultipartFile("ValidExternalOk.zip", inputStream);
      List<String> strings = service.readFileToStringList(resource);
      assertNotNull(strings);
      assertEquals(1, strings.size());
  }

  @Test(expected = ZipFileException.class)
  public void readFileToStringList_withEmptyzipfile_throwsException() throws Exception {
    ZipService service = new ZipService();
    
    File file = new File("src/test/resources/empty.zip");
    InputStream inputStream = new FileInputStream(file);
    
    MultipartFile resource = new MockMultipartFile("empty.zip", inputStream);
    List<String> strings = service.readFileToStringList(resource);
  }

  @Test(expected = ZipFileException.class)
  public void readFileToStringList_withBrokenZipFile_throwsException() throws Exception {
    ZipService service = new ZipService();
    
    File file = new File("src/test/resources/broken.zip");
    InputStream inputStream = new FileInputStream(file);
    
    MultipartFile resource = new MockMultipartFile("broken.zip", inputStream);
    List<String> strings = (service.readFileToStringList(resource));
  }


}