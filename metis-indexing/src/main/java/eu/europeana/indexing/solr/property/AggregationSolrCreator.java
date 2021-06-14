package eu.europeana.indexing.solr.property;

import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.common.SolrInputDocument;

/**
 * Property Solr Creator for 'ore:Aggregation' tags.
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class AggregationSolrCreator implements PropertySolrCreator<Aggregation> {

  private final List<? extends License> licenses;
  private final Map<String, OrganizationImpl> organizationMap;

  /**
   * Constructor.
   *
   * @param licenses the list of licenses for the record.
   * @param organizations the list of organizations in the record.
   */
  public AggregationSolrCreator(List<? extends License> licenses,
      List<OrganizationImpl> organizations) {
    this.licenses = new ArrayList<>(licenses);
    this.organizationMap = organizations.stream()
        .collect(Collectors.toMap(OrganizationImpl::getAbout, Function.identity(), (o1, o2) -> o1));
  }

  @Override
  public void addToDocument(SolrInputDocument doc, Aggregation aggregation) {
    //Extract organization uris
    final Pair<List<String>, Map<String, List<String>>> dataProviderPair = splitUrisFromLiterals(
        aggregation.getEdmDataProvider());
    final Pair<List<String>, Map<String, List<String>>> providerPair = splitUrisFromLiterals(
        aggregation.getEdmProvider());
    final Pair<List<String>, Map<String, List<String>>> intermediatePair = splitUrisFromLiterals(
        aggregation.getEdmIntermediateProvider());

    final String[] combinedProviderAndIntermediateUris = Stream
        .concat(providerPair.getLeft().stream(), intermediatePair.getLeft().stream())
        .toArray(String[]::new);

    //Single value, contains provider uri(in practice the list provided has one value)
    SolrPropertyUtils.addValue(doc, EdmLabel.DATA_PROVIDER, dataProviderPair.getLeft().get(0));
    //Multivalued, contains provider and intermediate uris
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER, combinedProviderAndIntermediateUris);

    //Literal fields
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_DATA_PROVIDER,
        dataProviderPair.getRight());
    SolrPropertyUtils
        .addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_PROVIDER, providerPair.getRight());
    SolrPropertyUtils.addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_INTERMEDIATE_PROVIDER,
        intermediatePair.getRight());

    SolrPropertyUtils
        .addValues(doc, EdmLabel.PROVIDER_AGGREGATION_DC_RIGHTS, aggregation.getDcRights());
    if (!SolrPropertyUtils.hasLicenseForRights(aggregation.getEdmRights(),
        item -> licenses.stream().anyMatch(license -> license.getAbout().equals(item)))) {
      SolrPropertyUtils
          .addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_RIGHTS, aggregation.getEdmRights());
    }
    SolrPropertyUtils
        .addValues(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_HASVIEW, aggregation.getHasView());
    SolrPropertyUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_AT,
        aggregation.getEdmIsShownAt());
    SolrPropertyUtils.addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY,
        aggregation.getEdmIsShownBy());
    SolrPropertyUtils
        .addValue(doc, EdmLabel.PROVIDER_AGGREGATION_EDM_OBJECT, aggregation.getEdmObject());
    SolrPropertyUtils.addValue(doc, EdmLabel.EDM_UGC, aggregation.getEdmUgc());
    doc.addField(EdmLabel.PREVIEW_NO_DISTRIBUTE.toString(),
        aggregation.getEdmPreviewNoDistribute());
    new WebResourceSolrCreator(licenses).addAllToDocument(doc, aggregation.getWebResources());
  }

  private Pair<List<String>, Map<String, List<String>>> splitUrisFromLiterals(
      final Map<String, List<String>> urisLiteralsMap) {
    final List<String> uriList = new ArrayList<>();
    final Map<String, List<String>> literalsMap = new HashMap<>();

    if (MapUtils.isNotEmpty(urisLiteralsMap)) {
      extractUris(urisLiteralsMap, uriList, literalsMap);

      //Extend map with organization pref labels
      if (CollectionUtils.isNotEmpty(uriList)) {
        extendWithOrganizationLiterals(literalsMap);
      }
    }
    return new ImmutablePair<>(uriList, literalsMap);
  }

  private void extractUris(Map<String, List<String>> urisLiteralsMap,
      List<String> uriList, Map<String, List<String>> literalsMap) {
    for (Map.Entry<String, List<String>> entry : urisLiteralsMap.entrySet()) {
      final List<String> literals = new ArrayList<>();
      for (String value : entry.getValue()) {
        if (organizationMap.containsKey(value)) {
          uriList.add(value);
        } else {
          literals.add(value);
        }
      }
      literalsMap.put(entry.getKey(), literals);
    }
    //Remove "def" key if it's empty
    if (CollectionUtils.isEmpty(literalsMap.get("def"))) {
      literalsMap.remove("def");
    }
  }

  private void extendWithOrganizationLiterals(Map<String, List<String>> literalsMap) {
    for (OrganizationImpl organizationImpl : organizationMap.values()) {
      for (Map.Entry<String, List<String>> entry : organizationImpl.getPrefLabel().entrySet()) {
        final List<String> literals = literalsMap
            .getOrDefault(entry.getKey(), new ArrayList<>());
        literals.addAll(entry.getValue());
        //Replace the list in the map and make sure duplicates were not introduced per key
        literalsMap.put(entry.getKey(), new ArrayList<>(new HashSet<>(literals)));
      }
    }
  }
}
