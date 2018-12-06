package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.RdfConverter.Parser;
import eu.europeana.metis.mediaprocessing.RdfConverter.Writer;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;

public class RdfConverterFactory {

  public RdfSerializer createRdfSerializer() throws MediaProcessorException {
    return new Writer();
  }

  public RdfDeserializer createRdfDeserializer() throws MediaProcessorException {
    return new Parser();
  }
}
