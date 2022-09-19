package eu.europeana.metis.utils;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Optional;
import java.util.stream.Stream;

/**
 * Collection of supported file compressions.
 */
public enum CompressedFileExtension {

  /**
   * ZIP files.
   **/
  ZIP(".zip"),

  /**
   * GZIP files (that are not also tar files)
   **/
  GZIP(".gz"),

  /**
   * Tarred GZIP files.
   **/
  TAR_GZ(".tar.gz"),

  /**
   * Tarred GZIP files.
   **/
  TGZIP(".tgz");

  private final String extension;

  CompressedFileExtension(String extension) {
    this.extension = extension;
  }

  /**
   * @return The extension associated with this file compression (including the separator).
   */
  public final String getExtension() {
    return extension;
  }

  private boolean hasExtension(String fileName) {
    return fileName.endsWith(getExtension());
  }

  /**
   * Find the compression for the given file based on the file's extension.
   *
   * @param file The file for which to return the compression.
   * @return The compression, or null if the file extension does not reflect a supported
   * compression.
   */
  public static CompressedFileExtension forPath(Path file) {
    // Note: sort to check long extension first (we want to match most specific extension).
    return Arrays.stream(values())
            .sorted(Comparator.comparing(candidate -> -candidate.getExtension().length()))
            .filter(extension -> extension.hasExtension(file.toString()))
            .findFirst()
            .orElse(null);
  }

  /**
   * Remove the extension of one of the supported compressions from the file.
   *
   * @param file The file from which to remove the extension.
   * @return The path without the file extension.
   * @throws IllegalArgumentException If the file does not have a supported file extension.
   */
  public static Path removeExtension(Path file) {
    final CompressedFileExtension extension = forPath(file);
    if (extension == null || file.getFileName() == null) {
      throw new IllegalArgumentException("File " + file + " is not a recognised compressed file.");
    }
    final String fileName = file.getFileName().toString();
    final String newFileName = fileName
            .substring(0, fileName.length() - extension.getExtension().length());
    return Optional.ofNullable(file.getParent()).map(parent -> parent.resolve(newFileName))
            .orElseGet(()-> Path.of(newFileName));
  }

  /**
   * Checks whether the file has a supported compressed file extension.
   *
   * @param file The file.
   * @return Whether the file has a supported compressed file extension.
   */
  public static boolean hasCompressedFileExtension(Path file) {
    return file.getFileName() != null && hasCompressedFileExtension(file.getFileName().toString());
  }

  /**
   * Checks whether the file name has a supported compressed file extension.
   *
   * @param fileName The file name.
   * @return Whether the file name has a supported compressed file extension.
   */
  public static boolean hasCompressedFileExtension(String fileName) {
    return Stream.of(values()).anyMatch(extension -> extension.hasExtension(fileName));
  }
}
