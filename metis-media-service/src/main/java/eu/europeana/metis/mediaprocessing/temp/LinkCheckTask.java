package eu.europeana.metis.mediaprocessing.temp;

import java.io.IOException;
import java.util.function.Function;
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
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;

public class LinkCheckTask extends HttpClientTask<Void> {

  private static final Logger logger = LoggerFactory.getLogger(LinkCheckTask.class);

  public LinkCheckTask(int followRedirects, int generalConnectionLimit,
      int connectionLimitPerSource) throws MediaProcessorException {
    super(followRedirects, generalConnectionLimit, connectionLimitPerSource, 2000, 5000);
  }

  @Override
  protected HttpAsyncRequestProducer createRequestProducer(String resourceUrl) {
    HttpHead request = new HttpHead(resourceUrl);
    request.setConfig(requestConfig);
    return HttpAsyncMethods.create(request);
  }

  @Override
  protected <I> HttpAsyncResponseConsumer<Void> createResponseConsumer(I resourceLink,
      HttpClientCallback<I, Void> callback, Function<I, String> urlExtractor) {
    return new HeadResponseConsumer<>(resourceLink, callback, urlExtractor.apply(resourceLink));
  }

  private abstract class ResponseConsumer<I> extends AbstractAsyncResponseConsumer<Void> {

    final I resourceLink;
    final String resourceUrl;
    final HttpClientCallback<I, Void> callback;

    protected ResponseConsumer(I resourceLink, HttpClientCallback<I, Void> callback,
        String resourceUrl) {
      this.resourceLink = resourceLink;
      this.callback = callback;
      this.resourceUrl = resourceUrl;
    }

    @Override
    protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
      int status = response.getStatusLine().getStatusCode();
      if (status < 200 || status >= 300) {
        logger.info("Link error (code {}) for {}", status, resourceUrl);
        callback.accept(resourceLink, null, "STATUS CODE " + status);
        return;
      }
      logger.debug("Link OK: {}", resourceUrl);
      callback.accept(resourceLink, null, null);
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
        logger.info("Link exception ({}) for {}", msg, resourceUrl);
        logger.trace("Link failure details:", e);
        callback.accept(resourceLink, null, "CONNECTION ERROR: " + msg);
      }
    }
  }

  private class HeadResponseConsumer<I> extends ResponseConsumer<I> {

    protected HeadResponseConsumer(I resourceLink, HttpClientCallback<I, Void> callback,
        String resourceUrl) {
      super(resourceLink, callback, resourceUrl);
    }

    @Override
    @SuppressWarnings("resource")
    protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
      int status = response.getStatusLine().getStatusCode();
      if (status >= 400 && status < 500) {
        logger.info("HEAD rejected ({}), retrying with GET: {}", response.getStatusLine(),
            resourceUrl);
        HttpGet request = new HttpGet(resourceUrl);
        request.setConfig(requestConfig);
        httpClient.execute(HttpAsyncMethods.create(request),
            new GetResponseConsumer<>(resourceLink, callback, resourceUrl), null);
        return;
      }
      super.onResponseReceived(response);
    }
  }

  private class GetResponseConsumer<I> extends ResponseConsumer<I> {

    protected GetResponseConsumer(I resourceLink, HttpClientCallback<I, Void> callback,
        String resourceUrl) {
      super(resourceLink, callback, resourceUrl);
    }

    @Override
    protected void onResponseReceived(HttpResponse response) throws HttpException, IOException {
      cancel();
      super.onResponseReceived(response);
    }
  }
}
