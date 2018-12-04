package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.metis.mediaprocessing.MediaProcessor;
import eu.europeana.metis.mediaprocessing.MetadataExtractionResult;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Deprecated
public class TemporaryMediaProcessor extends TemporaryMediaService implements MediaProcessor {

  private final LinkCheckTask linkCheckTask;
  private final DownloadTask downloadTask;

  public TemporaryMediaProcessor(int redirectCount, int generalConnectionLimit,
      int connectionLimitPerSource) throws MediaProcessorException {
    linkCheckTask =
        new LinkCheckTask(redirectCount, generalConnectionLimit, connectionLimitPerSource);
    downloadTask =
        new DownloadTask(redirectCount, generalConnectionLimit, connectionLimitPerSource);
  }

  // TODO triggering callback with null status means that the status is OK.
  // This method is thread-safe.
  public <I extends RdfResourceEntry> void executeLinkCheckTask(List<I> resourceLinks,
      Map<String, Integer> connectionLimitsPerSource, HttpClientCallback<I, Void> callback)
      throws MediaProcessorException {
    try {
      linkCheckTask.execute(resourceLinks, connectionLimitsPerSource, callback);
    } catch (RuntimeException e) {
      throw new MediaProcessorException(e);
    }
  }

  // TODO triggering callback with null status means that the status is OK.
  // This method is thread-safe.
  public <I extends RdfResourceEntry> void executeDownloadTask(List<I> resourceLinks,
      Map<String, Integer> connectionLimitsPerSource, HttpClientCallback<I, Resource> callback)
      throws MediaProcessorException {
    try {
      downloadTask.execute(resourceLinks, connectionLimitsPerSource, callback);
    } catch (RuntimeException e) {
      throw new MediaProcessorException(e);
    }
  }

  @Override
  public void perormLinkCheckingOnRecord(byte[] record) throws MediaProcessorException {
    // TODO
    throw new UnsupportedOperationException();
  }

  @Override
  public MetadataExtractionResult performMetadataExtractionOnRecord(byte[] record)
      throws MediaProcessorException {
    // TODO
    throw new UnsupportedOperationException();
  }

  @Override
  public void close() {
    linkCheckTask.close();
    downloadTask.close();
    super.close();
  }
}
