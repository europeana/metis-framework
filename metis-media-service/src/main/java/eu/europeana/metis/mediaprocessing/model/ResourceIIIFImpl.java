package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.extraction.iiif.IIIFInfoJson;
import java.net.URI;

/**
 * The type Resource with iiif info.json.
 */
public class ResourceIIIFImpl extends ResourceImpl {

  private final IIIFInfoJson iiifInfoJson;

  /**
   * Constructor.
   *
   * @param rdfResourceEntry The resource entry for which this file contains the content.
   * @param providedMimeType The mime type of this content, as provided by the source. Can be null if the source didn't specify a
   * mime type.
   * @param providedFileSize The file size of this content, as provided by the source. Can be null if the source didn't specify a
   * file size.
   * @param actualLocation The actual location where the resource was obtained (as opposed from the resource URL given by
   * {@link ResourceImpl#getResourceUrl()}).
   * @param iiifInfoJson the iiif info json of the iiif resource
   */
  public ResourceIIIFImpl(RdfResourceEntry rdfResourceEntry, String providedMimeType, Long providedFileSize,
      URI actualLocation, IIIFInfoJson iiifInfoJson) {
    super(rdfResourceEntry, providedMimeType, providedFileSize, actualLocation);
    this.iiifInfoJson = iiifInfoJson;
  }

  /**
   * Gets iiif info json.
   *
   * @return the iiif info json
   */
  public IIIFInfoJson getIiifInfoJson() {
    return iiifInfoJson;
  }
}
