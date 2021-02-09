package eu.europeana.metis.harvesting.http;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import eu.europeana.metis.harvesting.HarvesterException;
import eu.europeana.metis.harvesting.HarvesterFactory;
import java.io.File;
import java.io.IOException;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Test;

public class CompressedFileExtractorZipTest {

  private final static String DESTINATION_DIR = "src/test/resources/__files/";
  private final static int XML_FILES_COUNT = 13;
  private final static String FILE_NAME = "zipFileWithNestedZipFiles";
  private final static String FILE_NAME2 = "zipFileWithNestedFolders";
  private final static String FILE_NAME3 = "ZipFilesWithMixedCompressedFiles";
  private final static String DEFAULT_DESTINATION_NAME = "zipFile";
  private final static String XML_TYPE = "xml";
  public static final String ZIP_EXTENSION = ".zip";

  @Test
  public void shouldUnpackTheZipFilesRecursively() throws IOException, HarvesterException {
    CompressedFileExtractor
            .extractFile(DESTINATION_DIR + FILE_NAME + ZIP_EXTENSION, DESTINATION_DIR);
    Collection<File> files = getXMLFiles(DESTINATION_DIR + DEFAULT_DESTINATION_NAME);
    assertNotNull(files);
    assertEquals(XML_FILES_COUNT, files.size());
  }

  @Test
  public void shouldUnpackTheZipFilesWithNestedFoldersRecursively()
          throws IOException, HarvesterException {
    CompressedFileExtractor
            .extractFile(DESTINATION_DIR + FILE_NAME2 + ZIP_EXTENSION, DESTINATION_DIR);
    Collection<File> files = getXMLFiles(DESTINATION_DIR + DEFAULT_DESTINATION_NAME);
    assertNotNull(files);
    assertEquals(XML_FILES_COUNT, files.size());
  }

  @Test
  public void shouldUnpackTheZipFilesWithNestedMixedCompressedFiles()
          throws IOException, HarvesterException {
    CompressedFileExtractor
            .extractFile(DESTINATION_DIR + FILE_NAME3 + ZIP_EXTENSION, DESTINATION_DIR);
    Collection<File> files = getXMLFiles(DESTINATION_DIR + DEFAULT_DESTINATION_NAME);
    assertNotNull(files);
    assertEquals(XML_FILES_COUNT, files.size());
  }

  private Collection<File> getXMLFiles(String folderLocation) {
    return FileUtils.listFiles(new File(folderLocation), new String[]{XML_TYPE}, true);
  }

  @After
  public void cleanUp() throws IOException {
    FileUtils.forceDelete(new File(DESTINATION_DIR + DEFAULT_DESTINATION_NAME));
  }
}