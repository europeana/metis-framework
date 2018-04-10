package eu.europeana.indexing.solr.crf;

import java.util.Collections;
import java.util.Set;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

public class TextTagExtractor extends TagExtractor {

  @Override
  public Set<Integer> getFilterTags(WebResourceType webResource) {
    final Integer mimeTypeCode = TechnicalFacet.MIME_TYPE.evaluateAndShift(webResource).iterator().next();
    return Collections.singleton(MediaType.TEXT.getEncodedValue() | mimeTypeCode);
  }

  @Override
  public Set<Integer> getFacetTags(WebResourceType webResource) {
    final Integer mimeTypeCode = TechnicalFacet.MIME_TYPE.evaluateAndShift(webResource).iterator().next();
    return Collections.singleton(MediaType.TEXT.getEncodedValue() | mimeTypeCode);
  }
}
