package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.extraction.iiif.IIIFInfoJson;

/**
 * The type Resource with iiif info.json.
 */
public interface IIIFResource extends Resource {

  /**
   * Gets iiif info json.
   *
   * @return the iiif info json
   */
  IIIFInfoJson getIIIFInfoJson();
}
