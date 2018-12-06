package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.MediaException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.ResourceProcessingResult;
import java.io.Closeable;

public interface MediaProcessor extends Closeable {

  ResourceProcessingResult performMediaExtraction(RdfResourceEntry resourceEntry)
      throws MediaException, MediaProcessorException;

  void performLinkChecking(RdfResourceEntry resourceEntry)
      throws MediaException, MediaProcessorException;

}
