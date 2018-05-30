package eu.europeana.metis.data.checker.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import eu.europeana.metis.data.checker.common.exception.ZipFileException;
import java.util.List;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;

/**
 * Created by erikkonijnenburg on 27/07/2017.
 */
public class ZipServiceTest {

  @Test
  public void readFileToStringList_withOkzipfile_returnsListOfStrings() throws Exception {
      ZipService service = new ZipService();
      Resource resource = new ClassPathResource("ValidExternalOk.zip");            
      MultipartFile file = new MockMultipartFile("ValidExternalOk.zip", resource.getInputStream());
      List<String> strings = service.readFileToStringList(file);
      assertNotNull(strings);
      assertEquals(1, strings.size());
  }

  @Test(expected = ZipFileException.class)
  public void readFileToStringList_withEmptyzipfile_throwsException() throws Exception {
    ZipService service = new ZipService();
    Resource resource = new ClassPathResource("empty.zip");    
    MultipartFile file = new MockMultipartFile("empty.zip", resource.getInputStream());
    service.readFileToStringList(file);
  }

  @Test(expected = ZipFileException.class)
  public void readFileToStringList_withBrokenZipFile_throwsException() throws Exception {
    ZipService service = new ZipService();
    Resource resource = new ClassPathResource("broken.zip");    
    MultipartFile file = new MockMultipartFile("broken.zip", resource.getInputStream());
    service.readFileToStringList(file);
  }
}