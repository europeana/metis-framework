package api.external;


import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.ReferenceValue;
import eu.europeana.enrichment.utils.EntityType;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class ReferenceValueTest {

  private static final String REFERENCE = "reference";
  private static final Set<EntityType> ENTITY_TYPE_SET = new HashSet<>();
  private static ReferenceValue REFERENCE_VALUE;

  @BeforeEach
  void setUp(){
    ENTITY_TYPE_SET.add(EntityType.PLACE);
    ENTITY_TYPE_SET.add(EntityType.AGENT);
    REFERENCE_VALUE = new ReferenceValue(REFERENCE, ENTITY_TYPE_SET);
  }

  @Test
  void testGetReference(){

    String result = REFERENCE_VALUE.getReference();

    assertEquals(REFERENCE, result);

  }

  @Test
  void testSetReference(){

    String newReference = "differentReference";
    assertEquals(REFERENCE, REFERENCE_VALUE.getReference());

    REFERENCE_VALUE.setReference(newReference);

    assertEquals(newReference, REFERENCE_VALUE.getReference());

  }

  @Test
  void testGetEntityTypes(){

    List<EntityType> result = REFERENCE_VALUE.getEntityTypes();
    List<EntityType> expected = new ArrayList<>(ENTITY_TYPE_SET);

    for(EntityType type : expected){
      assertTrue(result.contains(type));
    }

  }

  @Test
  void testGetEntityTypesNull(){

    REFERENCE_VALUE.setEntityTypes(null);

    List<EntityType> result = REFERENCE_VALUE.getEntityTypes();
    assertNull(result);

  }

  @Test
  void testSetEntityTypes(){

    List<EntityType> differentList = new ArrayList<>();
    differentList.add(EntityType.CONCEPT);
    differentList.add(EntityType.TIMESPAN);
    for(EntityType type : ENTITY_TYPE_SET){
      assertTrue(REFERENCE_VALUE.getEntityTypes().contains(type));
    }

    REFERENCE_VALUE.setEntityTypes(differentList);
    List<EntityType> result = REFERENCE_VALUE.getEntityTypes();

    for(EntityType type : differentList){
      assertTrue(result.contains(type));
    }

  }

  @Test
  void testSetEntityTypesNull(){

    assertArrayEquals(ENTITY_TYPE_SET.toArray(), REFERENCE_VALUE.getEntityTypes().toArray());

    REFERENCE_VALUE.setEntityTypes(null);

    assertNull(REFERENCE_VALUE.getEntityTypes());

  }

}
