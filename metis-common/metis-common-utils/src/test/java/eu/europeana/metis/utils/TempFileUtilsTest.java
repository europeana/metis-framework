package eu.europeana.metis.utils;

import org.apache.commons.io.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.PosixFilePermission;
import java.util.EnumSet;
import java.util.LinkedHashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TempFileUtilsTest {

  public static final boolean IS_POSIX = FileSystems.getDefault().supportedFileAttributeViews().contains("posix");

  @Test
  void createSecureTempFile() throws IOException {
    final Path secureTempFile = TempFileUtils.createSecureTempFile("prefix", "suffix");
    assertFilePermissions(secureTempFile);
    assertTrue(Files.deleteIfExists(secureTempFile));
  }

  @Test
  void createSecureTempFileDeleteOnExit() throws IOException {
    final Path secureTempFile = TempFileUtils.createSecureTempFileDeleteOnExit("prefix", "suffix");
    assertFilePermissions(secureTempFile);
  }

  private LinkedHashSet<String> castList(Object objectList) {
    @SuppressWarnings("rawtypes") final LinkedHashSet linkedHashSet = (LinkedHashSet) objectList;
    LinkedHashSet<String> result = new LinkedHashSet<>();
    for (Object object : linkedHashSet) {
      result.add((String) object);
    }
    return result;
  }

  @Test
  void createSecureTempDirectoryAndFile() throws IOException {
    final Path secureTempDirectoryAndFile = TempFileUtils.createSecureTempDirectoryAndFile("directoryPrefix", "prefix", "suffix");
    final Path parent = secureTempDirectoryAndFile.getParent();
    assertFilePermissions(parent);
    assertFilePermissions(secureTempDirectoryAndFile);
    FileUtils.deleteDirectory(parent.toFile());
  }

  @Test
  void createSecureTempDirectory() throws IOException {
    final Path secureTempDirectory = TempFileUtils.createSecureTempDirectory("directoryPrefix");
    assertFilePermissions(secureTempDirectory);
    FileUtils.deleteDirectory(secureTempDirectory.toFile());
  }

  private void assertFilePermissions(Path secureTempFile) throws IOException {
    if (IS_POSIX) {
      final Set<PosixFilePermission> posixFilePermissions = Files.getPosixFilePermissions(secureTempFile);
      assertTrue(posixFilePermissions.containsAll(
          EnumSet.of(PosixFilePermission.OWNER_READ, PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE)));
      //Check that permissions to others are denied
      assertEquals(3, posixFilePermissions.size());
    }

    assertTrue(Files.isReadable(secureTempFile));
    assertTrue(Files.isWritable(secureTempFile));
    assertTrue(Files.isExecutable(secureTempFile));
  }
}