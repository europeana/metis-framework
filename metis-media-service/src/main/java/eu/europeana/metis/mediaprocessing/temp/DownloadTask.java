package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.metis.mediaprocessing.MediaProcessorException;
import eu.europeana.metis.mediaservice.MediaProcessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.util.function.BiConsumer;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.client.methods.ZeroCopyConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadTask extends HttpClientTask {

  private static final Logger logger = LoggerFactory.getLogger(DownloadTask.class);

  public DownloadTask(int followRedirects, int generalConnectionLimit,
      int connectionLimitPerSource) throws MediaProcessorException {
    super(followRedirects, generalConnectionLimit, connectionLimitPerSource, 10000, 20000);
  }

  @Override
  protected HttpAsyncRequestProducer createRequestProducer(FileInfo fileInfo) {
    HttpGet request = new HttpGet(fileInfo.getUrl());
    request.setConfig(requestConfig);
    return HttpAsyncMethods.create(request);
  }

  @Override
  protected ZeroCopyConsumer<Void> createResponseConsumer(FileInfo fileInfo,
      BiConsumer<FileInfo, String> callback) throws IOException {
    File content = File.createTempFile("media", null);
    return new DownloadConsumer(fileInfo, content, callback);
  }

  private final class DownloadConsumer extends ZeroCopyConsumer<Void> {

    private final FileInfo fileInfo;
    private final File content;
    private final BiConsumer<FileInfo, String> callback;

    private DownloadConsumer(FileInfo fileInfo, File content,
        BiConsumer<FileInfo, String> callback) throws FileNotFoundException {
      super(content);
      this.fileInfo = fileInfo;
      this.content = content;
      this.callback = callback;
    }

    @Override
    protected void onResponseReceived(HttpResponse response) {
      String mimeType = ContentType.get(response.getEntity()).getMimeType();
      if (MediaProcessor.supportsLinkProcessing(mimeType)) {
        logger.debug("Skipping download: {}", fileInfo.getUrl());
        cleanup();
        fileInfo.setMimeType(mimeType);
        callback.accept(fileInfo, null);
        cancel();
        return;
      }
      super.onResponseReceived(response);
    }

    @Override
    protected Void process(HttpResponse response, File file, ContentType contentType)
        throws Exception {
      int status = response.getStatusLine().getStatusCode();
      long byteCount = file.length();
      if (status < 200 || status >= 300 || byteCount == 0) {
        logger.info("Download error (code {}) for {}", status, fileInfo.getUrl());
        cleanup();
        fileInfo.setContent(FileInfo.ERROR_FLAG);
        callback.accept(fileInfo, "DOWNLOAD: STATUS CODE " + status);
        return null;
      }
      fileInfo.setContent(file);
      fileInfo.setMimeType(contentType.getMimeType());

      logger.debug("Downloaded {} bytes: {}", byteCount, fileInfo.getUrl());
      callback.accept(fileInfo, null);
      return null;
    }

    @Override
    protected void releaseResources() {
      super.releaseResources();
      Exception e = getException();
      if (e != null) {
        String msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        logger.info("Download exception ({}) for {}", msg, fileInfo.getUrl());
        logger.trace("Download failure details:", e);
        cleanup();
        fileInfo.setContent(FileInfo.ERROR_FLAG);
        callback.accept(fileInfo, "CONNECTION ERROR: " + msg);
      }
    }

    private void cleanup() {
      if (content.exists()) {
        try {
          Files.delete(content.toPath());
        } catch (IOException e) {
          logger.warn("Could not remove temp file " + content, e);
        }
      }
    }
  }

}
