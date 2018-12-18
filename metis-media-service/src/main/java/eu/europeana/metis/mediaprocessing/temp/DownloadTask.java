package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.extraction.MediaProcessor;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.entity.ContentType;
import org.apache.http.nio.client.methods.HttpAsyncMethods;
import org.apache.http.nio.client.methods.ZeroCopyConsumer;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DownloadTask extends HttpClientTask<Resource> {

  private static final Logger logger = LoggerFactory.getLogger(DownloadTask.class);

  public DownloadTask(int followRedirects, int generalConnectionLimit,
      int connectionLimitPerSource) throws MediaProcessorException {
    super(followRedirects, generalConnectionLimit, connectionLimitPerSource, 10000, 20000);
  }

  @Override
  protected HttpAsyncRequestProducer createRequestProducer(String resourceUrl) {
    HttpGet request = new HttpGet(resourceUrl);
    request.setConfig(requestConfig);
    return HttpAsyncMethods.create(request);
  }

  @Override
  protected <I extends RdfResourceEntry> ZeroCopyConsumer<Void> createResponseConsumer(
      I resourceLink, HttpClientCallback<I, Resource> callback) throws IOException {
    final Resource resource = new Resource(resourceLink, null);
    return new DownloadConsumer<>(resourceLink, resource, callback);
  }

  private final class DownloadConsumer<I> extends ZeroCopyConsumer<Void> {

    private final I resourceLink;
    private final Resource resource;
    private final HttpClientCallback<I, Resource> callback;

    private DownloadConsumer(I resourceLink, Resource resource,
        HttpClientCallback<I, Resource> callback) throws FileNotFoundException {
      super(resource.getContentPath().toFile());
      this.resourceLink = resourceLink;
      this.resource = resource;
      this.callback = callback;
    }

    @Override
    protected void onResponseReceived(HttpResponse response) {
      String mimeType = ContentType.get(response.getEntity()).getMimeType();
      if (MediaProcessor.supportsLinkProcessing(mimeType)) {
        logger.debug("Skipping download: {}", resource.getResourceUrl());
        deleteFile();
        resource.setMimeType(mimeType);
        callback.accept(resourceLink, resource, null);
        cancel();
        return;
      }
      super.onResponseReceived(response);
    }

    @Override
    protected Void process(HttpResponse response, File file, ContentType contentType) {
      final int status = response.getStatusLine().getStatusCode();
      final long byteCount = file.length();
      final String statusString;
      if (status < 200 || status >= 300 || byteCount == 0) {
        logger.info("Download error (code {}) for {}", status, resource.getResourceUrl());
        deleteFile();
        statusString = "DOWNLOAD: STATUS CODE " + status;
      } else {
        resource.setMimeType(contentType.getMimeType());
        logger.debug("Downloaded {} bytes: {}", byteCount, resource.getResourceUrl());
        statusString = null;
      }
      callback.accept(resourceLink, resource, statusString);
      return null;
    }

    @Override
    protected void releaseResources() {
      super.releaseResources();
      final Exception exception = getException();
      if (exception != null) {
        final String message = exception.getMessage() != null ? exception.getMessage()
            : exception.getClass().getSimpleName();
        logger.info("Download exception ({}) for {}", message, resource.getResourceUrl());
        logger.trace("Download failure details:", exception);
        deleteFile();
        callback.accept(resourceLink, resource, "CONNECTION ERROR: " + message);
      }
    }

    private void deleteFile() {
      try {
        resource.close();
      } catch (IOException e) {
        logger.warn("Could not delete resource.", e);
      }
    }
  }
}
