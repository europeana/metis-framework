package eu.europeana.indexing.solr.property;

import eu.europeana.corelib.definitions.edm.entity.Aggregation;
import eu.europeana.corelib.definitions.edm.entity.License;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.indexing.solr.EdmLabel;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.solr.common.SolrInputDocument;

/**
 * Property Solr Creator for 'ore:Aggregation' tags.
 */
public class AggregationSolrCreator implements PropertySolrCreator<Aggregation> {

  private final List<? extends License> licenses;

  // All organizations in the record. Mapped value is null in the rare absence of preflabels.
  private final Map<String, Pair<String, String>> organizationPrefLabelMap;

  /**
   * Constructor.
   *
   * @param licenses the list of licenses for the record.
   * @param organizations the list of organizations in the record.
   */
  public AggregationSolrCreator(List<? extends License> licenses,
      List<OrganizationImpl> organizations) {
    this.licenses = new ArrayList<>(licenses);
    this.organizationPrefLabelMap = organizations.stream()
                                                 .filter(org -> StringUtils.isNotBlank(org.getAbout()))
                                                 .collect(Collectors.toMap(OrganizationImpl::getAbout,
                                                     AggregationSolrCreator::findPrefLabelForOrganization, (o1, o2) -> o1));
  }

  private static Pair<String, String> findPrefLabelForOrganization(OrganizationImpl organization) {

    // Try to find an English one first.
    final List<Pair<String, String>> englishValues = new ArrayList<>(2);
    Optional.ofNullable(organization.getPrefLabel()).map(labels -> labels.get("en")).stream()
            .flatMap(List::stream).filter(Objects::nonNull).findFirst()
            .ifPresent(value -> englishValues.add(new ImmutablePair<>("en", value)));
    Optional.ofNullable(organization.getPrefLabel()).map(labels -> labels.get("eng")).stream()
            .flatMap(List::stream).filter(Objects::nonNull).findFirst()
            .ifPresent(value -> englishValues.add(new ImmutablePair<>("eng", value)));
    if (!englishValues.isEmpty()) {
      return englishValues.getFirst();
    }

    // Otherwise return any value (if available).
    return Optional.ofNullable(organization.getPrefLabel()).map(Map::entrySet).stream()
                   .flatMap(Collection::stream)
                   .filter(Objects::nonNull).filter(entry -> entry.getValue() != null)
                   .flatMap(entry -> entry.getValue().stream().filter(StringUtils::isNotBlank)
                                          .map(value -> new ImmutablePair<>(entry.getKey(), value)))
                   .findFirst().orElse(null);
  }

  @Override
  public void addToDocument(SolrInputDocument doc, Aggregation aggregation) {
    //Extract organization uris
    final Pair<Set<String>, Map<String, List<String>>> dataProviderPair = extractUrisAndLiterals(
        aggregation.getEdmDataProvider());
    final Pair<Set<String>, Map<String, List<String>>> providerPair = extractUrisAndLiterals(
        aggregation.getEdmProvider());
    final Pair<Set<String>, Map<String, List<String>>> intermediatePair = extractUrisAndLiterals(
        aggregation.getEdmIntermediateProvider());

    final String[] combinedProviderAndIntermediateUris = Stream
        .concat(providerPair.getLeft().stream(), intermediatePair.getLeft().stream())
        .toArray(String[]::new);

    //Single value, contains provider uri(in practice the list provided has one or no value)
    dataProviderPair.getLeft().stream().findFirst()
                    .ifPresent(uri -> SolrPropertyUtils.addValue(doc, EdmLabel.DATA_PROVIDER, uri));
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

  private Pair<Set<String>, Map<String, List<String>>> extractUrisAndLiterals(
      final Map<String, List<String>> urisLiteralsMap) {
    final Set<String> organizationUris = new HashSet<>();
    final Map<String, List<String>> literalsMap = new HashMap<>();

    if (MapUtils.isNotEmpty(urisLiteralsMap)) {
      splitOrganizationUrisFromLiterals(urisLiteralsMap, organizationUris, literalsMap);

      //Extend map with organization pref labels
      if (CollectionUtils.isNotEmpty(organizationUris)) {
        addOrganizationPrefLabelsToLiterals(organizationUris, literalsMap);
      }
    }
    return new ImmutablePair<>(organizationUris, literalsMap);
  }

  private void splitOrganizationUrisFromLiterals(Map<String, List<String>> urisLiteralsMap,
      Set<String> organizationUris, Map<String, List<String>> literalsMap) {
    for (Map.Entry<String, List<String>> entry : urisLiteralsMap.entrySet()) {
      final List<String> literals = new ArrayList<>();
      for (String value : entry.getValue()) {
        if (organizationPrefLabelMap.containsKey(value)) {
          organizationUris.add(value);
        } else {
          literals.add(value);
        }
      }
      if (!literals.isEmpty()) {
        literalsMap.put(entry.getKey(), literals);
      }
    }
  }

  private void addOrganizationPrefLabelsToLiterals(Set<String> organizationUris,
      Map<String, List<String>> literalsMap) {
    for (String organizationUri : organizationUris) {
      final Pair<String, String> entry = organizationPrefLabelMap.get(organizationUri);
      if (entry != null) {
        literalsMap.computeIfAbsent(entry.getKey(), key -> new ArrayList<>()).add(entry.getValue());
      }
    }
  }
}
