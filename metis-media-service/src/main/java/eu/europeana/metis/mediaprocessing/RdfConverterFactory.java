package eu.europeana.metis.mediaprocessing;

/**
 * This factory creates objects for (de)serializing RDF files.
 * <p>Used by external code such as scripts or ECloud.</p>
 */
public class RdfConverterFactory {

  /**
   * Create an RDF file serializer.
   *
   * @return An RDF file serializer.
   */
  public RdfSerializer createRdfSerializer() {
    return new RdfSerializerImpl();
  }

  /**
   * Create an RDF file deserializer.
   *
   * @return An RDF file deserializer.
   */
  public RdfDeserializer createRdfDeserializer() {
    return new RdfDeserializerImpl();
  }
}
