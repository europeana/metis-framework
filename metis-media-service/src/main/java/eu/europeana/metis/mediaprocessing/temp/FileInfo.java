package eu.europeana.metis.mediaprocessing.temp;

import java.io.File;
import java.net.InetAddress;

public class FileInfo {

  // TODO JV signal errors in a different way.
  public static final File ERROR_FLAG = new File("a");

  private final String url;
  private File content;
  private String mimeType;

  // TODO JV only used by calling code in topology.
  private InetAddress contentSource;

  public FileInfo(String url) {
    this.url = url;
  }

  public File getContent() {
    return content;
  }

  public void setContent(File content) {
    this.content = content;
  }

  // TODO JV only used by calling code in topology.
  public InetAddress getContentSource() {
    return contentSource;
  }

  // TODO JV only used by calling code in topology.
  public void setContentSource(InetAddress contentSource) {
    this.contentSource = contentSource;
  }

  public String getMimeType() {
    return mimeType;
  }

  public void setMimeType(String mimeType) {
    this.mimeType = mimeType;
  }

  public String getUrl() {
    return url;
  }
}
