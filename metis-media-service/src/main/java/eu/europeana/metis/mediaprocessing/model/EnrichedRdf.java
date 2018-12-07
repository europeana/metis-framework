package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.corelib.definitions.jibx.RDF;

/**
 * Objects implementing this class represent RDF files that can be enriched with extracted resource
 * metadata. There is also a method for post-processing the RDF file, which should be called before
 * using the RDF in this object.
 */
public interface EnrichedRdf {

  /**
   * Enrich the RDF with extracted resource metadata.
   *
   * @param resource The resource metadata.
   */
  void enrichResource(ResourceMetadata resource);

  /**
   * Finalizes (post-processes) the RDF before giving it out.
   *
   * @return The RDF.
   */
  RDF finalizeRdf();

}
