package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.exception.MediaException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public abstract class TemporaryResourceFileImpl implements TemporaryResourceFile {

  private static final Path TEMP_DIR = Paths.get(System.getProperty("java.io.tmpdir"));

  /**
   * The original resource url
   */
  private final String resourceUrl;

  /**
   * Temporary file with the thumbnail content.
   */
  private Path contentPath;

  protected TemporaryResourceFileImpl(String resourceUrl, String subDirectory, String prefix,
      String suffix) throws MediaException {

    // Construct the target directory.
    final Path directory = TEMP_DIR.resolve(subDirectory);

    // Create the directory in the temporary folder if needed.
    // Note: should use Files.exists instead when migrating away from Java 8.
    if (!directory.toFile().exists()) {
      try {
        Files.createDirectory(directory);
      } catch (IOException e) {
        throw new MediaException("Could not create thumbnails subdirectory: " + directory, e);
      }
    }

    // Check that the directory exists and is a directory.
    // Note: should use Files.isDirectory instead when migrating away from Java 8.
    if (!directory.toFile().isDirectory()) {
      throw new MediaException("Thumbnails directory is not a directory: " + directory);
    }

    // Create temporary file.
    try {
      this.contentPath = Files.createTempFile(directory, prefix, suffix);
    } catch (IOException e) {
      throw new MediaException("Could not create thumbnails file. ", e);
    }

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
  public InputStream getContentStream() throws MediaProcessorException {
    if (!hasContent()) {
      throw new IllegalStateException("Cannot get the file content: file does not exist.");
    }
    try {
      return Files.newInputStream(this.contentPath);
    } catch (IOException e) {
      throw new MediaProcessorException("Could not read file " + this.contentPath, e);
    }
  }

  @Override
  public long getContentSize() throws MediaProcessorException {
    if (!hasContent()) {
      throw new IllegalStateException("Cannot get the file size: file does not exist.");
    }
    try {
      return Files.size(this.contentPath);
    } catch (IOException e) {
      throw new MediaProcessorException("Could not read file " + this.contentPath, e);
    }
  }

  @Override
  public void deleteFile() throws MediaProcessorException {
    try {
      if (hasContent()) {
        Files.delete(this.contentPath);
      }
    } catch (IOException e) {
      throw new MediaProcessorException("Could not delete file: " + this.contentPath, e);
    } finally {
      this.contentPath = null;
    }
  }
}
