package eu.europeana.metis.preview.service;

import static org.junit.Assert.*;

import eu.europeana.metis.preview.common.exception.ZipFileException;
import java.util.List;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

/**
 * Created by erikkonijnenburg on 27/07/2017.
 */
public class ZipServiceTest {

  @Test
  public void readFileToStringList_withOkzipfile_returnsListOfStrings() throws Exception {
      ZipService service = new ZipService();
      Resource resource = new ClassPathResource("ValidExternalOk.zip");
      List<String> strings = (service.readFileToStringList(resource.getInputStream()));
      assertNotNull(strings);
      assertEquals(1, strings.size());
  }

  @Test(expected = ZipFileException.class)
  public void readFileToStringList_withEmptyzipfile_throwsException() throws Exception {
    ZipService service = new ZipService();
    Resource resource = new ClassPathResource("empty.zip");
    List<String> strings = (service.readFileToStringList(resource.getInputStream()));
  }

  @Test(expected = ZipFileException.class)
  public void readFileToStringList_withBrokenZipFile_throwsException() throws Exception {
    ZipService service = new ZipService();
    Resource resource = new ClassPathResource("broken.zip");
    List<String> strings = (service.readFileToStringList(resource.getInputStream()));
  }


}