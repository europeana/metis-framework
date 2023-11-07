package eu.europeana.metis.harvesting.http;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvesterFactory;
import eu.europeana.metis.harvesting.ReportingIteration;
import eu.europeana.metis.utils.TempFileUtils;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpHarvesterImplTest {

  private static final String ZIP_WITH_SUBDIR_WITH_SPACE_IN_NAME = "zip_with_subdir_with_space_in_name.zip";
  private static final String DIR_NAME_INSIDE_ZIP = "oai.do+-+khk";

  @Test
  void shouldExtractZipWithSpacesInName() throws HarvesterException, IOException {
    //given
    HttpHarvester httpHarvester = HarvesterFactory.createHttpHarvester();
    URL sourceZipFile = HttpHarvesterImplTest.class.getClassLoader().getResource(ZIP_WITH_SUBDIR_WITH_SPACE_IN_NAME);
    Path tempLocation = TempFileUtils.createSecureTempDirectory("temp_extraction");

    //when
    HttpRecordIterator httpRecordIterator = httpHarvester.harvestRecords(sourceZipFile.toString(),
            tempLocation.toString());

    //then
    File destinationFile = new File(tempLocation.toString(), ZIP_WITH_SUBDIR_WITH_SPACE_IN_NAME);
    File destinationExtractedDir = new File(tempLocation.toString(), DIR_NAME_INSIDE_ZIP);

    httpRecordIterator.forEach(path -> {
      assertFalse(path.toString().contains(" "));
      return ReportingIteration.IterationResult.CONTINUE;
    });

    assertTrue(destinationFile.exists());
    assertTrue(destinationExtractedDir.exists());

    FileUtils.deleteDirectory(tempLocation.toFile());
  }
}