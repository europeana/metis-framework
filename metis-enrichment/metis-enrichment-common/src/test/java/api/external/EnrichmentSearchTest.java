package api.external;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.enrichment.api.external.EnrichmentSearch;
import eu.europeana.enrichment.api.external.SearchValue;
import eu.europeana.enrichment.utils.EntityType;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class EnrichmentSearchTest {

  private static EnrichmentSearch ENRICHMENT_SEARCH;
  private static List<SearchValue> SEARCH_VALUES;

  @BeforeEach
  void setUp(){

    SEARCH_VALUES = new ArrayList<>();
    SEARCH_VALUES.add(new SearchValue("value1", "lang1", EntityType.CONCEPT, EntityType.AGENT));
    SEARCH_VALUES.add(new SearchValue("value2", "lang2", EntityType.PLACE, EntityType.TIMESPAN));

    ENRICHMENT_SEARCH = new EnrichmentSearch(SEARCH_VALUES.get(0), SEARCH_VALUES.get(1));

  }

  @Test
  void testGetSearchValues(){

    List<SearchValue> result = ENRICHMENT_SEARCH.getSearchValues();

    assertEquals(SEARCH_VALUES.size(), result.size());

    for(SearchValue value: SEARCH_VALUES){
      assertTrue(result.contains(value));
    }

  }

  @Test
  void testGetSearchValuesNull(){
    EnrichmentSearch nullEnrichmentSearch = new EnrichmentSearch();

    List<SearchValue> result = nullEnrichmentSearch.getSearchValues();
    assertNull(result);

  }

  @Test
  void testSetSearchValues(){

    SearchValue toCompare = new SearchValue("value3", "lang3", EntityType.ORGANIZATION);

    List<SearchValue> differentList = new ArrayList<>();
    differentList.add(toCompare);

    ENRICHMENT_SEARCH.setSearchValues(differentList);

    assertEquals(differentList.size(), ENRICHMENT_SEARCH.getSearchValues().size());
    assertEquals(toCompare, ENRICHMENT_SEARCH.getSearchValues().get(0));

  }

  @Test
  void testSetSearchValuesNull(){
    ENRICHMENT_SEARCH.setSearchValues(null);

    List<SearchValue> result = ENRICHMENT_SEARCH.getSearchValues();
    assertNull(result);

  }

}
