package eu.europeana.indexing.tiers.metadata;

import static org.junit.jupiter.api.Assertions.assertEquals;

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
import org.junit.jupiter.api.Test;

class PropertyTypeTest {

  @Test
  void checkValues() {
    assertEquals(22, PropertyType.values().length);
    assertEquals(Coverage.class, PropertyType.DC_COVERAGE.getTypedClass());
    assertEquals(Description.class, PropertyType.DC_DESCRIPTION.getTypedClass());
    assertEquals(Format.class, PropertyType.DC_FORMAT.getTypedClass());
    assertEquals(Relation.class, PropertyType.DC_RELATION.getTypedClass());
    assertEquals(Rights.class, PropertyType.DC_RIGHTS.getTypedClass());
    assertEquals(Source.class, PropertyType.DC_SOURCE.getTypedClass());
    assertEquals(Subject.class, PropertyType.DC_SUBJECT.getTypedClass());
    assertEquals(Title.class, PropertyType.DC_TITLE.getTypedClass());
    assertEquals(Type.class, PropertyType.DC_TYPE.getTypedClass());
    assertEquals(Alternative.class, PropertyType.DCTERMS_ALTERNATIVE.getTypedClass());
    assertEquals(HasPart.class, PropertyType.DCTERMS_HAS_PART.getTypedClass());
    assertEquals(IsPartOf.class, PropertyType.DCTERMS_IS_PART_OF.getTypedClass());
    assertEquals(IsReferencedBy.class, PropertyType.DCTERMS_IS_REFERENCED_BY.getTypedClass());
    assertEquals(Medium.class, PropertyType.DCTERMS_MEDIUM.getTypedClass());
    assertEquals(Provenance.class, PropertyType.DCTERMS_PROVENANCE.getTypedClass());
    assertEquals(References.class, PropertyType.DCTERMS_REFERENCES.getTypedClass());
    assertEquals(Spatial.class, PropertyType.DCTERMS_SPATIAL.getTypedClass());
    assertEquals(TableOfContents.class, PropertyType.DCTERMS_TABLE_OF_CONTENTS.getTypedClass());
    assertEquals(Temporal.class, PropertyType.DCTERMS_TEMPORAL.getTypedClass());
    assertEquals(CurrentLocation.class, PropertyType.EDM_CURRENT_LOCATION.getTypedClass());
    assertEquals(HasType.class, PropertyType.EDM_HAS_TYPE.getTypedClass());
    assertEquals(IsRelatedTo.class, PropertyType.EDM_IS_RELATED_TO.getTypedClass());
  }

}