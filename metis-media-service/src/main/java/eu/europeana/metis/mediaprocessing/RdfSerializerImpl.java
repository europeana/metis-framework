package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.RdfSerializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import java.nio.charset.StandardCharsets;

/**
 * This object implements RDF serialization functionality.
 */
class RdfSerializerImpl implements RdfSerializer {

  private final RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();

  @Override
  public byte[] serialize(EnrichedRdf rdf) throws RdfSerializationException {
    try {
      byte[] bytes = rdfConversionUtils.convertRdfToBytes(rdf.finalizeRdf());
      if (new String(bytes, StandardCharsets.UTF_8).trim().isEmpty()) {
        throw new RdfSerializationException("Serialized RDF is empty for: " + rdf);
      }
      return bytes;
    } catch (SerializationException e) {
      throw new RdfSerializationException("Problem with serializing RDF.", e);
    }
  }
}
