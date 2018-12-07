package eu.europeana.metis.mediaprocessing.model;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class RdfResourceEntryImpl implements RdfResourceEntry {

  private final String resourceUrl;
  private final Set<UrlType> urlTypes;

  public RdfResourceEntryImpl(String resourceUrl, List<UrlType> urlTypes) {
    this.resourceUrl = resourceUrl;
    this.urlTypes = new HashSet<>(urlTypes);
  }

  @Override
  public String getResourceUrl() {
    return resourceUrl;
  }

  @Override
  public Set<UrlType> getUrlTypes() {
    return Collections.unmodifiableSet(urlTypes);
  }
}
