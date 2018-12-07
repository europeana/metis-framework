package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.RdfConverter.Parser;
import eu.europeana.metis.mediaprocessing.RdfConverter.Writer;
import eu.europeana.metis.mediaprocessing.exception.RdfConverterException;

public class RdfConverterFactory {

  public RdfSerializer createRdfSerializer() throws RdfConverterException {
    return new Writer();
  }

  public RdfDeserializer createRdfDeserializer() throws RdfConverterException {
    return new Parser();
  }
}
