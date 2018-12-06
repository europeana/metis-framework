package eu.europeana.metis.mediaprocessing.model;

/**
 * Information about a generated thumbnail. See notes in {@link TemporaryFile}.
 */
public interface Thumbnail extends TemporaryFile {

  String getTargetName();

}
