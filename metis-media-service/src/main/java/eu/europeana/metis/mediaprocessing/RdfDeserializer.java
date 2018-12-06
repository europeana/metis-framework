package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import java.io.InputStream;
import java.util.List;

public interface RdfDeserializer {

  List<RdfResourceEntry> getResourceEntriesForMetadataExtraction(byte[] input)
      throws MediaProcessorException;

  List<RdfResourceEntry> getResourceEntriesForMetadataExtraction(InputStream inputStream)
      throws MediaProcessorException;

  List<RdfResourceEntry> getResourceEntriesForLinkChecking(byte[] input)
      throws MediaProcessorException;

  List<RdfResourceEntry> getResourceEntriesForLinkChecking(InputStream inputStream)
      throws MediaProcessorException;

  EnrichedRdf getRdfForResourceEnriching(byte[] input) throws MediaProcessorException;

  EnrichedRdf getRdfForResourceEnriching(InputStream inputStream) throws MediaProcessorException;

}
