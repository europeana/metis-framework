package eu.europeana.metis.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.File;
import java.io.InputStream;
import java.util.Enumeration;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.junit.jupiter.api.Test;

public class CompressedSecureFileTest {

  private final static String DESTINATION_DIR = String.format("src%1$stest%1$sresources%1$s__files%1$s", File.separator);
  private final static int XML_FILES_COUNT = 13;
  private final static String FILE_NAME = "zipFileWithNestedZipFiles";
  private final static String FILE_NAME2 = "zipFileWithNestedFolders";
  private final static String FILE_NAME3 = "ZipFilesWithMixedCompressedFiles";
  private final static String DEFAULT_DESTINATION_NAME = "zipFile";
  private final static String XML_TYPE = "xml";
  public static final String FILE_EXTENSION = ".zip";

//  @Test
  void testThresholdInputStream() throws Exception {
    // This fails in Java 10 because our reflection injection of the ThresholdInputStream causes a
    // ClassCastException in ZipFile now
    // The relevant change in the JDK is http://hg.openjdk.java.net/jdk/jdk10/rev/85ea7e83af30#l5.66


    try (ZipFile thresholdInputStream = new ZipFile(DESTINATION_DIR + FILE_NAME + FILE_EXTENSION)) {
      try (CompressedSecureFile secureFile = new CompressedSecureFile(DESTINATION_DIR + FILE_NAME + FILE_EXTENSION)) {
        Enumeration<? extends ZipArchiveEntry> entries = thresholdInputStream.getEntries();
        while (entries.hasMoreElements()) {
          ZipArchiveEntry entry = entries.nextElement();
          try (InputStream inputStream = secureFile.getInputStream(entry)) {
            assertTrue(IOUtils.toByteArray(inputStream).length > 0);
          }
        }
      }
    }
  }

  @Test
  void testSettingMaxEntrySizeAsNegative() {
    assertThrows(IllegalArgumentException.class, () -> CompressedSecureFile.setMaxEntrySize(-1));
  }

  @Test
  void testSettingMaxEntrySizeAs8Gb() {
    long approx8Gb = CompressedSecureFile.MAX_ENTRY_SIZE * 2;
    try {
      CompressedSecureFile.setMaxEntrySize(approx8Gb);
      assertEquals(approx8Gb, CompressedSecureFile.getMaxEntrySize());
    } finally {
      CompressedSecureFile.setMaxEntrySize(CompressedSecureFile.MAX_ENTRY_SIZE);
    }
  }

  @Test
  void testSettingMaxTextSizeAsNegative() {
    assertThrows(IllegalArgumentException.class, () -> CompressedSecureFile.setMaxTextSize(-1));
  }

  @Test
  void testSettingMaxTextSizeAs8GChars() {
    long approx8G = CompressedSecureFile.MAX_ENTRY_SIZE * 2;
    try {
      CompressedSecureFile.setMaxTextSize(approx8G);
      assertEquals(approx8G, CompressedSecureFile.getMaxTextSize());
    } finally {
      CompressedSecureFile.setMaxTextSize(CompressedSecureFile.DEFAULT_MAX_TEXT_SIZE);
    }
  }

}
