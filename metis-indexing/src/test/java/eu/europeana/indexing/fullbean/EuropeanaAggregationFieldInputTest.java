package eu.europeana.indexing.fullbean;

import static eu.europeana.indexing.fullbean.RdfToFullBeanConverter.convertList;
import static eu.europeana.indexing.fullbean.RdfToFullBeanConverter.getQualityAnnotations;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.indexing.base.IndexingTestUtils;
import eu.europeana.indexing.utils.RdfWrapper;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.convert.SerializationException;
import java.util.List;
import java.util.stream.Collectors;
import org.junit.jupiter.api.Test;

class EuropeanaAggregationFieldInputTest {

  private EuropeanaAggregationFieldInput europeanaAggregationFieldInput;

  @Test
  void apply() throws SerializationException {
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    final RdfWrapper inputRdf = new RdfWrapper(conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_rdf.xml")));
    europeanaAggregationFieldInput = new EuropeanaAggregationFieldInput(
        convertList(getQualityAnnotations(inputRdf), new QualityAnnotationFieldInput(), false));

    List<EuropeanaAggregation> aggregationList = inputRdf.getEuropeanaAggregation().stream()
                                                         .map(a -> europeanaAggregationFieldInput.apply(a))
                                                         .collect(Collectors.toList());

    assertFalse(aggregationList.isEmpty());
    assertEquals("/aggregation/europeana/277/CMC_HA_1185", aggregationList.getFirst().getAbout());
    assertEquals("/277/CMC_HA_1185", aggregationList.getFirst().getAggregatedCHO());
  }
}
