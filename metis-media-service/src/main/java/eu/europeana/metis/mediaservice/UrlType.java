package eu.europeana.metis.mediaservice;

import java.util.Arrays;
import java.util.Collection;

/**
 * The url types that are used to enable processing of specific resource fields in the EDM xml
 * record.
 */
public enum UrlType {

  OBJECT, HAS_VIEW, IS_SHOWN_BY, IS_SHOWN_AT;

  public static final Collection<UrlType> URL_TYPES_FOR_LINK_CHECKING =
      Arrays.asList(UrlType.values());
  public static final Collection<UrlType> URL_TYPES_FOR_METADATA_EXTRACTION =
      Arrays.asList(UrlType.OBJECT, UrlType.HAS_VIEW, UrlType.IS_SHOWN_BY, UrlType.IS_SHOWN_AT);

  static boolean shouldExtractMetadata(Collection<UrlType> resourceTypes) {
    return resourceTypes.stream().anyMatch(
        t -> t == UrlType.HAS_VIEW || t == UrlType.IS_SHOWN_BY || t == UrlType.IS_SHOWN_AT);
  }
}
