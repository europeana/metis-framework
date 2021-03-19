package eu.europeana.enrichment.api.internal;

import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.schema.jibx.AboutType;
import eu.europeana.metis.schema.jibx.ResourceOrLiteralType;
import java.util.Set;
import java.util.stream.Stream;

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
  Set<FieldValue> extractFieldValuesForEnrichment(AboutType aboutType);

  /**
   * Extract resources from a Proxy for enrichment
   *
   * @param proxy The proxy to use for enrichment
   * @return A list of values ready for enrichment
   */
  Set<String> extractFieldLinksForEnrichment(AboutType proxy);

  /**
   * Convert a {@link ResourceOrLiteralType} to a {@link FieldValue}
   *
   * @param content the type to convert from
   * @return the field value
   */
  default FieldValue convert(ResourceOrLiteralType content) {
    final String language = content.getLang() == null ? null : content.getLang().getLang();
    return new FieldValue(content.getString(), language);
  }

  /**
   * Get a stream of values for the specific field from the provided about type.
   *
   * @param aboutType the about type to use
   * @return the stream of values
   */
  Stream<? extends ResourceOrLiteralType> extractFields(AboutType aboutType);
}
