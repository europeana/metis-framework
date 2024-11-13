package eu.europeana.indexing;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import eu.europeana.corelib.definitions.edm.entity.ChangeLog;
import eu.europeana.corelib.definitions.edm.entity.EuropeanaAggregation;
import eu.europeana.corelib.solr.bean.impl.FullBeanImpl;
import eu.europeana.corelib.solr.entity.AgentImpl;
import eu.europeana.corelib.solr.entity.AggregationImpl;
import eu.europeana.corelib.solr.entity.ConceptImpl;
import eu.europeana.corelib.solr.entity.EuropeanaAggregationImpl;
import eu.europeana.corelib.solr.entity.LicenseImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.corelib.solr.entity.PlaceImpl;
import eu.europeana.corelib.solr.entity.ProvidedCHOImpl;
import eu.europeana.corelib.solr.entity.ProxyImpl;
import eu.europeana.corelib.solr.entity.TimespanImpl;
import eu.europeana.metis.utils.DepublicationReason;
import java.util.Date;
import java.util.List;
import java.util.Map;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class TombstoneUtilTest {

  private TombstoneUtil tombstoneUtil;
  private FullBeanImpl publishedFullbean;

  @BeforeEach
  void setUp() {
    tombstoneUtil = new TombstoneUtil();
    Date currentTime = new Date();

    publishedFullbean = new FullBeanImpl();
    publishedFullbean.setEuropeanaId(new ObjectId());
    publishedFullbean.setAbout("sampleAbout");
    publishedFullbean.setEuropeanaCollectionName(new String[]{"sampleCollection"});
    publishedFullbean.setTimestampCreated(currentTime);
    publishedFullbean.setTimestampUpdated(currentTime);

    ProvidedCHOImpl providedCHO = new ProvidedCHOImpl();
    providedCHO.setAbout(publishedFullbean.getAbout());
    providedCHO.setOwlSameAs(new String[]{"sampleOwlSameAs"});
    publishedFullbean.setProvidedCHOs(List.of(providedCHO));

    EuropeanaAggregation europeanaAggregation = new EuropeanaAggregationImpl();
    europeanaAggregation.setAbout("sampleEuropeanaAggregationAbout");
    europeanaAggregation.setEdmLanguage(Map.of("def", List.of("en")));
    europeanaAggregation.setAggregatedCHO(publishedFullbean.getAbout());
    europeanaAggregation.setEdmPreview("sampleEdmPreview");
    europeanaAggregation.setEdmCountry(Map.of("def", List.of("Greece")));
    europeanaAggregation.setEdmIsShownBy("sampleEdmIsShownBy");
    publishedFullbean.setEuropeanaAggregation(europeanaAggregation);

    final AggregationImpl aggregation = new AggregationImpl();
    aggregation.setAbout("sampleAggregationAbout");
    aggregation.setAggregatedCHO(publishedFullbean.getAbout());
    aggregation.setEdmDataProvider(Map.of("def", List.of("sampleEdmDataProvider")));
    aggregation.setEdmProvider(Map.of("def", List.of("sampleEdmProvider")));
    aggregation.setEdmIntermediateProvider(Map.of("def", List.of("sampleEdmIntermediateProvider")));
    aggregation.setEdmIsShownAt("sampleEdmIsShownAt");
    aggregation.setEdmIsShownBy("sampleEdmIsShownBy");
    aggregation.setDcRights(Map.of("def", List.of("sampleDcRights")));
    aggregation.setEdmRights(Map.of("def", List.of("sampleEdmRights")));
    publishedFullbean.setAggregations(List.of(aggregation));

    ProxyImpl dataProviderProxy = new ProxyImpl();
    dataProviderProxy.setAbout("sampleDataProviderProxyAbout");
    dataProviderProxy.setEuropeanaProxy(false);
    dataProviderProxy.setDcContributor(Map.of("def", List.of("sampleDcContributor")));
    dataProviderProxy.setDcCreator(Map.of("def", List.of("sampleDcCreator")));
    dataProviderProxy.setDcDescription(Map.of("def", List.of("sampleDcDescription")));
    dataProviderProxy.setDcIdentifier(Map.of("def", List.of("sampleDcIdentifier")));
    dataProviderProxy.setDcPublisher(Map.of("def", List.of("sampleDcPublisher")));
    dataProviderProxy.setDcRights(Map.of("def", List.of("sampleDcRights")));
    dataProviderProxy.setDcTitle(Map.of("def", List.of("sampleDcTitle")));
    dataProviderProxy.setDctermsCreated(Map.of("def", List.of("sampleDcTermsCreated")));
    dataProviderProxy.setDctermsIsReferencedBy(Map.of("def", List.of("sampleDcTermsIsReferencedBy")));
    dataProviderProxy.setDctermsIssued(Map.of("def", List.of("sampleDcTermsIssued")));
    dataProviderProxy.setEdmType("sampleEdmType");
    dataProviderProxy.setProxyIn(new String[]{aggregation.getAbout()});
    dataProviderProxy.setProxyFor(publishedFullbean.getAbout());

    ProxyImpl europeanaProxy = new ProxyImpl();
    europeanaProxy.setAbout("sampleEuropeanaProxyAbout");
    europeanaProxy.setEuropeanaProxy(true);
    europeanaProxy.setLineage(new String[]{dataProviderProxy.getAbout()});
    europeanaProxy.setProxyIn(new String[]{europeanaAggregation.getAbout()});
    europeanaProxy.setProxyFor(publishedFullbean.getAbout());
    publishedFullbean.setProxies(List.of(dataProviderProxy, europeanaProxy));

    OrganizationImpl organization = new OrganizationImpl();
    organization.setAbout("sampleOrganizationAbout");
    organization.setPrefLabel(Map.of("en", List.of("samplePrefLabel")));
    organization.setOwlSameAs(new String[]{"sampleOwlSameAs"});
    publishedFullbean.setOrganizations(List.of(organization));

    final AgentImpl agent = new AgentImpl();
    agent.setAbout("sampleAgentAbout");
    agent.setPrefLabel(Map.of("en", List.of("samplePrefLabel")));
    agent.setDcIdentifier(Map.of("en", List.of("sampleDcIdentifier")));
    agent.setOwlSameAs(new String[]{"sampleOwlSameAs"});
    publishedFullbean.setAgents(List.of(agent));

    final PlaceImpl place = new PlaceImpl();
    place.setAbout("samplePlaceAbout");
    place.setPrefLabel(Map.of("en", List.of("samplePrefLabel")));
    place.setOwlSameAs(new String[]{"sampleOwlSameAs"});
    publishedFullbean.setPlaces(List.of(place));

    final TimespanImpl timespan = new TimespanImpl();
    timespan.setAbout("sampleTimespanAbout");
    timespan.setPrefLabel(Map.of("en", List.of("samplePrefLabel")));
    timespan.setBegin(Map.of("def", List.of("sampleBegin")));
    timespan.setEnd(Map.of("def", List.of("sampleEnd")));
    timespan.setOwlSameAs(new String[]{"sampleOwlSameAs"});
    publishedFullbean.setTimespans(List.of(timespan));

    final ConceptImpl concept = new ConceptImpl();
    concept.setAbout("sampleConceptAbout");
    concept.setPrefLabel(Map.of("en", List.of("samplePrefLabel")));
    concept.setExactMatch(new String[]{"sampleExactMatch"});
    publishedFullbean.setConcepts(List.of(concept));

    final LicenseImpl license = new LicenseImpl();
    license.setAbout("sampleLicenseAbout");
    license.setOdrlInheritFrom("sampleOdrInheritFrom");
    publishedFullbean.setLicenses(List.of(license));
  }

  @Test
  void testPrepareTombstoneFullbean() {
    DepublicationReason depublicationReason = DepublicationReason.PERMISSION_ISSUES;
    FullBeanImpl tombstone = tombstoneUtil.prepareTombstoneFullbean(publishedFullbean, depublicationReason);

    assertNotEquals(publishedFullbean, tombstone);
    assertEquals(publishedFullbean.getAbout(), tombstone.getAbout());
    assertArrayEquals(publishedFullbean.getEuropeanaCollectionName(), tombstone.getEuropeanaCollectionName());
    assertEquals(publishedFullbean.getTimestampCreated(), tombstone.getTimestampCreated());
    assertEquals(publishedFullbean.getTimestampUpdated(), tombstone.getTimestampUpdated());

    assertProvidedCHOs(tombstone);
    assertEuropeanaAggregation(tombstone);
    assertAggregations(tombstone);
    assertProxies(tombstone);
    assertOrganization(tombstone);
    assertAgents(tombstone);
    assertPlaces(tombstone);
    assertTimespans(tombstone);
    assertConcepts(tombstone);
    assertLicenses(tombstone);
  }

  private void assertProvidedCHOs(FullBeanImpl tombstone) {
    final List<ProvidedCHOImpl> publishedProvidedCHOs = publishedFullbean.getProvidedCHOs();
    final List<ProvidedCHOImpl> tombstoneProvidedCHOs = tombstone.getProvidedCHOs();
    assertEquals(publishedProvidedCHOs.size(), tombstoneProvidedCHOs.size());
    publishedProvidedCHOs.forEach(providedCHO -> {
      final ProvidedCHOImpl matchingProvidedCHO = tombstoneProvidedCHOs.stream().filter(
          tombstoneProvidedCHO -> tombstoneProvidedCHO.getAbout().equals(providedCHO.getAbout())).findFirst().orElse(null);
      assertNotNull(matchingProvidedCHO);
      assertEquals(providedCHO.getAbout(), matchingProvidedCHO.getAbout());
      assertArrayEquals(providedCHO.getOwlSameAs(), matchingProvidedCHO.getOwlSameAs());
    });
  }

  private void assertEuropeanaAggregation(FullBeanImpl tombstone) {
    final EuropeanaAggregation publishedEuropeanaAggregation = publishedFullbean.getEuropeanaAggregation();
    final EuropeanaAggregation tombStoneEuropeanaAggregation = tombstone.getEuropeanaAggregation();
    assertEquals(publishedEuropeanaAggregation.getAbout(), tombStoneEuropeanaAggregation.getAbout());
    assertEquals(publishedEuropeanaAggregation.getEdmLanguage(), tombStoneEuropeanaAggregation.getEdmLanguage());
    assertEquals(publishedEuropeanaAggregation.getAggregatedCHO(), tombStoneEuropeanaAggregation.getAggregatedCHO());
    assertEquals(publishedEuropeanaAggregation.getEdmPreview(), tombStoneEuropeanaAggregation.getEdmPreview());
    assertEquals(publishedEuropeanaAggregation.getEdmCountry(), tombStoneEuropeanaAggregation.getEdmCountry());
    assertEquals(publishedEuropeanaAggregation.getEdmIsShownBy(), tombStoneEuropeanaAggregation.getEdmIsShownBy());

    final List<? extends ChangeLog> publishedChangeLogs = publishedEuropeanaAggregation.getChangeLog();
    assertTrue(publishedChangeLogs.isEmpty());
    final List<? extends ChangeLog> tombstoneChangeLogs = tombStoneEuropeanaAggregation.getChangeLog();
    assertEquals(1, tombstoneChangeLogs.size());
    assertEquals("Delete", tombstoneChangeLogs.getFirst().getType());
    assertNotNull(tombstoneChangeLogs.getFirst().getContext());
    assertNotNull(tombstoneChangeLogs.getFirst().getEndTime());
  }

  private void assertAggregations(FullBeanImpl tombstone) {
    final List<AggregationImpl> publishedFullbeanAggregations = publishedFullbean.getAggregations();
    final List<AggregationImpl> tombstoneAggregations = tombstone.getAggregations();
    assertEquals(publishedFullbeanAggregations.size(), tombstoneAggregations.size());

    publishedFullbeanAggregations.forEach(aggregation -> {
      final AggregationImpl matchingAggregation = tombstoneAggregations.stream().filter(
          tombstoneAggregation -> tombstoneAggregation.getAbout().equals(aggregation.getAbout())).findFirst().orElse(null);
      assertNotNull(matchingAggregation);
      assertEquals(aggregation.getAbout(), matchingAggregation.getAbout());
      assertEquals(aggregation.getAggregatedCHO(), matchingAggregation.getAggregatedCHO());
      assertEquals(aggregation.getEdmDataProvider(), matchingAggregation.getEdmDataProvider());
      assertEquals(aggregation.getEdmProvider(), matchingAggregation.getEdmProvider());
      assertEquals(aggregation.getEdmIntermediateProvider(), matchingAggregation.getEdmIntermediateProvider());
      assertEquals(aggregation.getEdmIsShownAt(), matchingAggregation.getEdmIsShownAt());
      assertEquals(aggregation.getEdmIsShownBy(), matchingAggregation.getEdmIsShownBy());
      assertEquals(aggregation.getDcRights(), matchingAggregation.getDcRights());
      assertEquals(aggregation.getEdmRights(), matchingAggregation.getEdmRights());
    });
  }

  private void assertProxies(FullBeanImpl tombstone) {
    final List<ProxyImpl> publishedProxies = publishedFullbean.getProxies();
    final List<ProxyImpl> tombstoneProxies = tombstone.getProxies();
    assertEquals(publishedProxies.size(), tombstoneProxies.size());
    publishedProxies.forEach(proxy -> {
      final ProxyImpl matchingProxy = tombstoneProxies.stream().filter(
          tombstoneProxy -> tombstoneProxy.getAbout().equals(proxy.getAbout())).findFirst().orElse(null);
      assertNotNull(matchingProxy);
      assertEquals(proxy.getAbout(), matchingProxy.getAbout());
      assertEquals(proxy.isEuropeanaProxy(), matchingProxy.isEuropeanaProxy());
      assertEquals(proxy.getDcContributor(), matchingProxy.getDcContributor());
      assertEquals(proxy.getDcCreator(), matchingProxy.getDcCreator());
      assertEquals(proxy.getDcDescription(), matchingProxy.getDcDescription());
      assertEquals(proxy.getDcIdentifier(), matchingProxy.getDcIdentifier());
      assertEquals(proxy.getDcPublisher(), matchingProxy.getDcPublisher());
      assertEquals(proxy.getDcRights(), matchingProxy.getDcRights());
      assertEquals(proxy.getDcTitle(), matchingProxy.getDcTitle());
      assertEquals(proxy.getDctermsCreated(), matchingProxy.getDctermsCreated());
      assertEquals(proxy.getDctermsIsReferencedBy(), matchingProxy.getDctermsIsReferencedBy());
      assertEquals(proxy.getDctermsIssued(), matchingProxy.getDctermsIssued());
      assertEquals(proxy.getEdmType(), matchingProxy.getEdmType());
      assertArrayEquals(proxy.getProxyIn(), matchingProxy.getProxyIn());
      assertEquals(proxy.getProxyFor(), matchingProxy.getProxyFor());
      assertArrayEquals(proxy.getLineage(), matchingProxy.getLineage());
    });
  }

  private void assertOrganization(FullBeanImpl tombstone) {
    final List<OrganizationImpl> publishedOrganizations = publishedFullbean.getOrganizations();
    final List<OrganizationImpl> tombstoneOrganizations = tombstone.getOrganizations();
    assertEquals(publishedOrganizations.size(), tombstoneOrganizations.size());
    publishedOrganizations.forEach(organization -> {
      final OrganizationImpl matchingOrganization = tombstoneOrganizations.stream().filter(
          tombstoneOrganization -> tombstoneOrganization.getAbout().equals(organization.getAbout())).findFirst().orElse(null);
      assertNotNull(matchingOrganization);
      assertEquals(organization.getAbout(), matchingOrganization.getAbout());
      assertEquals(organization.getPrefLabel(), matchingOrganization.getPrefLabel());
      assertArrayEquals(organization.getOwlSameAs(), matchingOrganization.getOwlSameAs());
    });
  }

  private void assertAgents(FullBeanImpl tombstone) {
    final List<AgentImpl> publishedAgents = publishedFullbean.getAgents();
    final List<AgentImpl> tombstoneAgents = tombstone.getAgents();
    assertEquals(publishedAgents.size(), tombstoneAgents.size());
    publishedAgents.forEach(agent -> {
      final AgentImpl matchingAgent = tombstoneAgents.stream().filter(
          tombstoneAgent -> tombstoneAgent.getAbout().equals(agent.getAbout())).findFirst().orElse(null);
      assertNotNull(matchingAgent);
      assertEquals(agent.getAbout(), matchingAgent.getAbout());
      assertEquals(agent.getPrefLabel(), matchingAgent.getPrefLabel());
      assertEquals(agent.getDcIdentifier(), matchingAgent.getDcIdentifier());
      assertArrayEquals(agent.getOwlSameAs(), matchingAgent.getOwlSameAs());
    });
  }

  private void assertPlaces(FullBeanImpl tombstone) {
    final List<PlaceImpl> publishedPlaces = publishedFullbean.getPlaces();
    final List<PlaceImpl> tombstonePlaces = tombstone.getPlaces();
    assertEquals(publishedPlaces.size(), tombstonePlaces.size());
    publishedPlaces.forEach(place -> {
      final PlaceImpl matchingPlace = tombstonePlaces.stream().filter(
          tombstoneAgent -> tombstoneAgent.getAbout().equals(place.getAbout())).findFirst().orElse(null);
      assertNotNull(matchingPlace);
      assertEquals(place.getAbout(), matchingPlace.getAbout());
      assertEquals(place.getPrefLabel(), matchingPlace.getPrefLabel());
      assertArrayEquals(place.getOwlSameAs(), matchingPlace.getOwlSameAs());
    });
  }

  private void assertTimespans(FullBeanImpl tombstone) {
    final List<TimespanImpl> publishedTimespans = publishedFullbean.getTimespans();
    final List<TimespanImpl> tombstoneTimespans = tombstone.getTimespans();
    assertEquals(publishedTimespans.size(), tombstoneTimespans.size());
    publishedTimespans.forEach(timespan -> {
      final TimespanImpl matchingTimespan = tombstoneTimespans.stream().filter(
          tombstoneAgent -> tombstoneAgent.getAbout().equals(timespan.getAbout())).findFirst().orElse(null);
      assertNotNull(matchingTimespan);
      assertEquals(timespan.getAbout(), matchingTimespan.getAbout());
      assertEquals(timespan.getPrefLabel(), matchingTimespan.getPrefLabel());
      assertEquals(timespan.getBegin(), matchingTimespan.getBegin());
      assertEquals(timespan.getEnd(), matchingTimespan.getEnd());
      assertArrayEquals(timespan.getOwlSameAs(), matchingTimespan.getOwlSameAs());
    });
  }

  private void assertConcepts(FullBeanImpl tombstone) {
    final List<ConceptImpl> publishedConcepts = publishedFullbean.getConcepts();
    final List<ConceptImpl> tombstoneConcepts = tombstone.getConcepts();
    assertEquals(publishedConcepts.size(), tombstoneConcepts.size());
    publishedConcepts.forEach(concept -> {
      final ConceptImpl matchingConcept = tombstoneConcepts.stream().filter(
          tombstoneAgent -> tombstoneAgent.getAbout().equals(concept.getAbout())).findFirst().orElse(null);
      assertNotNull(matchingConcept);
      assertEquals(concept.getAbout(), matchingConcept.getAbout());
      assertEquals(concept.getPrefLabel(), matchingConcept.getPrefLabel());
      assertArrayEquals(concept.getExactMatch(), matchingConcept.getExactMatch());
    });
  }

  private void assertLicenses(FullBeanImpl tombstone) {
    final List<LicenseImpl> publishedLicenses = publishedFullbean.getLicenses();
    final List<LicenseImpl> tombstoneLicences = tombstone.getLicenses();
    assertEquals(publishedLicenses.size(), tombstoneLicences.size());
    publishedLicenses.forEach(license -> {
      final LicenseImpl matchingLicense = tombstoneLicences.stream().filter(
          tombstoneAgent -> tombstoneAgent.getAbout().equals(license.getAbout())).findFirst().orElse(null);
      assertNotNull(matchingLicense);
      assertEquals(license.getAbout(), matchingLicense.getAbout());
      assertEquals(license.getOdrlInheritFrom(), matchingLicense.getOdrlInheritFrom());
    });
  }
}

