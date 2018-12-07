package eu.europeana.metis.mediaprocessing.model;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class TemporaryFileImpl implements TemporaryFile, Closeable {

  private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

  /**
   * The original resource url
   */
  private final String resourceUrl;

  /**
   * Temporary file with the thumbnail content.
   */
  private Path contentPath;

  protected TemporaryFileImpl(String resourceUrl, String subDirectory, String prefix,
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

  @Override
  public boolean hasContent() {
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
      throw new IllegalStateException("Cannot get the file content: file does not exist.");
    }
    return Files.newInputStream(this.contentPath);
  }

  @Override
  public long getContentSize() throws IOException {
    if (!hasContent()) {
      throw new IllegalStateException("Cannot get the file size: file does not exist.");
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
