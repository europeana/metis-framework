package eu.europeana.indexing.fullbean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import eu.europeana.corelib.definitions.edm.entity.AbstractEdmEntity;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.indexing.base.IndexingTestUtils;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class EuropeanaAggregationFieldInputTest {

  private EuropeanaAggregationFieldInput europeanaAggregationFieldInput;

  @Test
  void apply() throws SerializationException {
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RdfWrapper inputRdf = new RdfWrapper(conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_rdf_conversion.xml")));
    europeanaAggregationFieldInput = new EuropeanaAggregationFieldInput();

    List<EuropeanaAggregation> aggregationList = inputRdf.getEuropeanaAggregation().stream()
                                                         .map(a -> europeanaAggregationFieldInput.apply(a))
                                                         .collect(Collectors.toList());

    assertFalse(aggregationList.isEmpty());
    assertEquals(Set.of("/aggregation/europeana/277/CMC_HA_1185"), aggregationList
        .stream()
        .map(AbstractEdmEntity::getAbout)
        .collect(Collectors.toSet()));
    assertEquals(Set.of("/277/CMC_HA_1185"), aggregationList
        .stream()
        .map(EuropeanaAggregation::getAggregatedCHO)
        .collect(Collectors.toSet()));
  }
}
