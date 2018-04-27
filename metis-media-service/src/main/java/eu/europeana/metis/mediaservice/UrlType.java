package eu.europeana.metis.mediaservice;

import java.util.Collection;

public enum UrlType {
  
  OBJECT, HAS_VIEW, IS_SHOWN_BY, IS_SHOWN_AT;

  static boolean shouldExtractMetadata(Collection<UrlType> resourceTypes) {
    return resourceTypes.stream().anyMatch(t -> t == UrlType.HAS_VIEW || t == UrlType.IS_SHOWN_BY);
  }
}
