package eu.europeana.metis.mediaprocessing.model;

/**
 * Information about a generated thumbnail. See notes in {@link TemporaryResourceFile}.
 */
public interface Thumbnail extends TemporaryResourceFile {

  String getTargetName();

}
