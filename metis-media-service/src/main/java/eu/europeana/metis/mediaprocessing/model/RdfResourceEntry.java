package eu.europeana.metis.mediaprocessing.model;

import eu.europeana.metis.mediaprocessing.UrlType;
import java.util.Set;

public interface RdfResourceEntry {

  String getResourceUrl();

  Set<UrlType> getUrlTypes();

}
