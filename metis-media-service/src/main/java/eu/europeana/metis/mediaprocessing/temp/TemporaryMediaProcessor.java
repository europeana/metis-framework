package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.metis.mediaprocessing.LinkChecker;
import eu.europeana.metis.mediaprocessing.MediaExtractor;
import eu.europeana.metis.mediaprocessing.exception.LinkCheckingException;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.Resource;
import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class TemporaryMediaProcessor extends TemporaryMediaService implements MediaExtractor,
    LinkChecker {

  private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryMediaProcessor.class);

  private static final int GENERAL_CONNECTION_LIMIT = 200;
  private static final int CONNECTION_LIMIT_PER_SOURCE = 4;

  private final LinkCheckTask linkCheckTask;
  private final DownloadTask downloadTask;

  private static class ExecutionResult<T> implements HttpClientCallback<RdfResourceEntry, T> {

    private T result;
    private String status;
    private String url;
    private boolean callbackExecuted = false;

    @Override
    public void accept(RdfResourceEntry input, T output, String status) {
      this.result = output;
      this.status = status;
      this.url = input.getResourceUrl();
      this.callbackExecuted = true;
    }

    public T verify() throws IOException {
      if (!this.callbackExecuted) {
        throw new IOException(
            "Problem while processing " + this.url + ": Callback not yet executed.");
      }
      if (status != null) {
        throw new IOException("Problem while processing " + this.url + ": " + status);
      }
      return result;
    }
  }

  public TemporaryMediaProcessor(int redirectCount, int commandThreadPoolSize)
      throws MediaProcessorException {
    super(commandThreadPoolSize);
    linkCheckTask =
        new LinkCheckTask(redirectCount, GENERAL_CONNECTION_LIMIT, CONNECTION_LIMIT_PER_SOURCE);
    downloadTask =
        new DownloadTask(redirectCount, GENERAL_CONNECTION_LIMIT, CONNECTION_LIMIT_PER_SOURCE);
  }

  @Override
  public void performLinkChecking(RdfResourceEntry resourceEntry) throws LinkCheckingException {
    final ExecutionResult<Void> result = new ExecutionResult<>();
    executeLinkCheckTask(Collections.singletonList(resourceEntry), Collections.emptyMap(), result,
        true);
    try {
      result.verify();
    } catch (IOException e) {
      throw new LinkCheckingException(e);
    }
  }

  // TODO triggering callback with null status means that the status is OK.
  // This method is thread-safe.
  public <I extends RdfResourceEntry> void executeLinkCheckTask(List<I> resourceLinks,
      Map<String, Integer> connectionLimitsPerSource, HttpClientCallback<I, Void> callback)
      throws LinkCheckingException {
    executeLinkCheckTask(resourceLinks, connectionLimitsPerSource, callback, false);
  }

  // This method is thread-safe.
  private <I extends RdfResourceEntry> void executeLinkCheckTask(List<I> resourceLinks,
      Map<String, Integer> connectionLimitsPerSource, HttpClientCallback<I, Void> callback,
      boolean blockUntilDone) throws LinkCheckingException {
    try {
      linkCheckTask.execute(resourceLinks, connectionLimitsPerSource, callback, blockUntilDone);
    } catch (IOException | RuntimeException e) {
      throw new LinkCheckingException(e);
    }
  }

  @Override
  public ResourceExtractionResult performMediaExtraction(RdfResourceEntry resourceEntry)
      throws MediaExtractionException {

    // Perform download of resource.
    final ExecutionResult<Resource> resourceContainer = new ExecutionResult<>();
    executeDownloadTask(Collections.singletonList(resourceEntry), Collections.emptyMap(),
        resourceContainer, true);
    final Resource resource;
    try {
      resource = resourceContainer.verify();
    } catch (IOException e) {
      throw new MediaExtractionException(e);
    }

    // Perform metadata extraction
    final Exception[] exceptionContainer = new Exception[1];
    final List<ResourceExtractionResult> results = performResourceProcessing(
        Collections.singletonList(resource), new MediaProcessingListener<Resource>() {
          @Override
          public void beforeStartingFile(Resource source) {
          }

          @Override
          public void handleMediaExtractionException(Resource source,
              MediaExtractionException exception) {
            exceptionContainer[0] = exception;
          }

          @Override
          public void handleOtherException(Resource source, Exception exception) {
            exceptionContainer[0] = exception;
          }

          @Override
          public void afterCompletingFile(Resource source) {
            try {
              source.close();
            } catch (IOException e) {
              LOGGER.warn("", e);
            }
          }
        });

    // Check for errors
    if (exceptionContainer[0] instanceof MediaExtractionException) {
      throw (MediaExtractionException) exceptionContainer[0];
    }
    if (exceptionContainer[0] != null) {
      throw new MediaExtractionException("Problem while processing " + resource.getResourceUrl(),
          exceptionContainer[0]);
    }

    // Return result.
    if (results.size() != 1) {
      throw new IllegalStateException("Unexpected result size!");
    }
    return results.get(0);
  }

  // TODO triggering callback with null status means that the status is OK.
  // This method is thread-safe.
  public <I extends RdfResourceEntry> void executeDownloadTask(List<I> resourceLinks,
      Map<String, Integer> connectionLimitsPerSource, HttpClientCallback<I, Resource> callback)
      throws MediaExtractionException {
    executeDownloadTask(resourceLinks, connectionLimitsPerSource, callback, false);
  }

  // This method is thread-safe.
  private <I extends RdfResourceEntry> void executeDownloadTask(List<I> resourceLinks,
      Map<String, Integer> connectionLimitsPerSource, HttpClientCallback<I, Resource> callback,
      boolean blockUntilDone) throws MediaExtractionException {
    try {
      downloadTask.execute(resourceLinks, connectionLimitsPerSource, callback, blockUntilDone);
    } catch (IOException | RuntimeException e) {
      throw new MediaExtractionException(e);
    }
  }

  @Override
  public void close() {
    super.close();
    linkCheckTask.close();
    downloadTask.close();
  }
}
