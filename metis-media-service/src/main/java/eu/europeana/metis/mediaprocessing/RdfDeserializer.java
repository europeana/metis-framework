package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import java.io.InputStream;
import java.util.List;

public interface RdfDeserializer {

  List<RdfResourceEntry> getResourceEntriesForMediaExtraction(byte[] input)
      throws RdfDeserializationException;

  List<RdfResourceEntry> getResourceEntriesForMediaExtraction(InputStream inputStream)
      throws RdfDeserializationException;

  List<RdfResourceEntry> getResourceEntriesForLinkChecking(byte[] input)
      throws RdfDeserializationException;

  List<RdfResourceEntry> getResourceEntriesForLinkChecking(InputStream inputStream)
      throws RdfDeserializationException;

  EnrichedRdf getRdfForResourceEnriching(byte[] input) throws RdfDeserializationException;

  EnrichedRdf getRdfForResourceEnriching(InputStream inputStream)
      throws RdfDeserializationException;

}
