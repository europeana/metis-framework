package eu.europeana.metis.mediaprocessing.model;

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
   * The resource URL type that is subject to media extraction and provide the main thumbnail.
   * This is a member of {@link #URL_TYPES_FOR_MEDIA_EXTRACTION}.
   */
  public static final UrlType URL_TYPE_FOR_MAIN_THUMBNAIL_RESOURCE = UrlType.OBJECT;
}
