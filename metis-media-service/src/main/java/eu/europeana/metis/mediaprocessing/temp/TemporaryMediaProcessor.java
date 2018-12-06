package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.metis.mediaprocessing.MediaProcessor;
import eu.europeana.metis.mediaprocessing.exception.MediaException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceProcessingResult;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Deprecated
public class TemporaryMediaProcessor extends TemporaryMediaService implements MediaProcessor {

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

    public T verify() throws MediaException {
      if (!this.callbackExecuted) {
        throw new MediaException("Problem while processing " + this.url,
            "Callback not yet executed.");
      }
      if (status != null) {
        throw new MediaException("Problem while processing " + this.url, status);
      }
      return result;
    }
  }

  public TemporaryMediaProcessor(int redirectCount, int generalConnectionLimit,
      int connectionLimitPerSource) throws MediaProcessorException {
    linkCheckTask =
        new LinkCheckTask(redirectCount, generalConnectionLimit, connectionLimitPerSource);
    downloadTask =
        new DownloadTask(redirectCount, generalConnectionLimit, connectionLimitPerSource);
  }

  @Override
  public void performLinkChecking(RdfResourceEntry resourceEntry)
      throws MediaException, MediaProcessorException {
    final ExecutionResult<Void> result = new ExecutionResult<>();
    executeLinkCheckTask(Collections.singletonList(resourceEntry), Collections.emptyMap(), result,
        true);
    result.verify();
  }

  // TODO triggering callback with null status means that the status is OK.
  // This method is thread-safe.
  public <I extends RdfResourceEntry> void executeLinkCheckTask(List<I> resourceLinks,
      Map<String, Integer> connectionLimitsPerSource, HttpClientCallback<I, Void> callback)
      throws MediaProcessorException {
    executeLinkCheckTask(resourceLinks, connectionLimitsPerSource, callback, false);
  }

  // This method is thread-safe.
  private <I extends RdfResourceEntry> void executeLinkCheckTask(List<I> resourceLinks,
      Map<String, Integer> connectionLimitsPerSource, HttpClientCallback<I, Void> callback,
      boolean blockUntilDone) throws MediaProcessorException {
    try {
      linkCheckTask.execute(resourceLinks, connectionLimitsPerSource, callback, blockUntilDone);
    } catch (RuntimeException e) {
      throw new MediaProcessorException(e);
    }
  }

  @Override
  public ResourceProcessingResult performMediaExtraction(RdfResourceEntry resourceEntry)
      throws MediaException, MediaProcessorException {

    // Perform download of resource.
    final ExecutionResult<Resource> resourceContainer = new ExecutionResult<>();
    executeDownloadTask(Collections.singletonList(resourceEntry), Collections.emptyMap(),
        resourceContainer, true);
    final Resource resource = resourceContainer.verify();

    // Perform metadata extraction
    final Exception[] exceptionContainer = new Exception[1];
    final ResourceProcessingResult result = performResourceProcessing(
        Collections.singletonList(resource), new MediaProcessingListener<Resource>() {
          @Override
          public void beforeStartingFile(Resource source) {
          }

          @Override
          public boolean handleMediaException(Resource source, MediaException exception) {
            exceptionContainer[0] = exception;
            return false;
          }

          @Override
          public boolean handleOtherException(Resource source, Exception exception) {
            exceptionContainer[0] = exception;
            return false;
          }

          @Override
          public void afterCompletingFile(Resource source) {
          }
        }).get(0);

    // Check for errors
    if (exceptionContainer[0] instanceof MediaException) {
      throw (MediaException) exceptionContainer[0];
    }
    if (exceptionContainer[0] instanceof MediaProcessorException) {
      throw (MediaProcessorException) exceptionContainer[0];
    }
    if (exceptionContainer[0] != null) {
      throw new MediaProcessorException("Problem while processing " + resource.getResourceUrl(),
          exceptionContainer[0]);
    }

    // Return result.
    return result;
  }

  // TODO triggering callback with null status means that the status is OK.
  // This method is thread-safe.
  public <I extends RdfResourceEntry> void executeDownloadTask(List<I> resourceLinks,
      Map<String, Integer> connectionLimitsPerSource, HttpClientCallback<I, Resource> callback)
      throws MediaProcessorException {
    executeDownloadTask(resourceLinks, connectionLimitsPerSource, callback, false);
  }

  // This method is thread-safe.
  private <I extends RdfResourceEntry> void executeDownloadTask(List<I> resourceLinks,
      Map<String, Integer> connectionLimitsPerSource, HttpClientCallback<I, Resource> callback,
      boolean blockUntilDone) throws MediaProcessorException {
    try {
      downloadTask.execute(resourceLinks, connectionLimitsPerSource, callback, blockUntilDone);
    } catch (RuntimeException e) {
      throw new MediaProcessorException(e);
    }
  }

  @Override
  public void close() {
    linkCheckTask.close();
    downloadTask.close();
  }
}
