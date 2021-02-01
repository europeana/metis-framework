package api.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.utils.EntityType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SearchValueTest {

  private static final String VALUE = "value";
  private static final String LANGUAGE = "language";
  private static final List<EntityType>  ENTITY_TYPES_LIST = new ArrayList<>();
  private static SearchValue SEARCH_VALUE;

  @BeforeEach
  void setUp(){
    ENTITY_TYPES_LIST.add(EntityType.PLACE);
    ENTITY_TYPES_LIST.add(EntityType.AGENT);
    SEARCH_VALUE = new SearchValue(VALUE,LANGUAGE, ENTITY_TYPES_LIST.get(0), ENTITY_TYPES_LIST.get(1));
  }

  @Test
  void testGetValue(){

    String result = SEARCH_VALUE.getValue();
    assertEquals(VALUE, result);

  }

  @Test
  void testSetValue(){

    String newValue = "differentValue";

    SEARCH_VALUE.setValue(newValue);

    assertEquals(newValue, SEARCH_VALUE.getValue());

  }

  @Test
  void testGetLanguage(){

    String result = SEARCH_VALUE.getLanguage();
    assertEquals(LANGUAGE, result);

  }

  @Test
  void testSetLanguage(){

    String newLanguage = "differentLanguage";

    SEARCH_VALUE.setLanguage(newLanguage);

    assertEquals(newLanguage, SEARCH_VALUE.getLanguage());

  }

  @Test
  void testGetEntityTypes(){

    List<EntityType> result = SEARCH_VALUE.getEntityTypes();
    for(EntityType type : ENTITY_TYPES_LIST){
      assertTrue(result.contains(type));
    }
  }

  @Test
  void testGetEntityTypesNull(){

    SEARCH_VALUE.setEntityTypes(null);

    List<EntityType> result = SEARCH_VALUE.getEntityTypes();
    assertNull(result);

  }

  @Test
  void testSetEntityTypes(){

    List<EntityType> differentList = new ArrayList<>();
    differentList.add(EntityType.CONCEPT);
    differentList.add(EntityType.TIMESPAN);
    for(EntityType type : ENTITY_TYPES_LIST){
      assertTrue(SEARCH_VALUE.getEntityTypes().contains(type));
    }

    SEARCH_VALUE.setEntityTypes(differentList);
    List<EntityType> result = SEARCH_VALUE.getEntityTypes();

    for(EntityType type : differentList){
      assertTrue(result.contains(type));
    }

  }

  @Test
  void testSetEntityTypesNull(){

    for(EntityType type : ENTITY_TYPES_LIST){
      assertTrue(SEARCH_VALUE.getEntityTypes().contains(type));
    }

    SEARCH_VALUE.setEntityTypes(null);

    assertNull(SEARCH_VALUE.getEntityTypes());

  }

}
