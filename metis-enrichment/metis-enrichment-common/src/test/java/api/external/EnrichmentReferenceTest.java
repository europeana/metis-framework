package api.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.EnrichmentReference;
import eu.europeana.enrichment.api.external.ReferenceValue;
import eu.europeana.enrichment.utils.EntityType;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnrichmentReferenceTest {
  private static EnrichmentReference ENRICHMENT_REFERENCE;
  private static List<ReferenceValue> REFERENCE_VALUES;

  @BeforeEach
  void setUp(){

    REFERENCE_VALUES = new ArrayList<>();
    REFERENCE_VALUES.add(new ReferenceValue("reference1", Set.of(EntityType.CONCEPT, EntityType.AGENT)));
    REFERENCE_VALUES.add(new ReferenceValue("reference2", Set.of(EntityType.PLACE, EntityType.TIMESPAN)));

    ENRICHMENT_REFERENCE = new EnrichmentReference(REFERENCE_VALUES.get(0), REFERENCE_VALUES.get(1));

  }

  @Test
  void testGetReferenceValues(){

    List<ReferenceValue> result = ENRICHMENT_REFERENCE.getReferenceValues();

    assertEquals(REFERENCE_VALUES.size(), result.size());

    for(ReferenceValue value: REFERENCE_VALUES){
      assertTrue(result.contains(value));
    }

  }

  @Test
  void testGetReferenceValuesNull(){
    EnrichmentReference nullEnrichmentSearch = new EnrichmentReference();

    List<ReferenceValue> result = nullEnrichmentSearch.getReferenceValues();
    assertNull(result);

  }

  @Test
  void testSetReferenceValues(){

    ReferenceValue toCompare = new ReferenceValue("reference3", Set.of(EntityType.ORGANIZATION));

    List<ReferenceValue> differentList = new ArrayList<>();
    differentList.add(toCompare);

    ENRICHMENT_REFERENCE.setReferenceValues(differentList);

    assertEquals(differentList.size(), ENRICHMENT_REFERENCE.getReferenceValues().size());
    assertEquals(toCompare, ENRICHMENT_REFERENCE.getReferenceValues().get(0));

  }

  @Test
  void testSetReferenceValuesNull(){
    ENRICHMENT_REFERENCE.setReferenceValues(null);

    List<ReferenceValue> result = ENRICHMENT_REFERENCE.getReferenceValues();
    assertNull(result);

  }

}
