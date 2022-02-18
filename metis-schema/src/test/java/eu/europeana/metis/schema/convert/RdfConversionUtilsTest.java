package eu.europeana.metis.schema.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import eu.europeana.metis.schema.jibx.AgentType;
import eu.europeana.metis.schema.jibx.Alternative;
import eu.europeana.metis.schema.jibx.Concept;
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
import eu.europeana.metis.schema.jibx.PlaceType;
import eu.europeana.metis.schema.jibx.Provenance;
import eu.europeana.metis.schema.jibx.References;
import eu.europeana.metis.schema.jibx.Relation;
import eu.europeana.metis.schema.jibx.Rights;
import eu.europeana.metis.schema.jibx.Source;
import eu.europeana.metis.schema.jibx.Spatial;
import eu.europeana.metis.schema.jibx.Subject;
import eu.europeana.metis.schema.jibx.TableOfContents;
import eu.europeana.metis.schema.jibx.Temporal;
import eu.europeana.metis.schema.jibx.TimeSpanType;
import eu.europeana.metis.schema.jibx.Title;
import eu.europeana.metis.schema.jibx.Type;
import org.junit.jupiter.api.Test;

class RdfConversionUtilsTest {

  @Test
  void failRdfConversionUtilsInitialization() {
    //Force failure
    assertThrows(IllegalStateException.class, () -> new RdfConversionUtils(RdfConversionUtils.class));
  }

  @Test
  void getQualifiedElementNameForClass_ContextualClasses() {
    //Check contextual classes
    final RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();
    assertEquals("edm:AgentType", rdfConversionUtils.getQualifiedElementNameForClass(AgentType.class));
    assertEquals("edm:TimeSpanType", rdfConversionUtils.getQualifiedElementNameForClass(TimeSpanType.class));
    assertEquals("edm:PlaceType", rdfConversionUtils.getQualifiedElementNameForClass(PlaceType.class));
    assertEquals("skos:Concept", rdfConversionUtils.getQualifiedElementNameForClass(Concept.class));
  }

  @Test
  void getQualifiedElementNameForClass_Dc() {
    //Check dc elements
    final RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();
    assertEquals("dc:coverage", rdfConversionUtils.getQualifiedElementNameForClass(Coverage.class));
    assertEquals("dc:description", rdfConversionUtils.getQualifiedElementNameForClass(Description.class));
    assertEquals("dc:format", rdfConversionUtils.getQualifiedElementNameForClass(Format.class));
    assertEquals("dc:relation", rdfConversionUtils.getQualifiedElementNameForClass(Relation.class));
    assertEquals("dc:rights", rdfConversionUtils.getQualifiedElementNameForClass(Rights.class));
    assertEquals("dc:source", rdfConversionUtils.getQualifiedElementNameForClass(Source.class));
    assertEquals("dc:subject", rdfConversionUtils.getQualifiedElementNameForClass(Subject.class));
    assertEquals("dc:title", rdfConversionUtils.getQualifiedElementNameForClass(Title.class));
    assertEquals("dc:type", rdfConversionUtils.getQualifiedElementNameForClass(Type.class));
  }

  @Test
  void getQualifiedElementNameForClass_Dcterms() {
    //Check dcterms elements
    final RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();
    assertEquals("dcterms:alternative", rdfConversionUtils.getQualifiedElementNameForClass(Alternative.class));
    assertEquals("dcterms:hasPart", rdfConversionUtils.getQualifiedElementNameForClass(HasPart.class));
    assertEquals("dcterms:isPartOf", rdfConversionUtils.getQualifiedElementNameForClass(IsPartOf.class));
    assertEquals("dcterms:isReferencedBy", rdfConversionUtils.getQualifiedElementNameForClass(IsReferencedBy.class));
    assertEquals("dcterms:medium", rdfConversionUtils.getQualifiedElementNameForClass(Medium.class));
    assertEquals("dcterms:provenance", rdfConversionUtils.getQualifiedElementNameForClass(Provenance.class));
    assertEquals("dcterms:references", rdfConversionUtils.getQualifiedElementNameForClass(References.class));
    assertEquals("dcterms:spatial", rdfConversionUtils.getQualifiedElementNameForClass(Spatial.class));
    assertEquals("dcterms:tableOfContents", rdfConversionUtils.getQualifiedElementNameForClass(TableOfContents.class));
    assertEquals("dcterms:temporal", rdfConversionUtils.getQualifiedElementNameForClass(Temporal.class));
  }

  @Test
  void getQualifiedElementNameForClass_Edm() {
    //Check edm elements
    final RdfConversionUtils rdfConversionUtils = new RdfConversionUtils();
    assertEquals("edm:currentLocation", rdfConversionUtils.getQualifiedElementNameForClass(CurrentLocation.class));
    assertEquals("edm:hasType", rdfConversionUtils.getQualifiedElementNameForClass(HasType.class));
    assertEquals("edm:isRelatedTo", rdfConversionUtils.getQualifiedElementNameForClass(IsRelatedTo.class));
  }
}