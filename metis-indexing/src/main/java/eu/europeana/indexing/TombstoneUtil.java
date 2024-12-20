package eu.europeana.indexing;

import static java.util.function.Predicate.not;

import eu.europeana.corelib.definitions.edm.entity.ChangeLog;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ChangeLogImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.metis.utils.DepublicationReason;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.commons.lang3.ArrayUtils;

/**
 * Contains functionality for updating fields that are required for tombstone records.
 */
public final class TombstoneUtil {

  private TombstoneUtil() {
  }

  /**
   * Prepare the tombstone fullbean.
   *
   * @param publishedFullbean the published fullbean
   * @param depublicationReason the depublication reason
   * @return the newly created tombstone
   */
  public static FullBeanImpl prepareTombstoneFullbean(FullBeanImpl publishedFullbean, DepublicationReason depublicationReason) {

    final FullBeanImpl tombstoneFullbean = new FullBeanImpl();
    tombstoneFullbean.setAbout(publishedFullbean.getAbout());
    tombstoneFullbean.setEuropeanaCollectionName(publishedFullbean.getEuropeanaCollectionName());
    tombstoneFullbean.setTimestampCreated(publishedFullbean.getTimestampCreated());
    tombstoneFullbean.setTimestampUpdated(publishedFullbean.getTimestampUpdated());

    final ProvidedCHOImpl providedCHO = new ProvidedCHOImpl();
    providedCHO.setAbout(publishedFullbean.getProvidedCHOs().getFirst().getAbout());
    providedCHO.setOwlSameAs(safeClone(publishedFullbean.getProvidedCHOs().getFirst().getOwlSameAs()));
    tombstoneFullbean.setProvidedCHOs(List.of(providedCHO));

    tombstoneFullbean.setEuropeanaAggregation(
        prepareEuropeanaAggregation(publishedFullbean.getEuropeanaAggregation(), depublicationReason));
    tombstoneFullbean.setAggregations(prepareAggregations(publishedFullbean.getAggregations()));
    tombstoneFullbean.setProxies(prepareProxies(publishedFullbean));
    tombstoneFullbean.setOrganizations(prepareOrganizations(publishedFullbean.getOrganizations()));
    tombstoneFullbean.setAgents(prepareAgents(publishedFullbean.getAgents()));
    tombstoneFullbean.setPlaces(preparePlaces(publishedFullbean.getPlaces()));
    tombstoneFullbean.setTimespans(prepareTimespans(publishedFullbean.getTimespans()));
    tombstoneFullbean.setConcepts(prepareConcepts(publishedFullbean.getConcepts()));
    tombstoneFullbean.setLicenses(prepareLicenses(publishedFullbean.getLicenses()));

    return tombstoneFullbean;
  }

  private static ArrayList<ProxyImpl> prepareProxies(FullBeanImpl publishedFullbean) {
    final ArrayList<ProxyImpl> tombstoneProxies = new ArrayList<>();
    final Optional<ProxyImpl> dataProviderProxy =
        publishedFullbean.getProxies().stream().filter(not(ProxyImpl::isEuropeanaProxy)).findFirst();
    dataProviderProxy.ifPresent(proxy -> tombstoneProxies.add(prepareDataProviderProxy(proxy)));

    final List<ProxyImpl> europeanaOrAggregatorProxies = publishedFullbean.getProxies().stream().filter(
        proxy -> proxy.isEuropeanaProxy() || ArrayUtils.isNotEmpty(proxy.getLineage())).toList();
    tombstoneProxies.addAll(prepareEuropeanaOrAggregatorProxy(europeanaOrAggregatorProxies));
    return tombstoneProxies;
  }

  private static List<OrganizationImpl> prepareOrganizations(List<OrganizationImpl> organizations) {
    return organizations.stream().map(organization -> {
      final OrganizationImpl tombstoneOrganization = new OrganizationImpl();
      tombstoneOrganization.setAbout(organization.getAbout());
      tombstoneOrganization.setPrefLabel(copyMap(organization.getPrefLabel()));
      tombstoneOrganization.setOwlSameAs(safeClone(organization.getOwlSameAs()));
      return tombstoneOrganization;
    }).toList();
  }

  private static List<AgentImpl> prepareAgents(List<AgentImpl> agents) {
    return agents.stream().map(agent -> {
      final AgentImpl tombstoneAgent = new AgentImpl();
      tombstoneAgent.setAbout(agent.getAbout());
      tombstoneAgent.setPrefLabel(copyMap(agent.getPrefLabel()));
      tombstoneAgent.setDcIdentifier(copyMap(agent.getDcIdentifier()));
      tombstoneAgent.setOwlSameAs(safeClone(agent.getOwlSameAs()));
      return tombstoneAgent;
    }).toList();
  }

  private static List<PlaceImpl> preparePlaces(List<PlaceImpl> places) {
    return places.stream().map(place -> {
      final PlaceImpl tombstonePlace = new PlaceImpl();
      tombstonePlace.setAbout(place.getAbout());
      tombstonePlace.setPrefLabel(copyMap(place.getPrefLabel()));
      tombstonePlace.setOwlSameAs(safeClone(place.getOwlSameAs()));
      return tombstonePlace;
    }).toList();
  }

  private static List<TimespanImpl> prepareTimespans(List<TimespanImpl> timespans) {
    return timespans.stream().map(timespan -> {
      final TimespanImpl tombstoneTimespan = new TimespanImpl();
      tombstoneTimespan.setAbout(timespan.getAbout());
      tombstoneTimespan.setPrefLabel(copyMap(timespan.getPrefLabel()));
      tombstoneTimespan.setBegin(copyMap(timespan.getBegin()));
      tombstoneTimespan.setEnd(copyMap(timespan.getEnd()));
      tombstoneTimespan.setOwlSameAs(safeClone(timespan.getOwlSameAs()));
      return tombstoneTimespan;
    }).toList();
  }

  private static List<ConceptImpl> prepareConcepts(List<ConceptImpl> concepts) {
    return concepts.stream().map(concept -> {
      final ConceptImpl tombstoneConcept = new ConceptImpl();
      tombstoneConcept.setAbout(concept.getAbout());
      tombstoneConcept.setPrefLabel(copyMap(concept.getPrefLabel()));
      tombstoneConcept.setExactMatch(safeClone(concept.getExactMatch()));
      return tombstoneConcept;
    }).toList();
  }

  private static List<LicenseImpl> prepareLicenses(List<LicenseImpl> licenses) {
    return licenses.stream().map(license -> {
      final LicenseImpl tombstoneLicense = new LicenseImpl();
      tombstoneLicense.setAbout(license.getAbout());
      tombstoneLicense.setOdrlInheritFrom(license.getOdrlInheritFrom());
      return tombstoneLicense;
    }).toList();
  }

  private static EuropeanaAggregation prepareEuropeanaAggregation(EuropeanaAggregation europeanaAggregation,
      DepublicationReason depublicationReason) {
    final ChangeLog tombstoneChangeLog = new ChangeLogImpl();
    tombstoneChangeLog.setType("Delete");
    tombstoneChangeLog.setContext(depublicationReason.getUrl());
    tombstoneChangeLog.setEndTime(new Date());
    final EuropeanaAggregation tombstoneEuropeanaAggregation = new EuropeanaAggregationImpl();
    tombstoneEuropeanaAggregation.setAbout(europeanaAggregation.getAbout());
    tombstoneEuropeanaAggregation.setChangeLog(List.of(tombstoneChangeLog));
    tombstoneEuropeanaAggregation.setAggregatedCHO(europeanaAggregation.getAggregatedCHO());
    tombstoneEuropeanaAggregation.setEdmPreview(europeanaAggregation.getEdmPreview());
    tombstoneEuropeanaAggregation.setEdmLanguage(copyMap(europeanaAggregation.getEdmLanguage()));
    tombstoneEuropeanaAggregation.setEdmCountry(copyMap(europeanaAggregation.getEdmCountry()));
    tombstoneEuropeanaAggregation.setEdmIsShownBy(europeanaAggregation.getEdmIsShownBy());
    return tombstoneEuropeanaAggregation;
  }

  private static List<AggregationImpl> prepareAggregations(List<AggregationImpl> aggregations) {
    return aggregations.stream().map(aggregation -> {
      final AggregationImpl tombstoneAggregation = new AggregationImpl();
      tombstoneAggregation.setAbout(aggregation.getAbout());
      tombstoneAggregation.setAggregatedCHO(aggregation.getAggregatedCHO());
      tombstoneAggregation.setEdmDataProvider(copyMap(aggregation.getEdmDataProvider()));
      tombstoneAggregation.setEdmProvider(copyMap(aggregation.getEdmProvider()));
      tombstoneAggregation.setEdmIntermediateProvider(copyMap(aggregation.getEdmIntermediateProvider()));
      tombstoneAggregation.setEdmIsShownAt(aggregation.getEdmIsShownAt());
      tombstoneAggregation.setEdmIsShownBy(aggregation.getEdmIsShownBy());
      tombstoneAggregation.setDcRights(copyMap(aggregation.getDcRights()));
      tombstoneAggregation.setEdmRights(copyMap(aggregation.getEdmRights()));
      return tombstoneAggregation;
    }).toList();
  }

  private static ProxyImpl prepareDataProviderProxy(ProxyImpl proxy) {
    final ProxyImpl tombstoneProxy = new ProxyImpl();
    tombstoneProxy.setAbout(proxy.getAbout());
    tombstoneProxy.setDcContributor(copyMap(proxy.getDcContributor()));
    tombstoneProxy.setDcCreator(copyMap(proxy.getDcCreator()));
    tombstoneProxy.setDcDescription(copyMap(proxy.getDcDescription()));
    tombstoneProxy.setDcIdentifier(copyMap(proxy.getDcIdentifier()));
    tombstoneProxy.setDcPublisher(copyMap(proxy.getDcPublisher()));
    tombstoneProxy.setDcRights(copyMap(proxy.getDcRights()));
    tombstoneProxy.setDcTitle(copyMap(proxy.getDcTitle()));
    tombstoneProxy.setDctermsCreated(copyMap(proxy.getDctermsCreated()));
    tombstoneProxy.setDctermsIsReferencedBy(copyMap(proxy.getDctermsIsReferencedBy()));
    tombstoneProxy.setDctermsIssued(copyMap(proxy.getDctermsIssued()));
    tombstoneProxy.setEdmType(proxy.getEdmType());
    tombstoneProxy.setEuropeanaProxy(proxy.isEuropeanaProxy());
    tombstoneProxy.setProxyIn(safeClone(proxy.getProxyIn()));
    tombstoneProxy.setProxyFor(proxy.getProxyFor());
    return tombstoneProxy;
  }

  private static List<ProxyImpl> prepareEuropeanaOrAggregatorProxy(List<ProxyImpl> proxies) {
    return proxies.stream().map(proxy -> {
      final ProxyImpl tombstoneProxy = new ProxyImpl();
      tombstoneProxy.setAbout(proxy.getAbout());
      tombstoneProxy.setEuropeanaProxy(proxy.isEuropeanaProxy());
      tombstoneProxy.setLineage(safeClone(proxy.getLineage()));
      tombstoneProxy.setProxyIn(safeClone(proxy.getProxyIn()));
      tombstoneProxy.setProxyFor(proxy.getProxyFor());
      return tombstoneProxy;
    }).toList();
  }

  private static <K, V> Map<K, List<V>> copyMap(Map<K, List<V>> original) {
    final Map<K, List<V>> copy;
    if (original == null) {
      copy = null;
    } else {
      copy = new HashMap<>();
      for (Map.Entry<K, List<V>> entry : original.entrySet()) {
        copy.put(entry.getKey(), new ArrayList<>(entry.getValue()));
      }
    }
    return copy;
  }

  private static String[] safeClone(String[] object) {
    return Optional.ofNullable(object).map(String[]::clone).orElse(null);
  }
}
