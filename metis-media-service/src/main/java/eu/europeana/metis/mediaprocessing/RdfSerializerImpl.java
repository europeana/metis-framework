package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.RdfSerializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;

/**
 * This object implements RDF serialization functionality.
 */
class RdfSerializerImpl implements RdfSerializer {

  private final RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();

  @Override
  public byte[] serialize(EnrichedRdf rdf) throws RdfSerializationException {
    try {
      return rdfConversionUtils.convertRdfToBytes(rdf.finalizeRdf());
    } catch (SerializationException e) {
      throw new RdfSerializationException("Problem with serializing RDF.", e);
    }
  }
}
