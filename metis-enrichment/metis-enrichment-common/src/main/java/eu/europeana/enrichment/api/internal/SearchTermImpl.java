package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import java.util.Collections;
import java.util.Objects;
import java.util.Set;

/**
 * This class is a complete but minimal implementation of {@link SearchTerm}.
 */
public class SearchTermImpl extends AbstractSearchTerm {

  private final Set<EntityType> entityTypes;

  public SearchTermImpl(String textValue, String language, Set<EntityType> entityTypes) {
    super(textValue, language);
    this.entityTypes = Set.copyOf(entityTypes);
  }

  @Override
  public Set<EntityType> getCandidateTypes() {
    return Collections.unmodifiableSet(entityTypes);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SearchTermImpl that = (SearchTermImpl) o;
    return Objects.equals(getTextValue(), that.getTextValue()) &&
            Objects.equals(getLanguage(), that.getLanguage()) &&
            Objects.equals(getCandidateTypes(), that.getCandidateTypes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTextValue(), getLanguage(), getCandidateTypes());
  }

  @Override
  public String toString() {
    return "SearchTermImpl{" +
            "text=" + getTextValue() + '}';
  }
}
