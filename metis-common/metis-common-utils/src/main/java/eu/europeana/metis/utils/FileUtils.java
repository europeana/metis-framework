package eu.europeana.metis.utils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * File utilities class
 */
public final class FileUtils {

  private static final Logger LOGGER = LoggerFactory.getLogger(FileUtils.class);

  private FileUtils() {
    //Private constructor
  }

  /**
   * Create a secure temp file for posix and other file systems.
   *
   * @param prefix the prefix
   * @param suffix the suffix
   * @return the secure temp file
   * @throws IOException if the file failed to be created
   */
  public static File createSecureTempFile(String prefix, String suffix) throws IOException {
    //Use nio that sets the posix permissions only to owner
    final File file = Files.createTempFile(prefix, suffix).toFile();
    //And set again for non posix permissions
    if (!(file.setReadable(true, true) && file.setWritable(true, true) && file.setExecutable(true, true))) {
      LOGGER.debug("Setting permissions failed on file {}", file.getAbsolutePath());
    }
    return file;
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
}
