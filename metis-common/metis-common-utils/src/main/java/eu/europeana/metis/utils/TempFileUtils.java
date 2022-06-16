package eu.europeana.metis.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.attribute.FileAttribute;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.EnumSet;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File utilities class
 */
public final class TempFileUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(TempFileUtils.class);
  private static final EnumSet<PosixFilePermission> OWNER_PERMISSIONS_ONLY_SET = EnumSet.of(PosixFilePermission.OWNER_READ,
      PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE);
  private static final FileAttribute<Set<PosixFilePermission>> OWNER_PERMISSIONS_ONLY_FILE_ATTRIBUTE = PosixFilePermissions.asFileAttribute(
      OWNER_PERMISSIONS_ONLY_SET);
  public static final String PNG_FILE_EXTENSION = ".png";
  public static final String JPEG_FILE_EXTENSION = ".jpeg";

  private TempFileUtils() {
    //Private constructor
  }

  /**
   * Creates a secure temporary file for posix and other file systems.
   * <p>This method is not responsible of removing the temporary file.
   * An implementation that uses this method should delete the temp files by itself.</p>
   *
   * @param prefix the prefix, (e.g. the class simple name that generates the temp file)
   * @param suffix the suffix
   * @return the secure temporary file
   * @throws IOException if the file failed to be created
   */
  public static File createSecureTempFile(String prefix, String suffix) throws IOException {
    //Set permissions only to owner, posix style
    final File file = Files.createTempFile(prefix, suffix, OWNER_PERMISSIONS_ONLY_FILE_ATTRIBUTE).toFile();
    //Set again for non posix systems
    setManualFilePermissions(file);

    return file;
  }

  /**
   * Creates a secure temporary file for posix and other file systems.
   * <p>
   * This is equivalent to calling {@link #createSecureTempFile(String, String)} and in addition it declares that it will remove
   * the temporary file with {@link File#deleteOnExit()}.
   * </p>
   *
   * <p>CAUTION: This method can have a memory impact if too many files are created, and that is because
   * {@link File#deleteOnExit()} keeps an in memory cache of the file paths. If possible prefer the use of
   * {@link #createSecureTempFile(String, String)} and make your implementation remove the temporary files created
   * explicitly.</p>
   *
   * @param prefix the prefix
   * @param suffix the suffix
   * @return the secure temporary file
   * @throws IOException if the file failed to be created
   */
  @SuppressWarnings("java:S2308") //Delete on exit is intended here and javadoc warns the user
  public static File createSecureTempFileDeleteOnExit(String prefix, String suffix) throws IOException {
    final File secureTempFile = createSecureTempFile(prefix, suffix);
    secureTempFile.deleteOnExit();
    return secureTempFile;
  }

  /**
   * Creates a secure temporary directory with the {@code directoryPrefix} specified and then creates a temporary file inside that
   * directory with the {@code prefix} and {@code suffix} specified.
   *
   * @param directoryPrefix the directory prefix
   * @param prefix the file prefix
   * @param suffix the file suffix
   * @return the secure temporary file in the newly created secure temporary directory
   * @throws IOException if the directory or file failed to be created
   */
  public static Path createSecureTempDirectoryAndFile(String directoryPrefix, String prefix, String suffix) throws IOException {
    Path tempParentDir = createSecureTempDirectory(directoryPrefix);
    //Set permissions only to owner, posix style
    final File file = Files.createTempFile(tempParentDir, prefix, suffix, OWNER_PERMISSIONS_ONLY_FILE_ATTRIBUTE).toFile();
    //Set again for non posix systems
    setManualFilePermissions(file);

    return file.toPath();
  }

  /**
   * Creates a secure temporary directory with the {@code prefix} specified.
   *
   * @param prefix the prefix
   * @return the secure temporary directory
   * @throws IOException if the directory failed to be created
   */
  public static Path createSecureTempDirectory(String prefix) throws IOException {
    //Set permissions only to owner, posix style
    final File file = Files.createTempDirectory(prefix, OWNER_PERMISSIONS_ONLY_FILE_ATTRIBUTE).toFile();
    //Set again for non posix systems
    setManualFilePermissions(file);

    return file.toPath();
  }

  private static void setManualFilePermissions(File file) {
    //Set again for non posix systems
    if (!(file.setReadable(true, true) && file.setWritable(true, true) && file.setExecutable(true, true))) {
      LOGGER.debug("Setting permissions failed on file {}", file.getAbsolutePath());
    }
  }

}
