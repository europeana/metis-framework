package eu.europeana.metis.mediaprocessing.temp;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import eu.europeana.metis.mediaprocessing.MediaProcessor;
import eu.europeana.metis.mediaprocessing.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.MetadataExtractionResult;

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
  public void executeLinkCheckTask(List<FileInfo> files,
      Map<String, Integer> connectionLimitsPerSource, BiConsumer<FileInfo, String> callback)
      throws MediaProcessorException {
    try {
      linkCheckTask.execute(files, connectionLimitsPerSource, callback);
    } catch (RuntimeException e) {
      throw new MediaProcessorException(e);
    }
  }

  // TODO triggering callback with null status means that the status is OK.
  // This method is thread-safe.
  public void executeDownloadTask(List<FileInfo> files,
      Map<String, Integer> connectionLimitsPerSource, BiConsumer<FileInfo, String> callback)
      throws MediaProcessorException {
    try {
      downloadTask.execute(files, connectionLimitsPerSource, callback);
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
