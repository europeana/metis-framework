package eu.europeana.metis.mediaprocessing;

import java.io.Closeable;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;

public interface MediaProcessor extends Closeable {

  // This method is thread-safe?
  void perormLinkCheckingOnRecord(byte[] record) throws MediaProcessorException;

  // This method is thread-safe?
  MetadataExtractionResult performMetadataExtractionOnRecord(byte[] record)
      throws MediaProcessorException;

}
