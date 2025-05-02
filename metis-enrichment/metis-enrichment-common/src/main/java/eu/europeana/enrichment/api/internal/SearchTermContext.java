package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.commons.lang3.StringUtils;

/**
 * This is an implementation of {@link SearchTerm} that provides context in the sense that it is
 * aware of the field type(s) in which the search term was found.
 */
public class SearchTermContext extends AbstractSearchTerm implements TermContext {

  private final Set<FieldType<? extends AboutType>> fieldTypes;

  public <T extends AboutType> SearchTermContext(String textValue, String language,
      Set<FieldType<T>> fieldTypes) {
    super(textValue, language);
    this.fieldTypes = Set.copyOf(fieldTypes);
  }

  @Override
  public Set<EntityType> getCandidateTypes() {
    return fieldTypes.stream().map(FieldType::getEntityType).collect(Collectors.toSet());
  }

  @Override
  public Set<FieldType<? extends AboutType>> getFieldTypes() {
    return fieldTypes;
  }

  @Override
  public boolean valueEquals(ResourceOrLiteralType resourceOrLiteralType) {
    boolean areEqual = false;
    //Check literal values
    if (resourceOrLiteralType.getString() != null && resourceOrLiteralType.getString()
        .equals(this.getTextValue())) {
      //Check if both languages are blank
      if ((resourceOrLiteralType.getLang() == null || StringUtils
          .isBlank(resourceOrLiteralType.getLang().getLang())) && StringUtils
          .isBlank(this.getLanguage())) {
        areEqual = true;
      } else if (resourceOrLiteralType.getLang() != null
          && resourceOrLiteralType.getLang().getLang() != null) {
        //If not blank, check language equality
        areEqual = resourceOrLiteralType.getLang().getLang().equals(this.getLanguage());
      }
    }
    return areEqual;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final SearchTermContext that = (SearchTermContext) o;
    return Objects.equals(getTextValue(), that.getTextValue()) && Objects
        .equals(getLanguage(), that.getLanguage()) && Objects
        .equals(getFieldTypes(), that.getFieldTypes());
  }

  @Override
  public int hashCode() {
    return Objects.hash(getTextValue(), getLanguage(), getFieldTypes());
  }
}
