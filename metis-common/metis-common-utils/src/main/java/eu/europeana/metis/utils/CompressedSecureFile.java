package eu.europeana.metis.utils;

import java.io.File;
import java.io.IOException;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class CompressedSecureFile extends ZipFile {

  private static final Logger LOGGER = LoggerFactory.getLogger(CompressedSecureFile.class);
  static double MIN_INFLATE_RATIO = 0.01d;
  static final long DEFAULT_MAX_ENTRY_SIZE = 0xFFFFFFFFL;
  static long MAX_ENTRY_SIZE = DEFAULT_MAX_ENTRY_SIZE;

  // The maximum chars of extracted text, currently 10Mb.
  static final long DEFAULT_MAX_TEXT_SIZE = 10 * 1024 * 1024L;
  private static long MAX_TEXT_SIZE = DEFAULT_MAX_TEXT_SIZE;

  private final String fileName;


  /**
   * Sets the ratio between de- and inflated bytes to detect zip-bomb. It defaults to 1% (= 0.01d), i.e. when the compression is
   * better than 1% for any given read package part, the parsing will fail indicating a Zip-Bomb.
   *
   * @param ratio the ratio between de- and inflated bytes to detect zip-bomb
   */
  public static void setMinInflateRatio(double ratio) {
    MIN_INFLATE_RATIO = ratio;
  }

  /**
   * Returns the current minimum compression rate that is used.
   * <p>
   * See setMinInflateRatio() for details.
   *
   * @return The min accepted compression-ratio.
   */
  public static double getMinInflateRatio() {
    return MIN_INFLATE_RATIO;
  }

  /**
   * Sets the maximum file size of a single zip entry. It defaults to 4GB, i.e. the 32-bit zip format maximum.
   * <p>
   * This can be used to limit memory consumption and protect against security vulnerabilities when documents are provided by
   * users.
   *
   * @param maxEntrySize the max. file size of a single zip entry
   * @throws IllegalArgumentException for negative maxEntrySize
   */
  public static void setMaxEntrySize(long maxEntrySize) {
    if (maxEntrySize < 0) {
      throw new IllegalArgumentException("Max entry size must be greater than or equal to zero");
    } else if (maxEntrySize > DEFAULT_MAX_ENTRY_SIZE) {
      LOGGER.warn("setting max entry size greater than 4Gb can be risky; set to {} bytes", maxEntrySize);
    }
    MAX_ENTRY_SIZE = maxEntrySize;
  }

  /**
   * Returns the current maximum allowed uncompressed file size.
   * <p>
   * See setMaxEntrySize() for details.
   *
   * @return The max accepted uncompressed file size.
   */
  public static long getMaxEntrySize() {
    return MAX_ENTRY_SIZE;
  }

  /**
   * Sets the maximum number of characters of text that are extracted before an exception is thrown during extracting text from
   * documents.
   * <p>
   * This can be used to limit memory consumption and protect against security vulnerabilities when documents are provided by
   * users.
   *
   * @param maxTextSize the max. file size of a single zip entry
   * @throws IllegalArgumentException for negative maxTextSize
   */
  public static void setMaxTextSize(long maxTextSize) {
    if (maxTextSize < 0) {
      throw new IllegalArgumentException("Max text size must be greater than or equal to zero");
    } else if (maxTextSize > DEFAULT_MAX_TEXT_SIZE) {
      LOGGER.warn("setting max text size greater than {} can be risky; set to {} chars", DEFAULT_MAX_TEXT_SIZE, maxTextSize);
    }
    MAX_TEXT_SIZE = maxTextSize;
  }

  /**
   * Returns the current maximum allowed text size.
   *
   * @return The max accepted text size.
   * @see #setMaxTextSize(long)
   */
  public static long getMaxTextSize() {
    return MAX_TEXT_SIZE;
  }

  public CompressedSecureFile(File file) throws IOException {
    super(file);
    this.fileName = file.getAbsolutePath();
  }

  public CompressedSecureFile(String name) throws IOException {
    super(name);
    this.fileName = new File(name).getAbsolutePath();
  }


  public CompressedSecureFile(File file, int i) throws IOException {
    super(file.getName());
    this.fileName = file.getAbsolutePath();
  }

  /**
   * Returns an input stream for reading the contents of the specified zip file entry.
   *
   * <p> Closing this ZIP file will, in turn, close all input
   * streams that have been returned by invocations of this method.
   *
   * @param entry the zip file entry
   * @return the input stream for reading the contents of the specified zip file entry.
   * @throws IOException if an I/O error has occurred
   * @throws IllegalStateException if the zip file has been closed
   */

  @Override
  public CompressedFileInputStreamThreshold getInputStream(ZipEntry entry) throws IOException {
    CompressedFileInputStreamThreshold fis = new CompressedFileInputStreamThreshold(super.getInputStream(entry));
    fis.setEntry((ZipArchiveEntry) entry);
    return fis;
  }

  /**
   * Returns the path name of the ZIP file.
   *
   * @return the path name of the ZIP file
   */
  public String getName() {
    return fileName;
  }
}

