package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.corelib.definitions.jibx.RDF;

public interface EnrichedRdf {

  void enrichResource(ResourceMetadata resource);

  RDF finalizeRdf();

}
