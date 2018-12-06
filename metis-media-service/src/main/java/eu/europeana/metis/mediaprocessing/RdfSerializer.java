package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import java.io.OutputStream;

public interface RdfSerializer {

  byte[] serialize(EnrichedRdf rdf) throws MediaProcessorException;

  void serialize(EnrichedRdf rdf, OutputStream outputStream) throws MediaProcessorException;

}
