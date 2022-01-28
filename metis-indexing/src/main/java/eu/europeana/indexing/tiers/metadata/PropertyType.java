package eu.europeana.indexing.tiers.metadata;

import eu.europeana.metis.schema.jibx.Alternative;
import eu.europeana.metis.schema.jibx.Coverage;
import eu.europeana.metis.schema.jibx.CurrentLocation;
import eu.europeana.metis.schema.jibx.Description;
import eu.europeana.metis.schema.jibx.Format;
import eu.europeana.metis.schema.jibx.HasPart;
import eu.europeana.metis.schema.jibx.HasType;
import eu.europeana.metis.schema.jibx.IsPartOf;
import eu.europeana.metis.schema.jibx.IsReferencedBy;
import eu.europeana.metis.schema.jibx.IsRelatedTo;
import eu.europeana.metis.schema.jibx.Medium;
import eu.europeana.metis.schema.jibx.Provenance;
import eu.europeana.metis.schema.jibx.References;
import eu.europeana.metis.schema.jibx.Relation;
import eu.europeana.metis.schema.jibx.Rights;
import eu.europeana.metis.schema.jibx.Source;
import eu.europeana.metis.schema.jibx.Spatial;
import eu.europeana.metis.schema.jibx.Subject;
import eu.europeana.metis.schema.jibx.TableOfContents;
import eu.europeana.metis.schema.jibx.Temporal;
import eu.europeana.metis.schema.jibx.Title;
import eu.europeana.metis.schema.jibx.Type;

/**
 * The property types that can be used for the language tags statistics.
 */
public enum PropertyType {
  DC_COVERAGE(Coverage.class),
  DC_DESCRIPTION(Description.class),
  DC_FORMAT(Format.class),
  DC_RELATION(Relation.class),
  DC_RIGHTS(Rights.class),
  DC_SOURCE(Source.class),
  DC_SUBJECT(Subject.class),
  DC_TITLE(Title.class),
  DC_TYPE(Type.class),
  DCTERMS_ALTERNATIVE(Alternative.class),
  DCTERMS_HAS_PART(HasPart.class),
  DCTERMS_IS_PART_OF(IsPartOf.class),
  DCTERMS_IS_REFERENCED_BY(IsReferencedBy.class),
  DCTERMS_MEDIUM(Medium.class),
  DCTERMS_PROVENANCE(Provenance.class),
  DCTERMS_REFERENCES(References.class),
  DCTERMS_SPATIAL(Spatial.class),
  DCTERMS_TABLE_OF_CONTENTS(TableOfContents.class),
  DCTERMS_TEMPORAL(Temporal.class),
  EDM_CURRENT_LOCATION(CurrentLocation.class),
  EDM_HAS_TYPE(HasType.class),
  EDM_IS_RELATED_TO(IsRelatedTo.class);

  final Class<?> typedClass;

  PropertyType(Class<?> typedClass) {
    this.typedClass = typedClass;
  }

  public Class<?> getTypedClass() {
    return typedClass;
  }
}
