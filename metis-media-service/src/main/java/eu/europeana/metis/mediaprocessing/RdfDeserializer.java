package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import java.io.InputStream;
import java.util.List;

/**
 * Implementations of this interface provide a variety of deserialization options for RDF files.
 * This object can be reused multiple times, as the construction of it incurs overhead. Please note
 * that this object is not guaranteed to be thread-safe. Access to this object should be from one
 * thread only, or synchronized/locked.
 */
public interface RdfDeserializer {

  /**
   * Obtain the resource entries for media extraction from an RDF file.
   *
   * @param input The RDF file.
   * @return The list of resource entries that are subject to media extraction.
   * @throws RdfDeserializationException In case something went wrong deserializing the RDF.
   */
  List<RdfResourceEntry> getResourceEntriesForMediaExtraction(byte[] input)
      throws RdfDeserializationException;

  /**
   * Obtain the resource entries for media extraction from an RDF file.
   *
   * @param inputStream The RDF file.
   * @return The list of resource entries that are subject to media extraction.
   * @throws RdfDeserializationException In case something went wrong deserializing the RDF.
   */
  List<RdfResourceEntry> getResourceEntriesForMediaExtraction(InputStream inputStream)
      throws RdfDeserializationException;

  /**
   * Obtain the resource entries for link checking from an RDF file.
   *
   * @param input The RDF file.
   * @return The list of resource entries that are subject to link checking.
   * @throws RdfDeserializationException In case something went wrong deserializing the RDF.
   */
  List<String> getResourceEntriesForLinkChecking(byte[] input) throws RdfDeserializationException;

  /**
   * Obtain the resource entries for link checking from an RDF file.
   *
   * @param inputStream The RDF file.
   * @return The list of resource entries that are subject to link checking.
   * @throws RdfDeserializationException In case something went wrong deserializing the RDF.
   */
  List<String> getResourceEntriesForLinkChecking(InputStream inputStream)
      throws RdfDeserializationException;

  /**
   * Deserialize the RDF into an object that can be used for adding extracted media metadata.
   *
   * @param input The RDF file.
   * @return The RDF object to be used for adding extracted media metadata.
   * @throws RdfDeserializationException In case something went wrong deserializing the RDF.
   */
  EnrichedRdf getRdfForResourceEnriching(byte[] input) throws RdfDeserializationException;

  /**
   * Deserialize the RDF into an object that can be used for adding extracted media metadata.
   *
   * @param inputStream The RDF file.
   * @return The RDF object to be used for adding extracted media metadata.
   * @throws RdfDeserializationException In case something went wrong deserializing the RDF.
   */
  EnrichedRdf getRdfForResourceEnriching(InputStream inputStream)
      throws RdfDeserializationException;

}
