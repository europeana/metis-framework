package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.extraction.MediaProcessor;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import java.io.Closeable;
import java.io.File;
import java.io.IOException;

@Deprecated
public class TemporaryMediaService implements Closeable {

  private final MediaProcessor mediaProcessor;

  public TemporaryMediaService(int commandThreadPoolSize) throws MediaProcessorException {
    mediaProcessor = new MediaProcessor(commandThreadPoolSize);
  }

  // This method is probably thread-safe.
  ResourceExtractionResult performResourceProcessing(Resource resource)
      throws MediaExtractionException {
    final File file = resource.getContentPath() == null ? null : resource.getContentPath().toFile();
    return mediaProcessor.processResource(resource.getResourceUrl(), resource.getUrlTypes(),
        resource.getMimeType(), file);
  }

  @Override
  public void close() throws IOException {
    mediaProcessor.close();
  }
}
