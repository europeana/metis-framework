package eu.europeana.metis.mediaprocessing;

import java.io.Closeable;

public interface MediaProcessor extends Closeable {

  void perormLinkCheckingOnRecord(byte[] record) throws MediaProcessorException;

  MetadataExtractionResult performMetadataExtractionOnRecord(byte[] record)
      throws MediaProcessorException;

}
