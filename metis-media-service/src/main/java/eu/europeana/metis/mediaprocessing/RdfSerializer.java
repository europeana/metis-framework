package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.RdfSerializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import java.io.OutputStream;

/**
 * Implementations of this interface provide a variety of serialization options for RDF files. This
 * object can be reused multiple times, as the construction of it incurs overhead. Please note that
 * this object is thread-safe, but currently it achieves this by synchronization, meaning that it is
 * not designed for many threads to access the object simultaneously.
 * @deprecated Use {@link eu.europeana.metis.mediaprocessing.utils.ConverterUtils} and/or RdfSerializer from metis-schema library.
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

  /**
   * Serialize an RDF into a file. This method should call the {@link EnrichedRdf#finalizeRdf()}
   * method before serialization.
   *
   * @param rdf The RDF to serialize.
   * @param outputStream The output stream to which to send the serialized file.
   * @throws RdfSerializationException In case there was a problem serializing this RDF.
   */
  void serialize(EnrichedRdf rdf, OutputStream outputStream) throws RdfSerializationException;

}
