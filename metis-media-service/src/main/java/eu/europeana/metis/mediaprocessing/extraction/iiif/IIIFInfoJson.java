package eu.europeana.metis.mediaprocessing.extraction.iiif;

import java.util.Set;

/**
 * The interface Iiif info json.
 */
public interface IIIFInfoJson {

  Object getContext();

  String getId();

  int getWidth();

  int getHeight();

  SupportedFormats getSupportedFormats();

  /**
   * The image formats that are supported by this IIIF resource.
   *
   * @param recommendedFormats The preferred/recommended formats.
   * @param additionalFormats  Additional supported formats.
   */
  record SupportedFormats(Set<String> recommendedFormats, Set<String> additionalFormats) {}
}
