package eu.europeana.enrichment.api.internal;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Set;
import org.junit.jupiter.api.Test;

class ReferenceTermContextTest {

  @Test
  void createFromString() {
    ReferenceTermContext referenceTermContext = ReferenceTermContext.createFromString("http://reference", Set.of(ProxyFieldType.DCTERMS_SPATIAL));
    assertEquals("http://reference", referenceTermContext.getReferenceAsString());
    assertEquals(1, referenceTermContext.getFieldTypes().size());
    assertEquals(ProxyFieldType.DCTERMS_SPATIAL, referenceTermContext.getFieldTypes().iterator().next());
  }

  @Test
  void referenceEquals() {
    ReferenceTermContext referenceTermContext = ReferenceTermContext.createFromString("http://reference", Set.of(ProxyFieldType.DCTERMS_SPATIAL));
    ReferenceTermContext referenceTermContext2 = ReferenceTermContext.createFromString("http://reference", Set.of(ProxyFieldType.DCTERMS_SPATIAL));
    assertEquals(referenceTermContext, referenceTermContext2);
  }

  @Test
  void getCandidateTypes() {
    ReferenceTermContext referenceTermContext = ReferenceTermContext.createFromString("http://reference", Set.of(ProxyFieldType.DCTERMS_SPATIAL));
    assertEquals(1, referenceTermContext.getCandidateTypes().size());
    assertEquals(ProxyFieldType.DCTERMS_SPATIAL.getEntityType(), referenceTermContext.getCandidateTypes().iterator().next());
    assertEquals("http://reference", referenceTermContext.getReferenceAsString());
    assertEquals(1, referenceTermContext.getFieldTypes().size());
    assertEquals(ProxyFieldType.DCTERMS_SPATIAL, referenceTermContext.getFieldTypes().iterator().next());
    assertEquals("http://reference", referenceTermContext.getReferenceAsString());
    assertEquals(1, referenceTermContext.getCandidateTypes().size());
    assertEquals(ProxyFieldType.DCTERMS_SPATIAL.getEntityType(), referenceTermContext.getCandidateTypes().iterator().next());
  }

}
