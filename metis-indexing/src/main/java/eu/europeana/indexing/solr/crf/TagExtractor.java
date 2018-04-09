package eu.europeana.indexing.solr.crf;

import java.util.Set;
import eu.europeana.corelib.definitions.jibx.WebResourceType;

public abstract class TagExtractor {

  public abstract Set<Integer> getFilterTags(WebResourceType webResource);

  public abstract Set<Integer> getFacetTags(WebResourceType webResource);

}
