package eu.europeana.metis.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
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
public final class FileUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);
  private static final EnumSet<PosixFilePermission> OWNER_PERMISSIONS_ONLY_SET = EnumSet.of(PosixFilePermission.OWNER_READ,
      PosixFilePermission.OWNER_WRITE, PosixFilePermission.OWNER_EXECUTE);
  private static final FileAttribute<Set<PosixFilePermission>> OWNER_PERMISSIONS_ONLY_FILE_ATTRIBUTE = PosixFilePermissions.asFileAttribute(
      OWNER_PERMISSIONS_ONLY_SET);
  public static final String PNG_FILE_EXTENSION = ".png";
  public static final String JPEG_FILE_EXTENSION = ".jpeg";

  private FileUtils() {
    //Private constructor
  }

  /**
   * Create a secure temp file for posix and other file systems.
   * <p>This method is not responsible of removing the temp file.
   * An implementation that uses this method should delete the temp files by itself.</p>
   *
   * @param prefix the prefix
   * @param suffix the suffix
   * @return the secure temp file
   * @throws IOException if the file failed to be created
   */
  public static File createSecureTempFile(String prefix, String suffix) throws IOException {
    //Set permissions only to owner, posix style
    final File file = Files.createTempFile(prefix, suffix, OWNER_PERMISSIONS_ONLY_FILE_ATTRIBUTE).toFile();
    //Set again for non posix systems
    if (!(file.setReadable(true, true) && file.setWritable(true, true) && file.setExecutable(true, true))) {
      LOGGER.debug("Setting permissions failed on file {}", file.getAbsolutePath());
    }

    return file;
  }

  /**
   * Create a secure temp file for posix and other file systems.
   * <p>
   * This is equivalent to calling {@link #createSecureTempFile(String, String)} and in addition it declares that it will remove
   * the temp file with {@link File#deleteOnExit()}.
   * </p>
   *
   * <p>CAUTION: This method can have a memory impact if too many files are created, and that is because
   * {@link File#deleteOnExit()} keeps an in memory cache of the file paths. If possible prefer the use of
   * {@link #createSecureTempFile(String, String)} and make your implementation remove the temp files created explicitly.</p>
   *
   * @param prefix the prefix
   * @param suffix the suffix
   * @return the secure temp file
   * @throws IOException if the file failed to be created
   */
  @SuppressWarnings("java:S2308")
  public static File createSecureTempFileDeleteOnExit(String prefix, String suffix) throws IOException {
    final File secureTempFile = createSecureTempFile(prefix, suffix);
    secureTempFile.deleteOnExit();
    return secureTempFile;
  }

  /**
   * Create a secure temp file for posix and other file systems.
   * <p>
   * This is equivalent to calling {@link #createSecureTempFile(String, String)} with null as a prefix.
   * </p>
   *
   * @param suffix the suffix
   * @return the secure temp file
   * @throws IOException if the file failed to be created
   */
  public static File createSecureTempFile(String suffix) throws IOException {
    return createSecureTempFile(null, suffix);
  }

  /**
   * Create a secure temp file for posix and other file systems.
   * <p>
   * This is equivalent to calling {@link #createSecureTempFile(String, String)} with null as a prefix.
   * </p>
   *
   * @param suffix the suffix
   * @return the secure temp file
   * @throws IOException if the file failed to be created
   */
  public static File createSecureTempFileDeleteOnExit(String suffix) throws IOException {
    return createSecureTempFileDeleteOnExit(null, suffix);
  }
}
