package eu.europeana.metis.mediaprocessing.extraction.iiif;

/**
 * The interface Iiif info json.
 */
public interface IIIFInfoJson {

  Object getContext();

  String getId();

  int getWidth();

  int getHeight();

}
