package eu.europeana.enrichment.service.dao;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.bson.Document;

public class EnrichmentObjectUtils {

  public static final String ABOUT_PREFIX = "http://data.europeana.eu/concept/base/";
  public static final String DIRECTORY_WITH_EXAMPLES = "example_enrichment_entities/";
  public static final String SUBDIRECTORY_AGENTS = "agents/";
  public static final String SUBDIRECTORY_PLACES = "places/";
  public static final String SUBDIRECTORY_CONCEPTS = "concepts/";
  public static final String SUBDIRECTORY_TIMESPANS = "timespans/";
  public static final String SUBDIRECTORY_ORGANIZATIONS = "organizations/";

  public static Document getDocument(String filePath) throws IOException {
    String fullPath = DIRECTORY_WITH_EXAMPLES + filePath;
    try (InputStream inputStream = EnrichmentObjectUtils.class.getClassLoader()
        .getResourceAsStream(fullPath)) {
      if (inputStream == null) {
        throw new IOException("Count not read file: " + fullPath);
      } else {
        final String json = new String(inputStream.readAllBytes());
        return Document.parse(json);
      }
    }
  }

  //  public static EnrichmentTerm createConceptTerm(String aboutSuffix) throws Exception {
  //    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
  //    enrichmentTerm.setEntityType(EntityType.CONCEPT);
  //    enrichmentTerm.setId(null);
  //    enrichmentTerm.setParent(null);
  //    enrichmentTerm.setUpdated(null);
  //    enrichmentTerm.setCreated(null);
  //
  //    final ConceptEnrichmentEntity conceptEnrichmentEntity = (ConceptEnrichmentEntity) createAbstractEnrichmentEntity(
  //        ConceptEnrichmentEntity.class, aboutSuffix);
  //
  //    conceptEnrichmentEntity.setBroader(null);
  //    conceptEnrichmentEntity.setNarrower(null);
  //    conceptEnrichmentEntity.setRelated(null);
  //    conceptEnrichmentEntity.setBroadMatch(null);
  //    conceptEnrichmentEntity.setNarrowMatch(null);
  //    conceptEnrichmentEntity.setRelatedMatch(new String[]{"http://external-related-concept1"});
  //    conceptEnrichmentEntity.setExactMatch(
  //        new String[]{"http://external-exact-concept1", "http://external-exact-concept2"});
  //    conceptEnrichmentEntity.setCloseMatch(null);
  //    conceptEnrichmentEntity.setNotation(new HashMap<>());
  //    conceptEnrichmentEntity.setInScheme(null);
  //    enrichmentTerm.setEnrichmentEntity(conceptEnrichmentEntity);
  //
  //    enrichmentTerm.setLabelInfos(createLabelInfoList(conceptEnrichmentEntity));
  //
  //    return enrichmentTerm;
  //  }
  //
  //  public static EnrichmentTerm createTimespanTerm(String aboutSuffix) throws Exception {
  //    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
  //    enrichmentTerm.setEntityType(EntityType.TIMESPAN);
  //    enrichmentTerm.setId(null);
  //    enrichmentTerm.setParent(null);
  //    enrichmentTerm.setUpdated(null);
  //    enrichmentTerm.setCreated(null);
  //
  //    final TimespanEnrichmentEntity timespanEnrichmentEntity = (TimespanEnrichmentEntity) createAbstractEnrichmentEntity(
  //        TimespanEnrichmentEntity.class, aboutSuffix);
  //    final HashMap<String, List<String>> beginMap = new HashMap<>();
  //    beginMap.put("def", List.of("Thu Jan 01 01:00:00 CET 78"));
  //    timespanEnrichmentEntity.setBegin(beginMap);
  //    final HashMap<String, List<String>> endMap = new HashMap<>();
  //    endMap.put("def", List.of("Thu Dec 31 01:00:00 CET 78"));
  //    timespanEnrichmentEntity.setEnd(endMap);
  //    timespanEnrichmentEntity.setDctermsHasPart(null);
  //    timespanEnrichmentEntity.setIsNextInSequence("");
  //    timespanEnrichmentEntity.setIsPartOf("http://semium.org/time/00xx_4_quarter");
  //
  //    enrichmentTerm.setEnrichmentEntity(timespanEnrichmentEntity);
  //    enrichmentTerm.setLabelInfos(createLabelInfoList(timespanEnrichmentEntity));
  //
  //    return enrichmentTerm;
  //  }
  //
  //  public static Document createAgentTerm(String aboutSuffix) throws Exception {
  //    final String json = new String(
  //        EnrichmentObjectUtils.class.getClassLoader().getResourceAsStream("agent.json")
  //            .readAllBytes());
  //
  //    return Document.parse( json);
  //  }
  //
  //  public static <T> AbstractEnrichmentEntity createAbstractEnrichmentEntity(Class<T> clazz,
  //      String aboutSuffix)
  //      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
  //    final AbstractEnrichmentEntity abstractEnrichmentEntity = (AbstractEnrichmentEntity) clazz
  //        .getConstructor().newInstance();
  //    abstractEnrichmentEntity.setAbout(ABOUT_PREFIX + aboutSuffix);
  //    final HashMap<String, List<String>> prefLabelMap = new HashMap<>();
  //    prefLabelMap.put("en", List.of("Pref Label"));
  //    abstractEnrichmentEntity.setPrefLabel(prefLabelMap);
  //    final HashMap<String, List<String>> altLabelMap = new HashMap<>();
  //    altLabelMap.put("en", List.of("Alt Label"));
  //    abstractEnrichmentEntity.setAltLabel(altLabelMap);
  //    final HashMap<String, List<String>> hiddenLabelMap = new HashMap<>();
  //    hiddenLabelMap.put("en", List.of("Hidden Label"));
  //    abstractEnrichmentEntity.setHiddenLabel(hiddenLabelMap);
  //    final HashMap<String, List<String>> noteMap = new HashMap<>();
  //    noteMap.put("en", List.of("Note Label"));
  //    abstractEnrichmentEntity.setNote(noteMap);
  //    abstractEnrichmentEntity.setOwlSameAs(
  //        List.of("http://example-another-concept.org", "http://example-some-concept.org"));
  //    abstractEnrichmentEntity.setIsPartOf("");
  //    abstractEnrichmentEntity.setFoafDepiction("");
  //    return abstractEnrichmentEntity;
  //  }
  //
  //  public static List<LabelInfo> createLabelInfoList(
  //      AbstractEnrichmentEntity abstractEnrichmentEntity) {
  //    final Map<String, List<String>> combinedLabels = new HashMap<>();
  //    final Map<String, List<String>> prefLabel = abstractEnrichmentEntity.getPrefLabel();
  //    final Map<String, List<String>> altLabel = abstractEnrichmentEntity.getAltLabel();
  //
  //    copyToCombinedLabels(combinedLabels, prefLabel);
  //    copyToCombinedLabels(combinedLabels, altLabel);
  //
  //    return combinedLabels.entrySet().stream()
  //        .map(entry -> new LabelInfo(entry.getValue(), entry.getKey())).collect(Collectors.toList());
  //  }
  //
  //  public static void copyToCombinedLabels(Map<String, List<String>> combinedLabels,
  //      Map<String, List<String>> labels) {
  //    if (labels != null) {
  //      labels.forEach((key, value) -> {
  //        value = value.stream().map(String::toLowerCase).collect(Collectors.toList());
  //        combinedLabels.merge(key, value,
  //            (v1, v2) -> Stream.of(v1, v2).flatMap(List::stream).distinct()
  //                .collect(Collectors.toList()));
  //      });
  //    }
  //  }
  //
  public static boolean areHashMapsWithListValuesEqual(Map<String, List<String>> first,
      Map<String, List<String>> second) {
    if (MapUtils.isEmpty(first) && MapUtils.isEmpty(second)) {
      return true;
    }
    if (first == null || second == null || first.size() != second.size()) {
      return false;
    }
    return first.entrySet().stream().allMatch(e -> e.getValue().equals(second.get(e.getKey())));
  }
}
