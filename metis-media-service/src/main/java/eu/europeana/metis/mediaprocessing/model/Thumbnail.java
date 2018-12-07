package eu.europeana.metis.mediaprocessing.model;

/**
 * This class contains a representation of a thumbnail generated for a given resource. Please see
 * {@link ResourceFile} for more information.
 */
public interface Thumbnail extends ResourceFile {

  /**
   * @return The unique (target) name of the thumbnail by which it is known.
   */
  String getTargetName();

}
