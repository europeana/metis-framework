package eu.europeana.metis.mediaprocessing.model;

import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;

/**
 * The url types that are used to enable processing of specific resource fields in the EDM xml
 * record.
 */
public enum UrlType {

  OBJECT, HAS_VIEW, IS_SHOWN_BY, IS_SHOWN_AT;

  public static final Set<UrlType> URL_TYPES_FOR_LINK_CHECKING = EnumSet.allOf(UrlType.class);
  public static final Set<UrlType> URL_TYPES_FOR_MEDIA_EXTRACTION = EnumSet.allOf(UrlType.class);
  public static final Set<UrlType> URL_TYPES_FOR_METADATA_EXTRACTION = EnumSet
      .of(HAS_VIEW, IS_SHOWN_BY, IS_SHOWN_AT);

  public static boolean shouldExtractMetadata(Collection<UrlType> resourceTypes) {
    return resourceTypes.stream().anyMatch(URL_TYPES_FOR_METADATA_EXTRACTION::contains);
  }
}
