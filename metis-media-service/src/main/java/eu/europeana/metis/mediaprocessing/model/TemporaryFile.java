package eu.europeana.metis.mediaprocessing.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * This class represents a file in the temporary folder that's associated with a resource. Please
 * see {@link ResourceFile} for more information.
 */
public abstract class TemporaryFile implements ResourceFile {

  private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

  /**
   * The resource URL of the resource with which this file is associated.
   */
  private final String resourceUrl;

  /**
   * Path pointing to the temporary file.
   */
  private Path contentPath;

  /**
   * Constructor.
   *
   * @param resourceUrl The URL of the resource with which this file is associated.
   * @param subDirectory The subdirectory within the user's temporary directory in which the file is
   * to be located.
   * @param prefix The prefix used for generating the file.
   * @param suffix The suffix used for generating the file.
   * @throws IOException In case there was a problem creating the file.
   */
  protected TemporaryFile(String resourceUrl, String subDirectory, String prefix,
      String suffix) throws IOException {

    // Construct the target directory.
    final Path directory = TEMP_DIR.resolve(subDirectory);

    // Create the directory in the temporary folder if needed.
    // Note: should use Files.exists instead when migrating away from Java 8.
    if (!directory.toFile().exists()) {
      Files.createDirectory(directory);
    }

    // Check that the directory exists and is a directory.
    // Note: should use Files.isDirectory instead when migrating away from Java 8.
    if (!directory.toFile().isDirectory()) {
      throw new IOException("Directory is not a directory: " + directory);
    }

    // Create temporary file.
    this.contentPath = Files.createTempFile(directory, prefix, suffix);

    // Set resource URL.
    this.resourceUrl = resourceUrl;
  }

  private boolean hasContent() {
    // Note: should use Files.exists instead when migrating away from Java 8.
    final boolean result = this.contentPath != null && this.contentPath.toFile().exists();
    if (!result) {
      this.contentPath = null;
    }
    return result;
  }

  @Override
  public String getResourceUrl() {
    return resourceUrl;
  }

  public Path getContentPath() {
    return this.contentPath;
  }

  @Override
  public InputStream getContentStream() throws IOException {
    if (!hasContent()) {
      throw new IOException("Cannot get the file content: file does not exist.");
    }
    return Files.newInputStream(this.contentPath);
  }

  @Override
  public long getContentSize() throws IOException {
    if (!hasContent()) {
      throw new IOException("Cannot get the file size: file does not exist.");
    }
    return Files.size(this.contentPath);
  }

  @Override
  public void close() throws IOException {
    try {
      if (hasContent()) {
        Files.delete(this.contentPath);
      }
    } finally {
      this.contentPath = null;
    }
  }
}
