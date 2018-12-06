package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.corelib.definitions.jibx.RDF;

public interface EnrichedRdf {

  public void enrichResource(ResourceMetadata resource);

  public RDF finalizeRdf();

}
