package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

public interface FieldType {

  /**
   * @return the entity type associated to this field - it is not null.
   */
  EntityType getEntityType();

  /**
   * Extract the field values set from the provided about type.
   * <p>It gets the values for the specific field and creates a set of {@link FieldValue}s</p>
   *
   * @param aboutType the about type to use
   * @return the set of field values
   */
  default <T extends AboutType> Set<FieldValue> extractFieldValuesForEnrichment(T aboutType) {
    return extractFields(aboutType).filter(content -> StringUtils.isNotEmpty(content.getString()))
        .map(FieldType::convert).collect(Collectors.toSet());
  }

  /**
   * Get a stream of values for the specific field from the provided about type.
   *
   * @param aboutType the about type to use
   * @return the stream of values
   */
  <T extends AboutType> Stream<? extends ResourceOrLiteralType> extractFields(T aboutType);

  /**
   * Convert a {@link ResourceOrLiteralType} to a {@link FieldValue}
   *
   * @param content the type to convert from
   * @return the field value
   */
  static FieldValue convert(ResourceOrLiteralType content) {
    final String language = content.getLang() == null ? null : content.getLang().getLang();
    return new FieldValue(content.getString(), language);
  }
}
