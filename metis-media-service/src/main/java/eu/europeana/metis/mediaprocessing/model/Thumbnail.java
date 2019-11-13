package eu.europeana.metis.mediaprocessing.model;

/**
 * This interface contains a representation of a thumbnail generated for a given resource. Please
 * see {@link ResourceRelatedFile} for more information.
 */
public interface Thumbnail extends ResourceRelatedFile {

  /**
   * @return The mime type of the thumbnail.
   */
  String getMimeType();

  /**
   * @return The unique (target) name of the thumbnail by which it is known.
   */
  String getTargetName();

}
