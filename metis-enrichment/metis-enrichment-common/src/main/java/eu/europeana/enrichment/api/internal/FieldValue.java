package eu.europeana.enrichment.api.internal;

import java.util.Objects;

public final class FieldValue {

  private final String value;
  private final String language;

  public FieldValue(String value, String language) {
    this.value = value;
    this.language = language;
  }

  public String getValue() {
    return value;
  }

  public String getLanguage() {
    return language;
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    final FieldValue that = (FieldValue) o;
    return Objects.equals(value, that.value) && Objects.equals(language, that.language);
  }

  @Override
  public int hashCode() {
    return Objects.hash(value, language);
  }
}
