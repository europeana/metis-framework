package eu.europeana.indexing.solr.crf;

import java.util.Collections;
import java.util.Set;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.crf_faketags.extractor.MediaTypeEncoding;
import eu.europeana.crf_faketags.extractor.TagEncoding;

public class TextTagExtractor extends TagExtractor {

  public TextTagExtractor(String mimeType) {
    super(mimeType);
  }

  @Override
  public Set<Integer> getFilterTags(WebResourceType webResource) {
    return Collections.singleton(MediaTypeEncoding.TEXT.getEncodedValue()
        | (getMimeTypeCode() << TagEncoding.MIME_TYPE.getBitPos()));
  }

  @Override
  public Set<Integer> getFacetTags(WebResourceType webResource) {
    return Collections.singleton(MediaTypeEncoding.TEXT.getEncodedValue()
        | (getMimeTypeCode() << TagEncoding.MIME_TYPE.getBitPos()));
  }
}
