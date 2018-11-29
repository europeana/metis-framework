package eu.europeana.metis.mediaprocessing;

import java.io.Closeable;

public interface MediaProcessor extends Closeable {

  // This method is thread-safe?
  void perormLinkCheckingOnRecord(byte[] record) throws MediaProcessorException;

  // This method is thread-safe?
  MetadataExtractionResult performMetadataExtractionOnRecord(byte[] record)
      throws MediaProcessorException;

}
