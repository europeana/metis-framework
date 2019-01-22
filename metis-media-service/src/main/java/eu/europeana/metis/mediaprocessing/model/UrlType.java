package eu.europeana.metis.mediaprocessing.model;

import java.util.Collection;
import java.util.Collections;
import java.util.EnumSet;
import java.util.Set;

/**
 * The resource reference types that are used in RDF files to reference resources. This list is not
 * complete: it only contains those types that are considered for media processing.
 */
public enum UrlType {

  OBJECT, HAS_VIEW, IS_SHOWN_BY, IS_SHOWN_AT;

  /**
   * The resource URL types that are subject to link checking.
   **/
  public static final Set<UrlType> URL_TYPES_FOR_LINK_CHECKING = Collections
      .unmodifiableSet(EnumSet.allOf(UrlType.class));

  /**
   * The resource URL types that are subject to media extraction.
   **/
  public static final Set<UrlType> URL_TYPES_FOR_MEDIA_EXTRACTION = Collections
      .unmodifiableSet(EnumSet.allOf(UrlType.class));

  /**
   * The resource URL types that are subject to metadata extraction (as opposed to thumbnail
   * extraction).
   **/
  public static final Set<UrlType> URL_TYPES_FOR_METADATA_EXTRACTION = Collections
      .unmodifiableSet(EnumSet.of(HAS_VIEW, IS_SHOWN_BY, IS_SHOWN_AT));

  /**
   * Convenience method that checks whether any of the given types would make a resource eligible
   * for metadata extraction.
   *
   * @param resourceTypes The resource URL types against which to determine eligibility
   * @return Whether the resource is eligible for metadata extraction.
   */
  public static boolean shouldExtractMetadata(Collection<UrlType> resourceTypes) {
    return resourceTypes.stream().anyMatch(URL_TYPES_FOR_METADATA_EXTRACTION::contains);
  }
}
