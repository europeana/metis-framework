package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.metis.mediaprocessing.MediaProcessorException;
import java.io.IOException;
import java.util.function.BiConsumer;
import org.apache.http.HttpEntity;
import org.apache.http.HttpException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpHead;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.ContentDecoder;
import org.apache.http.nio.IOControl;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.protocol.AbstractAsyncResponseConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.protocol.HttpContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LinkCheckTask extends HttpClientTask {

  private static final Logger logger = LoggerFactory.getLogger(LinkCheckTask.class);

  public LinkCheckTask(int followRedirects, int generalConnectionLimit,
      int connectionLimitPerSource) throws MediaProcessorException {
    super(followRedirects, generalConnectionLimit, connectionLimitPerSource, 2000, 5000);
  }

  @Override
  protected HttpAsyncRequestProducer createRequestProducer(FileInfo fileInfo) {
    HttpHead request = new HttpHead(fileInfo.getUrl());
    request.setConfig(requestConfig);
    return HttpAsyncMethods.create(request);
  }

  @Override
  protected HttpAsyncResponseConsumer<Void> createResponseConsumer(FileInfo fileInfo,
      BiConsumer<FileInfo, String> callback) {
    return new HeadResponseConsumer(fileInfo, callback);
  }

  private abstract class ResponseConsumer extends AbstractAsyncResponseConsumer<Void> {

    final FileInfo fileInfo;
    final BiConsumer<FileInfo, String> callback;

    protected ResponseConsumer(FileInfo fileInfo, BiConsumer<FileInfo, String> callback) {
      this.fileInfo = fileInfo;
      this.callback = callback;
    }

    @Override
    protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
      int status = response.getStatusLine().getStatusCode();
      if (status < 200 || status >= 300) {
        logger.info("Link error (code {}) for {}", status, fileInfo.getUrl());
        callback.accept(fileInfo, "STATUS CODE " + status);
        return;
      }
      logger.debug("Link OK: {}", fileInfo.getUrl());
      callback.accept(fileInfo, null);
    }

    @Override
    protected void onContentReceived(ContentDecoder decoder, IOControl ioctrl) throws IOException {
      logger.error("unexpected content received");
      ioctrl.shutdown();
    }

    @Override
    protected void onEntityEnclosed(HttpEntity entity, ContentType contentType) throws IOException {
      // ignoring entity
    }

    @Override
    protected Void buildResult(HttpContext context) throws Exception {
      return null;
    }

    @Override
    protected void releaseResources() {
      Exception e = getException();
      if (e != null) {
        Object msg = e.getMessage() != null ? e.getMessage() : e.getClass().getSimpleName();
        logger.info("Link exception ({}) for {}", msg, fileInfo.getUrl());
        logger.trace("Link failure details:", e);
        callback.accept(fileInfo, "CONNECTION ERROR: " + msg);
      }
    }
  }

  private class HeadResponseConsumer extends ResponseConsumer {

    protected HeadResponseConsumer(FileInfo fileInfo, BiConsumer<FileInfo, String> callback) {
      super(fileInfo, callback);
    }

    @Override
    @SuppressWarnings("resource")
    protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
      int status = response.getStatusLine().getStatusCode();
      if (status >= 400 && status < 500) {
        logger.info("HEAD rejected ({}), retrying with GET: {}", response.getStatusLine(),
            fileInfo.getUrl());
        HttpGet request = new HttpGet(fileInfo.getUrl());
        request.setConfig(requestConfig);
        httpClient.execute(HttpAsyncMethods.create(request),
            new GetResponseConsumer(fileInfo, callback), null);
        return;
      }
      super.onResponseReceived(response);
    }
  }

  private class GetResponseConsumer extends ResponseConsumer {

    protected GetResponseConsumer(FileInfo fileInfo, BiConsumer<FileInfo, String> callback) {
      super(fileInfo, callback);
    }

    @Override
    protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
      cancel();
      super.onResponseReceived(response);
    }
  }
}
