package eu.europeana.normalization.dates.edtf;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.normalization.dates.DateNormalizationResult;
import eu.europeana.normalization.dates.DatesNormalizer;
import eu.europeana.normalization.dates.EdmSerializer;
import eu.europeana.normalization.dates.EdmSerializer.Dcterms;
import eu.europeana.normalization.dates.EdmSerializer.Edm;
import eu.europeana.normalization.dates.EdmSerializer.Skos;
import java.util.HashSet;
import java.util.List;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.junit.jupiter.api.Test;

public class EdmSerializerTest {

  DatesNormalizer normaliser = new DatesNormalizer();

  @Test
  void serializeDcmiPeriod() {
    DateNormalizationResult dateNormalizationResult = normaliser.normalizeDateProperty("Byzantine Period; start=0395; end=0641");
    Resource edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    assertEquals(edm.getURI(), "#0395%2F0641");
    assertEquals(edm.listProperties(Dcterms.isPartOf).toList().size(), 4);
    assertEquals("Byzantine Period", dateNormalizationResult.getEdtfDate().getLabel());
    assertEquals("Byzantine Period", edm.getProperty(Skos.prefLabel).getObject().asLiteral().getString());
    assertEquals("0395/0641", edm.getProperty(Skos.notation).getObject().asLiteral().getString());

    dateNormalizationResult = normaliser.normalizeDateProperty("name=Prehistoric Period; end=-5300");
    edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    assertEquals(edm.getURI(), "#..%2F-5300");
    assertEquals(edm.listProperties(Dcterms.isPartOf).toList().size(), 0);
    assertEquals("Prehistoric Period", dateNormalizationResult.getEdtfDate().getLabel());
    assertEquals("Prehistoric Period", edm.getProperty(Skos.prefLabel).getObject().asLiteral().getString());
    assertEquals("../-5300", edm.getProperty(Skos.notation).getObject().asLiteral().getString());
    assertEquals(edm.listProperties(Edm.begin).toList().size(), 0);
    assertEquals("-5300-12-31", edm.getProperty(Edm.end).getObject().asLiteral().getString());
  }

  @Test
  void serializeInterval() {
    DateNormalizationResult dateNormalizationResult = normaliser.normalizeDateProperty("1942-1943");
    Resource edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    assertEquals(edm.getURI(), "#1942%2F1943");
    List<Statement> partOf = edm.listProperties(Dcterms.isPartOf).toList();
    assertEquals(partOf.size(), 1);
    assertEquals("1942/1943", edm.getProperty(Skos.prefLabel).getObject().asLiteral().getString());
    assertEquals("1942/1943", edm.getProperty(Skos.notation).getObject().asLiteral().getString());

    dateNormalizationResult = normaliser.normalizeDateProperty("[1842-1943]");
    edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    partOf = edm.listProperties(Dcterms.isPartOf).toList();
    assertEquals(partOf.size(), 2);
    assertTrue(getUris(partOf).contains("http://data.europeana.eu/timespan/19"));
    assertTrue(getUris(partOf).contains("http://data.europeana.eu/timespan/20"));

    dateNormalizationResult = normaliser.normalizeDateProperty("[1801-1900]");
    edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    partOf = edm.listProperties(Dcterms.isPartOf).toList();
    assertEquals(partOf.size(), 1);

    dateNormalizationResult = normaliser.normalizeDateProperty("-0005/0200");
    edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    partOf = edm.listProperties(Dcterms.isPartOf).toList();
    assertEquals(partOf.size(), 2);
    assertTrue(getUris(partOf).contains("http://data.europeana.eu/timespan/1"));
    assertTrue(getUris(partOf).contains("http://data.europeana.eu/timespan/2"));

    dateNormalizationResult = normaliser.normalizeDateProperty("-0500/-0200");
    edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    partOf = edm.listProperties(Dcterms.isPartOf).toList();
    assertEquals(partOf.size(), 0);
  }

  @Test
  void serializeInstant() {
    DateNormalizationResult dateNormalizationResult = normaliser.normalizeDateProperty("1942-03-03?");
    Resource edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    assertEquals(edm.listProperties(Dcterms.isPartOf).toList().size(), 1);
    assertEquals("uncertain", edm.getProperty(Skos.note).getObject().asLiteral().getString());

    dateNormalizationResult = normaliser.normalizeDateProperty("0001");
    edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    assertEquals(edm.listProperties(Dcterms.isPartOf).toList().size(), 1);

    dateNormalizationResult = normaliser.normalizeDateProperty("-0001");
    edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    assertEquals(edm.listProperties(Dcterms.isPartOf).toList().size(), 0);
  }

  @Test
  void serializeNegativeLongYear() {
    DateNormalizationResult dateNormalizationResult = normaliser.normalizeDateProperty("-600000");
    Resource edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    assertEquals(edm.listProperties(Dcterms.isPartOf).toList().size(), 0);
    assertEquals("Y-600000", edm.getProperty(Edm.begin).getObject().asLiteral().getString());
    assertEquals("Y-600000", edm.getProperty(Edm.end).getObject().asLiteral().getString());
  }

  @Test
  void serializeCentury() {
    DateNormalizationResult dateNormalizationResult = normaliser.normalizeDateProperty("19XX");
    Resource edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    assertEquals(edm.listProperties(Dcterms.isPartOf).toList().size(), 1);
    assertEquals("1901-01-01", edm.getProperty(Edm.begin).getObject().asLiteral().getString());
    assertEquals("2000-12-31", edm.getProperty(Edm.end).getObject().asLiteral().getString());
  }

  @Test
  void serializeDecade() {
    DateNormalizationResult dateNormalizationResult = normaliser.normalizeDateProperty("190X");
    Resource edm = EdmSerializer.serialize(dateNormalizationResult.getEdtfDate());
    assertEquals(edm.listProperties(Dcterms.isPartOf).toList().size(), 2);
    assertEquals("1900-01-01", edm.getProperty(Edm.begin).getObject().asLiteral().getString());
    assertEquals("1909-12-31", edm.getProperty(Edm.end).getObject().asLiteral().getString());
  }

  private HashSet<String> getUris(List<Statement> stms) {
    HashSet<String> uris = new HashSet<String>();
    for (Statement st : stms) {
      uris.add(st.getObject().asResource().getURI());
    }
    return uris;
  }

}