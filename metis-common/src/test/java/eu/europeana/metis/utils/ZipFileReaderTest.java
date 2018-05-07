package eu.europeana.metis.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class ZipFileReaderTest {

  @Test
  public void testGetRecordsFromEmptyZipFile() throws IOException {

    // Call reader for empty zipfile
    final ZipFile emptyZipFile = mock(ZipFile.class);
    doReturn(Stream.empty()).when(emptyZipFile).stream();
    assertTrue(new ZipFileReader().getRecordsFromZipFile(emptyZipFile).isEmpty());

    // Create zipfile with content and reader that rejects everything
    final List<ZipEntry> entries =
        Stream.of("A", "B", "C").map(name -> createEntry(name, false)).collect(Collectors.toList());
    final ZipFileReader reader = spy(new ZipFileReader());
    final ZipFile zipFileWithBadContent = mock(ZipFile.class);
    doReturn(entries.stream()).when(zipFileWithBadContent).stream();
    doReturn(false).when(reader).accept(any());
    assertTrue(reader.getRecordsFromZipFile(zipFileWithBadContent).isEmpty());
  }

  @Test
  public void testGetRecordsFromZipFile() throws IOException {

    // We have good entries (regular entries) and bad entries (directories).
    final List<ZipEntry> entries = new ArrayList<>();
    final List<String> goodEntries = Arrays.asList("A", "B", "C");
    entries.addAll(
        goodEntries.stream().map(name -> createEntry(name, false)).collect(Collectors.toList()));
    final List<String> badEntries = Arrays.asList("X", "Y", "Z");
    entries.addAll(
        badEntries.stream().map(name -> createEntry(name, true)).collect(Collectors.toList()));

    // The content is equal to the name.
    final ZipFile zipFile = mock(ZipFile.class);
    doReturn(entries.stream()).when(zipFile).stream();
    doAnswer(invocation -> IOUtils.toInputStream(((ZipEntry) invocation.getArgument(0)).getName()))
        .when(zipFile).getInputStream(any());

    // Create reader that knows the difference between good and bad.
    final ZipFileReader reader = spy(new ZipFileReader());
    doAnswer(invocation -> goodEntries.contains(((ZipEntry) invocation.getArgument(0)).getName()))
        .when(reader).accept(any());

    // The result should therefore be equal to the list of good entries.
    final List<String> result = reader.getRecordsFromZipFile(zipFile);
    assertEquals(goodEntries, result);
  }

  @Test
  public void testAccept() {
    // Test against directories and special Mac files. Entry examples taken from actual zip file.
    ZipFileReader reader = new ZipFileReader();
    assertFalse(reader.accept(createEntry("Internal_valid/", true)));
    assertFalse(reader.accept(createEntry("Internal_valid/.DS_Store", false)));
    assertFalse(reader.accept(createEntry("__MACOSX/", true)));
    assertFalse(reader.accept(createEntry("__MACOSX/Internal_valid/", true)));
    assertFalse(reader.accept(createEntry("__MACOSX/Internal_valid/._.DS_Store", false)));
    assertTrue(reader.accept(createEntry("Internal_valid/Item_445790357.xml", false)));
    assertFalse(reader.accept(createEntry("__MACOSX/Internal_valid/._Item_445790357.xml", false)));
  }

  private ZipEntry createEntry(String name, boolean isDirectory) {
    final ZipEntry result = mock(ZipEntry.class);
    doReturn(name).when(result).getName();
    doReturn(isDirectory).when(result).isDirectory();
    return result;
  }
}
