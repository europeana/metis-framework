package eu.europeana.metis.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class ZipFileUtilsTest {

  @Test
  public void testGetRecordsFromZipFile() throws IOException {

    // We have good entries (regular entries) and bad entries (directories).
    final List<ZipEntry> entries = new ArrayList<>();
    final String[] goodEntries = {"A", "B", "C"};
    entries.addAll(
        Stream.of(goodEntries).map(name -> createEntry(name, false)).collect(Collectors.toList()));
    final String[] badEntries = {"X", "Y", "Z"};
    entries.addAll(
        Stream.of(badEntries).map(name -> createEntry(name, true)).collect(Collectors.toList()));
    Collections.shuffle(entries);

    // The content is equal to the name.
    final ZipFile zipFile = mock(ZipFile.class);
    doReturn(entries.stream()).when(zipFile).stream();
    doAnswer(invocation -> IOUtils.toInputStream(((ZipEntry) invocation.getArgument(0)).getName()))
        .when(zipFile).getInputStream(any());

    // The result should therefore be equal to the list of good entries.
    final List<String> result = ZipFileUtils.getRecordsFromZipFile(zipFile);
    assertArrayEquals(goodEntries, result.stream().sorted().toArray());
  }

  @Test
  public void testAccept() {
    // Test against directories and special Mac files. Entry examples taken from actual zip file.
    assertFalse(ZipFileUtils.accept(createEntry("Internal_valid/", true)));
    assertFalse(ZipFileUtils.accept(createEntry("Internal_valid/.DS_Store", false)));
    assertFalse(ZipFileUtils.accept(createEntry("__MACOSX/", true)));
    assertFalse(ZipFileUtils.accept(createEntry("__MACOSX/Internal_valid/", true)));
    assertFalse(ZipFileUtils.accept(createEntry("__MACOSX/Internal_valid/._.DS_Store", false)));
    assertTrue(ZipFileUtils.accept(createEntry("Internal_valid/Item_445790357.xml", false)));
    assertFalse(
        ZipFileUtils.accept(createEntry("__MACOSX/Internal_valid/._Item_445790357.xml", false)));
  }

  private ZipEntry createEntry(String name, boolean isDirectory) {
    final ZipEntry result = mock(ZipEntry.class);
    doReturn(name).when(result).getName();
    doReturn(isDirectory).when(result).isDirectory();
    return result;
  }
}
