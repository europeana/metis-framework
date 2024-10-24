package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.RdfSerializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;

/**
 * Implementations of this interface provide a variety of serialization options for RDF files. This
 * object can be reused multiple times, as the construction of it incurs overhead. Please note that
 * this object is thread-safe, but currently it achieves this by synchronization, meaning that it is
 * not designed for many threads to access the object simultaneously.
 */
public interface RdfSerializer {

  /**
   * Serialize an RDF into a file. This method should call the {@link EnrichedRdf#finalizeRdf()}
   * method before serialization.
   *
   * @param rdf The RDF to serialize.
   * @return The serialized RDF file.
   * @throws RdfSerializationException In case there was a problem serializing this RDF.
   */
  byte[] serialize(EnrichedRdf rdf) throws RdfSerializationException;

}
