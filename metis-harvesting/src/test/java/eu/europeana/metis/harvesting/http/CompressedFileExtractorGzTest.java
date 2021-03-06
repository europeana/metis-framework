package eu.europeana.metis.harvesting.http;


import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Test;

public class CompressedFileExtractorGzTest {

  private final static String DESTINATION_DIR = "src/test/resources/__files/";
  private final static int XML_FILES_COUNT = 13;
  private final static String FILE_NAME = "gzFile";
  private final static String FILE_NAME2 = "gzFileWithCompressedGZFiles";
  private final static String FILE_NAME3 = "gzFilesWithMixedCompressedFiles";
  private final static String XML_TYPE = "xml";

  @Test
  public void shouldUnpackTheTarGzFilesRecursively() throws IOException {
    CompressedFileExtractor.extractFile(Path.of(DESTINATION_DIR + FILE_NAME + ".tar.gz"),
            Path.of(DESTINATION_DIR));
    Collection<File> files = getXMLFiles(DESTINATION_DIR + FILE_NAME);
    assertNotNull(files);
    assertEquals(XML_FILES_COUNT, files.size());
  }

  @Test
  public void shouldUnpackTheTarGzFilesRecursivelyWithCompressedXMLFiles() throws IOException {
    CompressedFileExtractor.extractFile(Path.of(DESTINATION_DIR + FILE_NAME2 + ".tar.gz"),
            Path.of(DESTINATION_DIR));
    Collection<File> files = getXMLFiles(DESTINATION_DIR + FILE_NAME2);
    assertNotNull(files);
    assertEquals(XML_FILES_COUNT, files.size());
  }

  @Test
  public void shouldUnpackTheTGZFilesRecursivelyWithCompressedXMLFiles() throws IOException {
    CompressedFileExtractor
            .extractFile(Path.of(DESTINATION_DIR + FILE_NAME2 + ".tgz"), Path.of(DESTINATION_DIR));
    Collection<File> files = getXMLFiles(DESTINATION_DIR + FILE_NAME2);
    assertNotNull(files);
    assertEquals(XML_FILES_COUNT, files.size());
  }

  @Test
  public void shouldUnpackTheTarGzFilesRecursivelyWithMixedNestedCompressedFiles() throws IOException {
    CompressedFileExtractor.extractFile(Path.of(DESTINATION_DIR + FILE_NAME3 + ".tar.gz"),
            Path.of(DESTINATION_DIR));
    Collection<File> files = getXMLFiles(DESTINATION_DIR + FILE_NAME3);
    assertNotNull(files);
    assertEquals(XML_FILES_COUNT, files.size());
  }

  private Collection<File> getXMLFiles(String folderLocation) {
    return FileUtils.listFiles(new File(folderLocation), new String[]{XML_TYPE}, true);
  }

  @AfterAll
  public static void cleanUp() throws IOException {
    FileUtils.forceDelete(new File(DESTINATION_DIR + FILE_NAME));
    FileUtils.forceDelete(new File(DESTINATION_DIR + FILE_NAME2));
    FileUtils.forceDelete(new File(DESTINATION_DIR + FILE_NAME3));
  }
}