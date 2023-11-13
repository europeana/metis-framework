package eu.europeana.metis.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Collection;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

/**
 * Unit tests for {@link CompressedFileHandler}
 */

class CompressedFileHandlerZipTest {

  private final static String DESTINATION_DIR = String.format("src%1$stest%1$sresources%1$s__files%1$s",
      File.separator);
  private final static int XML_FILES_COUNT = 13;
  private final static String FILE_NAME = "zipFileWithNestedZipFiles";
  private final static String FILE_NAME2 = "zipFileWithNestedFolders";
  private final static String FILE_NAME3 = "ZipFilesWithMixedCompressedFiles";
  private final static String FILE_NAME4 = "zipFileWithSubdirContainingSpaceInName";
  private final static String DEFAULT_DESTINATION_NAME = "zipFile";
  private final static String DESTINATION_NAME_FOR_ZIP_WITH_SPACES = "zip_file";
  private final static String XML_TYPE = "xml";
  public static final String FILE_EXTENSION = ".zip";

  @Test
  void shouldUnpackTheZipFilesRecursively() throws IOException {
    CompressedFileHandler.extractFile(Path.of(DESTINATION_DIR + FILE_NAME + FILE_EXTENSION),
        Path.of(DESTINATION_DIR));
    Collection<File> files = getXMLFiles(DESTINATION_DIR + DEFAULT_DESTINATION_NAME);
    assertNotNull(files);
    assertEquals(XML_FILES_COUNT, files.size());
  }

  @Test
  void shouldUnpackTheZipFilesWithNestedFoldersRecursively() throws IOException {
    CompressedFileHandler.extractFile(Path.of(DESTINATION_DIR + FILE_NAME2 + FILE_EXTENSION),
        Path.of(DESTINATION_DIR));
    Collection<File> files = getXMLFiles(DESTINATION_DIR + DEFAULT_DESTINATION_NAME);
    assertNotNull(files);
    assertEquals(XML_FILES_COUNT, files.size());
  }

  @Test
  void shouldUnpackTheZipFilesWithNestedMixedCompressedFiles() throws IOException {
    CompressedFileHandler.extractFile(Path.of(DESTINATION_DIR + FILE_NAME3 + FILE_EXTENSION),
        Path.of(DESTINATION_DIR));
    Collection<File> files = getXMLFiles(DESTINATION_DIR + DEFAULT_DESTINATION_NAME);
    assertNotNull(files);
    assertEquals(XML_FILES_COUNT, files.size());
  }

  @Test
  void shouldExtractZipWithSpacesInName() throws IOException {
    CompressedFileHandler.extractFile(Path.of(DESTINATION_DIR + FILE_NAME4 + FILE_EXTENSION),
        Path.of(DESTINATION_DIR));

    Collection<File> files = getXMLFiles(DESTINATION_DIR + DESTINATION_NAME_FOR_ZIP_WITH_SPACES);
    assertNotNull(files);
    assertEquals(10, files.size());

    files.forEach(file -> assertEquals(
        -1,
        StringUtils.indexOfAny(file.getName(), CompressedFileHandler.FILE_NAME_BANNED_CHARACTERS)));
  }

  private Collection<File> getXMLFiles(String folderLocation) {
    return FileUtils.listFiles(new File(folderLocation), new String[]{XML_TYPE}, true);
  }

  @AfterEach
  public void cleanUp() throws IOException {
    if (new File(DESTINATION_DIR + DEFAULT_DESTINATION_NAME).exists()) {
      FileUtils.forceDelete(new File(DESTINATION_DIR + DEFAULT_DESTINATION_NAME));
    }
    if (new File(DESTINATION_DIR + DESTINATION_NAME_FOR_ZIP_WITH_SPACES).exists()) {
      FileUtils.forceDelete(new File(DESTINATION_DIR + "zip_file"));
    }
  }
}
