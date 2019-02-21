package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.RdfConverterException;

/**
 * This factory creates objects for serializing and deserializing RDF files.
 */
public class RdfConverterFactory {

  /**
   * Create an RDF file serializer.
   *
   * @return An RDF file serializer.
   * @throws RdfConverterException In case the serializer could not be instantiated.
   */
  public RdfSerializer createRdfSerializer() throws RdfConverterException {
    return new RdfSerializerImpl();
  }

  /**
   * Create an RDF file deserializer.
   *
   * @return An RDF file deserializer.
   * @throws RdfConverterException In case the deserializer could not be instantiated.
   */
  public RdfDeserializer createRdfDeserializer() throws RdfConverterException {
    return new RdfDeserializerWithXPath();
  }
}
