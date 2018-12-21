package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.metis.mediaprocessing.LinkChecker;
import eu.europeana.metis.mediaprocessing.MediaExtractor;
import eu.europeana.metis.mediaprocessing.exception.LinkCheckingException;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.http.LinkCheckClient;
import eu.europeana.metis.mediaprocessing.http.ResourceDownloadClient;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Deprecated
public class TemporaryMediaProcessor extends TemporaryMediaService implements MediaExtractor,
    LinkChecker {

  private static final Logger LOGGER = LoggerFactory.getLogger(TemporaryMediaProcessor.class);

  private final LinkCheckClient linkCheckClient;
  private final ResourceDownloadClient resourceDownloadClient;

  public TemporaryMediaProcessor(int redirectCount, int commandThreadPoolSize)
      throws MediaProcessorException {
    super(commandThreadPoolSize);
    linkCheckClient = new LinkCheckClient(redirectCount);
    resourceDownloadClient = new ResourceDownloadClient(redirectCount);
  }

  @Override
  public void performLinkChecking(RdfResourceEntry resourceEntry) throws LinkCheckingException {
    try {
      linkCheckClient.download(resourceEntry);
    } catch (IOException | RuntimeException e) {
      throw new LinkCheckingException(e);
    }
  }

  @Override
  public ResourceExtractionResult performMediaExtraction(RdfResourceEntry resourceEntry)
      throws MediaExtractionException {

    // Download resource and then perform media extraction on it.
    final ResourceExtractionResult result;
    try (Resource resource = resourceDownloadClient.download(resourceEntry)){
      result = performResourceProcessing(resource);
    } catch (IOException | RuntimeException e) {
      throw new MediaExtractionException("Problem while processing " + resourceEntry.getResourceUrl(), e);
    }

    // Return result.
    if (result == null) {
      // TODO unknown type... should that result in an exception? Or should we just return null.
      throw new IllegalStateException("Unexpected result size!");
    }
    return result;
  }

  @Override
  public void close() throws IOException {
    try {
      super.close();
    } finally {
      try {
        linkCheckClient.close();
      } finally {
        resourceDownloadClient.close();
      }
    }
  }
}
