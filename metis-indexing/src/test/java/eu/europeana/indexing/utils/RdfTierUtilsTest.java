package eu.europeana.indexing.utils;

import static eu.europeana.indexing.utils.RdfTier.CONTENT_TIER_1;
import static eu.europeana.indexing.utils.RdfTier.CONTENT_TIER_2;
import static eu.europeana.indexing.utils.RdfTier.CONTENT_TIER_4;
import static eu.europeana.indexing.utils.RdfTier.CONTENT_TIER_BASE_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.corelib.solr.entity.QualityAnnotationImpl;
import eu.europeana.indexing.base.IndexingTestUtils;
import eu.europeana.indexing.tiers.model.MediaTier;
import eu.europeana.metis.schema.convert.RdfConversionUtils;
import eu.europeana.metis.schema.jibx.Aggregation;
import eu.europeana.metis.schema.jibx.EuropeanaAggregationType;
import eu.europeana.metis.schema.jibx.HasQualityAnnotation;
import eu.europeana.metis.schema.jibx.RDF;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;
import org.bson.types.ObjectId;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;

class RdfTierUtilsTest {

  private RDF inputRdf;

  static <T> @NotNull List<HasQualityAnnotation> getHasQualityAnnotations(
      Stream<List<HasQualityAnnotation>> aggregationList) {
    return aggregationList
        .filter(Objects::nonNull)
        .flatMap(Collection::stream)
        .filter(hasQualityAnnotation -> hasQualityAnnotation
            .getQualityAnnotation()
            .getHasBody()
            .getResource()
            .startsWith(CONTENT_TIER_BASE_URI))
        .toList();
  }

  <T> void assertContentTier(String tierUri, List<T> aggregationList,
      Function<T, List<HasQualityAnnotation>> qualityAnnotationSupplier) {
    List<HasQualityAnnotation> qualityAnnotationList = getHasQualityAnnotations(
        aggregationList
            .stream()
            .map(qualityAnnotationSupplier));
    assertEquals(tierUri, qualityAnnotationList.getFirst()
                                               .getQualityAnnotation()
                                               .getHasBody()
                                               .getResource());
  }

  <T> void assertNoContentTier(List<T> aggregationList,
      Function<T, List<HasQualityAnnotation>> qualityAnnotationSupplier) {
    List<HasQualityAnnotation> qualityAnnotationList = getHasQualityAnnotations(
        aggregationList
            .stream()
            .map(qualityAnnotationSupplier));
    assertEquals(0, qualityAnnotationList.size());
  }

  void setUpRdfWithTierCalculated() throws Exception {
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_rdf_with_tier_calculated.xml"));
  }

  void setUpRdfWithoutTierCalculated() throws Exception {
    final RdfConversionUtils conversionUtils = new RdfConversionUtils();
    inputRdf = conversionUtils.convertStringToRdf(
        IndexingTestUtils.getResourceFileContent("europeana_record_tier_calculation_rdf.xml"));
  }

  @Test
  void getTier() {
    // given
    final QualityAnnotationImpl qualityAnnotation = new QualityAnnotationImpl();
    qualityAnnotation.setId(new ObjectId("5f9330393434343434343434"));
    qualityAnnotation.setBody("http://www.europeana.eu/schemas/epf/contentTier1");
    qualityAnnotation.setAbout("/aggregation/provider/2022502/_KAMRA_356338");
    qualityAnnotation.setTarget(new String[]{"/aggregation/provider/2022502/_KAMRA_356338"});

    // when
    RdfTier rdfTier = RdfTierUtils.getTier(qualityAnnotation);

    // then
    assertEquals(RdfTier.CONTENT_TIER_1, rdfTier);
  }

  @Test
  void setTierOverwrite() throws Exception {
    // given
    setUpRdfWithTierCalculated();

    // before
    assertContentTier(CONTENT_TIER_1.getUri(), inputRdf.getAggregationList(), Aggregation::getHasQualityAnnotationList);

    // when
    RdfTierUtils.setTier(inputRdf, MediaTier.T4);

    // after
    assertContentTier(CONTENT_TIER_4.getUri(), inputRdf.getAggregationList(), Aggregation::getHasQualityAnnotationList);
  }

  @Test
  void setTierInitialise() throws Exception {
    // given
    setUpRdfWithoutTierCalculated();

    // before
    assertNoContentTier(inputRdf.getAggregationList(), Aggregation::getHasQualityAnnotationList);

    // when
    RdfTierUtils.setTierIfAbsent(inputRdf, MediaTier.T2);

    // after
    assertContentTier(CONTENT_TIER_2.getUri(), inputRdf.getAggregationList(), Aggregation::getHasQualityAnnotationList);
  }

  @Test
  void setTierEuropeanaOverwrite() throws Exception {
    // given
    setUpRdfWithTierCalculated();

    // before
    assertContentTier(CONTENT_TIER_1.getUri(), inputRdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList);

    // when
    RdfTierUtils.setTierEuropeana(inputRdf, MediaTier.T4);

    // after
    assertContentTier(CONTENT_TIER_4.getUri(), inputRdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList);
  }

  @Test
  void setTierEuropeanaInitialise() throws Exception {
    //given
    setUpRdfWithoutTierCalculated();

    // before
    assertNoContentTier(inputRdf.getEuropeanaAggregationList(), EuropeanaAggregationType::getHasQualityAnnotationList);

    // when
    RdfTierUtils.setTierEuropeanaIfAbsent(inputRdf, MediaTier.T2);

    // after
    assertContentTier(CONTENT_TIER_2.getUri(), inputRdf.getEuropeanaAggregationList(),
        EuropeanaAggregationType::getHasQualityAnnotationList);
  }
}
