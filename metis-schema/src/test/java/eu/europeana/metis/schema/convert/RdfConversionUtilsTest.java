package eu.europeana.metis.schema.convert;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
import eu.europeana.metis.schema.jibx.RDF;
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
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.JiBXException;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

class RdfConversionUtilsTest {

  @Test
  @Disabled
  void initializeRdfConversionUtils() {
    try (MockedStatic<BindingDirectory> bindingDirectoryMockedStatic = Mockito.mockStatic(BindingDirectory.class)) {
      bindingDirectoryMockedStatic.when(() -> BindingDirectory.getFactory(RDF.class)).thenThrow(JiBXException.class);
      try {
        RdfConversionUtils.initializeStaticComponents();
      } catch (Throwable throwable) {
        //Check for two possibilities because based on the execution order of the tests this can throw a different exception
        assertTrue(throwable instanceof ExceptionInInitializerError || throwable instanceof IllegalStateException);
        assertTrue(throwable.getCause() instanceof IllegalStateException || throwable.getCause() instanceof JiBXException);
      }
    }
  }

  @Test
  void getQualifiedElementNameForClass_ContextualClasses() {
    //Check contextual classes
    assertEquals("edm:AgentType", RdfConversionUtils.getQualifiedElementNameForClass(AgentType.class));
    assertEquals("edm:TimeSpanType", RdfConversionUtils.getQualifiedElementNameForClass(TimeSpanType.class));
    assertEquals("edm:PlaceType", RdfConversionUtils.getQualifiedElementNameForClass(PlaceType.class));
    assertEquals("skos:Concept", RdfConversionUtils.getQualifiedElementNameForClass(Concept.class));
  }

  @Test
  void getQualifiedElementNameForClass_Dc() {
    //Check dc elements
    assertEquals("dc:coverage", RdfConversionUtils.getQualifiedElementNameForClass(Coverage.class));
    assertEquals("dc:description", RdfConversionUtils.getQualifiedElementNameForClass(Description.class));
    assertEquals("dc:format", RdfConversionUtils.getQualifiedElementNameForClass(Format.class));
    assertEquals("dc:relation", RdfConversionUtils.getQualifiedElementNameForClass(Relation.class));
    assertEquals("dc:rights", RdfConversionUtils.getQualifiedElementNameForClass(Rights.class));
    assertEquals("dc:source", RdfConversionUtils.getQualifiedElementNameForClass(Source.class));
    assertEquals("dc:subject", RdfConversionUtils.getQualifiedElementNameForClass(Subject.class));
    assertEquals("dc:title", RdfConversionUtils.getQualifiedElementNameForClass(Title.class));
    assertEquals("dc:type", RdfConversionUtils.getQualifiedElementNameForClass(Type.class));
  }

  @Test
  void getQualifiedElementNameForClass_Dcterms() {
    //Check dcterms elements
    assertEquals("dcterms:alternative", RdfConversionUtils.getQualifiedElementNameForClass(Alternative.class));
    assertEquals("dcterms:hasPart", RdfConversionUtils.getQualifiedElementNameForClass(HasPart.class));
    assertEquals("dcterms:isPartOf", RdfConversionUtils.getQualifiedElementNameForClass(IsPartOf.class));
    assertEquals("dcterms:isReferencedBy", RdfConversionUtils.getQualifiedElementNameForClass(IsReferencedBy.class));
    assertEquals("dcterms:medium", RdfConversionUtils.getQualifiedElementNameForClass(Medium.class));
    assertEquals("dcterms:provenance", RdfConversionUtils.getQualifiedElementNameForClass(Provenance.class));
    assertEquals("dcterms:references", RdfConversionUtils.getQualifiedElementNameForClass(References.class));
    assertEquals("dcterms:spatial", RdfConversionUtils.getQualifiedElementNameForClass(Spatial.class));
    assertEquals("dcterms:tableOfContents", RdfConversionUtils.getQualifiedElementNameForClass(TableOfContents.class));
    assertEquals("dcterms:temporal", RdfConversionUtils.getQualifiedElementNameForClass(Temporal.class));
  }

  @Test
  void getQualifiedElementNameForClass_Edm() {
    //Check edm elements
    assertEquals("edm:currentLocation", RdfConversionUtils.getQualifiedElementNameForClass(CurrentLocation.class));
    assertEquals("edm:hasType", RdfConversionUtils.getQualifiedElementNameForClass(HasType.class));
    assertEquals("edm:isRelatedTo", RdfConversionUtils.getQualifiedElementNameForClass(IsRelatedTo.class));
  }
}