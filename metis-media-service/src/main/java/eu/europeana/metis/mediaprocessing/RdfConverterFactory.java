package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.exception.RdfSerializationException;

/**
 * This factory creates objects for (de)serializing RDF files.
 * <p>Used by external code such as scripts or ECloud.</p>
 */
public class RdfConverterFactory {

  /**
   * Create an RDF file serializer.
   *
   * @return An RDF file serializer.
   * @throws RdfSerializationException In case the serializer could not be instantiated.
   */
  public RdfSerializer createRdfSerializer() throws RdfSerializationException {
    return new RdfSerializerImpl();
  }

  /**
   * Create an RDF file deserializer.
   *
   * @return An RDF file deserializer.
   * @throws RdfDeserializationException In case the deserializer could not be instantiated.
   */
  public RdfDeserializer createRdfDeserializer() throws RdfDeserializationException {
    return new RdfDeserializerImpl();
  }
}
