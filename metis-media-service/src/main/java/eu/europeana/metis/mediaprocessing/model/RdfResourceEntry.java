package eu.europeana.metis.mediaprocessing.model;

import java.io.Serializable;
import java.util.Set;

public interface RdfResourceEntry extends Serializable {

  String getResourceUrl();

  Set<UrlType> getUrlTypes();

}
