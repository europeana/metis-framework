package eu.europeana.metis.mediaprocessing.model;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * This class represents a file in the temporary folder that's associated with a resource. Please
 * see {@link ResourceFile} for more information.
 */
public abstract class TemporaryFile implements ResourceFile {

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
   * @param prefix The prefix used for generating the file.
   * @param suffix The suffix used for generating the file.
   * @throws IOException In case there was a problem creating the file.
   */
  protected TemporaryFile(String resourceUrl, String prefix, String suffix) throws IOException {
    this.contentPath = Files.createTempFile(prefix, suffix);
    this.resourceUrl = resourceUrl;
  }

  // TODO should be private
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
