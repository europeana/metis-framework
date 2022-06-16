package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.utils.TempFileUtils;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * This class represents a file in the temporary folder that's associated with a resource. Please
 * see {@link ResourceRelatedFile} for more information.
 */
abstract class AbstractTemporaryFile implements ResourceRelatedFile {

  /**
   * The resource URL of the resource with which this file is associated.
   */
  private final String resourceUrl;

  /**
   * Path pointing to the temporary file.
   */
  private Path contentPath;

  /**
   * Action that creates the content file.
   */
  private final ContentFileCreator contentFileCreator;

  /**
   * Constructor.
   *
   * @param resourceUrl The URL of the resource with which this file is associated.
   * @param prefix The prefix used for generating the file.
   * @param suffix The suffix used for generating the file.
   */
  AbstractTemporaryFile(String resourceUrl, String prefix, String suffix) {
    this.resourceUrl = resourceUrl;
    this.contentFileCreator = () -> TempFileUtils.createSecureTempFile(prefix, suffix).toPath();
  }

  @Override
  public String getResourceUrl() {
    return resourceUrl;
  }

  Path getContentPath() {
    return this.contentPath;
  }

  @Override
  public void markAsWithContent(InputStream newContent) throws IOException {
    if (contentPath == null) {
      this.contentPath = contentFileCreator.createFile();
    }
    Files.copy(newContent, this.contentPath, StandardCopyOption.REPLACE_EXISTING);
  }

  private Long computeContentSizeInternal() throws IOException {

    // If the content path does not exist, remove the reference.
    if (this.contentPath != null && Files.notExists(this.contentPath)) {
      this.contentPath = null;
    }

    // Return the size and null in case the content file does not exist.
    return this.contentPath == null ? null : Files.size(this.contentPath);
  }

  @Override
  public boolean hasContent() throws IOException {
    final Long result = computeContentSizeInternal();
    return result != null && result > 0;
  }

  @Override
  public InputStream getContentStream() throws IOException {
    if (!hasContent()) {
      throw new IOException("Cannot get the file content: file does not exist or is empty.");
    }
    return Files.newInputStream(this.contentPath);
  }

  @Override
  public Long getContentSize() throws IOException {
    final Long result = computeContentSizeInternal();
    if (result == null) {
      throw new IOException("Cannot get the file size: file does not exist.");
    }
    return result;
  }

  @Override
  public void markAsNoContent() throws IOException {
    try {
      if (this.contentPath != null) {
        Files.deleteIfExists(this.contentPath);
      }
    } finally {
      this.contentPath = null;
    }
  }

  @Override
  public void close() throws IOException {
    this.markAsNoContent();
  }

  @FunctionalInterface
  private interface ContentFileCreator {

    Path createFile() throws IOException;
  }
}
