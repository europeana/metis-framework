package eu.europeana.indexing.solr.crf;

import java.util.Set;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import eu.europeana.crf_faketags.extractor.CommonTagExtractor;

public abstract class TagExtractor {

  private final String mimeType;

  public TagExtractor(String mimeType) {
    this.mimeType = mimeType;
  }

  public int getMimeTypeCode() {
    return CommonTagExtractor.getMimeTypeCode(mimeType);
  }
  
  public String getMimeType() {
    return mimeType;
  }

  public abstract Set<Integer> getFilterTags(WebResourceType webResource);

  public abstract Set<Integer> getFacetTags(WebResourceType webResource);

}
