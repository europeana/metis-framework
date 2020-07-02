package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Implementations of this interface provide a variety of deserialization options for RDF files.
 * This object can be reused multiple times, as the construction of it incurs overhead. Please note
 * that this object is thread-safe, but currently it achieves this by synchronization, meaning that
 * it is not designed for many threads to access the object simultaneously.
 */
public interface RdfDeserializer {

  /**
   * Obtain the resource entry providing the main thumbnail for media extraction from an RDF file.
   * This is the particular resource entry that is to be used to create the main thumbnail, and
   * which helps decide whether thumbnail generation is necessary for other resources. This method
   * may return null, signifying that among all resources none qualify as providing the one main
   * thumbnail.
   *
   * @param input The RDF file.
   * @return The resource entry. Can be null.
   * @throws RdfDeserializationException In case something went wrong deserializing the RDF.
   */
  RdfResourceEntry getMainThumbnailResourceForMediaExtraction(byte[] input)
          throws RdfDeserializationException;

  /**
   * Obtain the resource entry providing the main thumbnail for media extraction from an RDF file.
   * This is the particular resource entry that is to be used to create the main thumbnail, and
   * which helps decide whether thumbnail generation is necessary for other resources. This method
   * may return null, signifying that among all resources none qualify as providing the one main
   * thumbnail.
   *
   * @param inputStream The RDF file.
   * @return The resource entry. Can be null.
   * @throws RdfDeserializationException In case something went wrong deserializing the RDF.
   */
  RdfResourceEntry getMainThumbnailResourceForMediaExtraction(InputStream inputStream)
          throws RdfDeserializationException;

  /**
   * Obtain the remaining resource entries for media extraction from an RDF file. This method
   * returns all resources except the one returned by {@link #getMainThumbnailResourceForMediaExtraction(byte[])}.
   *
   * @param input The RDF file.
   * @return The list of resource entries that are subject to media extraction. Can be empty.
   * @throws RdfDeserializationException In case something went wrong deserializing the RDF.
   */
  List<RdfResourceEntry> getRemainingResourcesForMediaExtraction(byte[] input)
          throws RdfDeserializationException;

  /**
   * Obtain the remaining resource entries for media extraction from an RDF file. This method
   * returns all resources except the one returned by {@link #getMainThumbnailResourceForMediaExtraction(InputStream)}.
   *
   * @param inputStream The RDF file.
   * @return The list of resource entries that are subject to media extraction.
   * @throws RdfDeserializationException In case something went wrong deserializing the RDF.
   */
  List<RdfResourceEntry> getRemainingResourcesForMediaExtraction(InputStream inputStream)
          throws RdfDeserializationException;

  /**
   * Combines the two outputs of {@link #getMainThumbnailResourceForMediaExtraction(byte[])} and
   * {@link #getRemainingResourcesForMediaExtraction(byte[])}.
   *
   * @param input The RDF file.
   * @return The list of resources.
   * @throws RdfDeserializationException In case something goes wrong.
   * @deprecated Use the methods separately instead.
   */
  @Deprecated(forRemoval = true)
  default List<RdfResourceEntry> getResourceEntriesForMediaExtraction(byte[] input)
          throws RdfDeserializationException {
    final List<RdfResourceEntry> result = new ArrayList<>(
            getRemainingResourcesForMediaExtraction(input));
    final RdfResourceEntry mainThumbnailResource = getMainThumbnailResourceForMediaExtraction(
            input);
    if (mainThumbnailResource != null) {
      result.add(mainThumbnailResource);
    }
    return result;
  }

  /**
   * Combines the two outputs of {@link #getMainThumbnailResourceForMediaExtraction(InputStream)}
   * and {@link #getRemainingResourcesForMediaExtraction(InputStream)}.
   *
   * @param inputStream The RDF file.
   * @return The list of resources.
   * @throws RdfDeserializationException In case something goes wrong.
   * @deprecated Use the methods separately instead.
   */
  @Deprecated(forRemoval = true)
  default List<RdfResourceEntry> getResourceEntriesForMediaExtraction(InputStream inputStream)
          throws RdfDeserializationException {
    final List<RdfResourceEntry> result = new ArrayList<>(
            getRemainingResourcesForMediaExtraction(inputStream));
    final RdfResourceEntry mainThumbnailResource = getMainThumbnailResourceForMediaExtraction(
            inputStream);
    if (mainThumbnailResource != null) {
      result.add(mainThumbnailResource);
    }
    return result;
  }

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
