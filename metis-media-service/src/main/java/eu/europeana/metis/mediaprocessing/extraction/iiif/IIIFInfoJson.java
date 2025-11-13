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

  record SupportedFormats(Set<String> recommendedFormats, Set<String> additionalFormats) {}
}
