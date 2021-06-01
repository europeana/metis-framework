package eu.europeana.enrichment.service.dao;

import static eu.europeana.enrichment.service.EnrichmentObjectUtils.areHashMapsWithListValuesEqual;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import eu.europeana.enrichment.internal.model.AbstractEnrichmentEntity;
import eu.europeana.enrichment.internal.model.AgentEnrichmentEntity;
import eu.europeana.enrichment.internal.model.ConceptEnrichmentEntity;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.internal.model.OrganizationEnrichmentEntity;
import eu.europeana.enrichment.internal.model.PlaceEnrichmentEntity;
import eu.europeana.enrichment.internal.model.TimespanEnrichmentEntity;
import eu.europeana.enrichment.service.EnrichmentObjectUtils;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class EnrichmentDaoTest {

  private static EmbeddedLocalhostMongo embeddedLocalhostMongo;
  private static EnrichmentDao enrichmentMongoDao;
  private static EnrichmentObjectUtils enrichmentObjectUtils;

  @BeforeAll
  static void prepare() {
    embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    MongoClient mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    enrichmentMongoDao = new EnrichmentDao(mongoClient, "enrichment-test");
    enrichmentObjectUtils = new EnrichmentObjectUtils();
  }

  @AfterAll
  static void destroy() {
    embeddedLocalhostMongo.stop();
  }

  @BeforeEach
  void setUp() {
    saveEnrichmentTerms();
  }

  @AfterEach
  void cleanUp() {
    enrichmentMongoDao.purgeDatabase();
  }

  @Test
  void saveEnrichmentTerm() throws Exception {
    assertProvidedWithStoredEnrichmentTerm(enrichmentObjectUtils.conceptTerm1, EntityType.CONCEPT);
    assertProvidedWithStoredEnrichmentTerm(enrichmentObjectUtils.timespanTerm1,
        EntityType.TIMESPAN);
    assertProvidedWithStoredEnrichmentTerm(enrichmentObjectUtils.agentTerm1, EntityType.AGENT);
    assertProvidedWithStoredEnrichmentTerm(enrichmentObjectUtils.placeTerm1, EntityType.PLACE);
    assertProvidedWithStoredEnrichmentTerm(enrichmentObjectUtils.organizationTerm1,
        EntityType.ORGANIZATION);

    assertProvidedWithStoredEnrichmentTerm(enrichmentObjectUtils.customConceptTerm,
        EntityType.CONCEPT);
    assertProvidedWithStoredEnrichmentTerm(enrichmentObjectUtils.customTimespanTerm,
        EntityType.TIMESPAN);
    assertProvidedWithStoredEnrichmentTerm(enrichmentObjectUtils.customAgentTerm, EntityType.AGENT);
    assertProvidedWithStoredEnrichmentTerm(enrichmentObjectUtils.customPlaceTerm, EntityType.PLACE);
    assertProvidedWithStoredEnrichmentTerm(enrichmentObjectUtils.customOrganizationTerm,
        EntityType.ORGANIZATION);
  }

  @Test
  void getEnrichmentTermByField() throws Exception {
    //Check with ENTITY_ABOUT_FIELD
    Optional<EnrichmentTerm> storedEnrichmentTerm = enrichmentMongoDao
        .getEnrichmentTermByField(EnrichmentDao.ENTITY_ABOUT_FIELD,
            enrichmentObjectUtils.conceptTerm1.getEnrichmentEntity().getAbout());
    assertTrue(storedEnrichmentTerm.isPresent());
    assertEnrichmentTerm(enrichmentObjectUtils.conceptTerm1, storedEnrichmentTerm.get(),
        EntityType.CONCEPT);

    //Check with ID_FIELD
    storedEnrichmentTerm = enrichmentMongoDao
        .getEnrichmentTermByField(EnrichmentDao.ENTITY_ABOUT_FIELD,
            enrichmentObjectUtils.timespanTerm1.getEnrichmentEntity().getAbout());
    assertTrue(storedEnrichmentTerm.isPresent());
    storedEnrichmentTerm = enrichmentMongoDao
        .getEnrichmentTermByField(EnrichmentDao.ID_FIELD, storedEnrichmentTerm.get().getId());
    assertTrue(storedEnrichmentTerm.isPresent());
    assertEnrichmentTerm(enrichmentObjectUtils.timespanTerm1, storedEnrichmentTerm.get(),
        EntityType.TIMESPAN);

    //Check with ENTITY_TYPE_FIELD
    storedEnrichmentTerm = enrichmentMongoDao
        .getEnrichmentTermByField(EnrichmentDao.ENTITY_TYPE_FIELD,
            enrichmentObjectUtils.timespanTerm1.getEntityType().toString());
    assertTrue(storedEnrichmentTerm.isPresent());
    assertEnrichmentTerm(enrichmentObjectUtils.timespanTerm1, storedEnrichmentTerm.get(),
        EntityType.TIMESPAN);

    //Check with ENTITY_OWL_SAME_AS_FIELD
    storedEnrichmentTerm = enrichmentMongoDao
        .getEnrichmentTermByField(EnrichmentDao.ENTITY_OWL_SAME_AS_FIELD,
            enrichmentObjectUtils.agentTerm1.getEnrichmentEntity().getOwlSameAs().get(0));
    assertTrue(storedEnrichmentTerm.isPresent());
    assertEnrichmentTerm(enrichmentObjectUtils.agentTerm1, storedEnrichmentTerm.get(),
        EntityType.AGENT);

    //Empty result with non existent ENTITY_ABOUT_FIELD
    storedEnrichmentTerm = enrichmentMongoDao
        .getEnrichmentTermByField(EnrichmentDao.ENTITY_ABOUT_FIELD, "NonExistentAbout");
    assertTrue(storedEnrichmentTerm.isEmpty());

  }

  @Test
  void getEnrichmentTermObjectIdByField() {
    //Check with ENTITY_ABOUT_FIELD
    Optional<ObjectId> objectIdOptional = enrichmentMongoDao
        .getEnrichmentTermObjectIdByField(EnrichmentDao.ENTITY_ABOUT_FIELD,
            enrichmentObjectUtils.conceptTerm1.getEnrichmentEntity().getAbout());
    assertTrue(objectIdOptional.isPresent());

    //Check with ID_FIELD
    objectIdOptional = enrichmentMongoDao
        .getEnrichmentTermObjectIdByField(EnrichmentDao.ENTITY_ABOUT_FIELD,
            enrichmentObjectUtils.timespanTerm1.getEnrichmentEntity().getAbout());
    assertTrue(objectIdOptional.isPresent());
    objectIdOptional = enrichmentMongoDao
        .getEnrichmentTermObjectIdByField(EnrichmentDao.ID_FIELD, objectIdOptional.get());
    assertTrue(objectIdOptional.isPresent());

    //Check with ENTITY_TYPE_FIELD
    objectIdOptional = enrichmentMongoDao
        .getEnrichmentTermObjectIdByField(EnrichmentDao.ENTITY_TYPE_FIELD,
            enrichmentObjectUtils.timespanTerm1.getEntityType().toString());
    assertTrue(objectIdOptional.isPresent());

    //Check with ENTITY_OWL_SAME_AS_FIELD
    objectIdOptional = enrichmentMongoDao
        .getEnrichmentTermObjectIdByField(EnrichmentDao.ENTITY_OWL_SAME_AS_FIELD,
            enrichmentObjectUtils.agentTerm1.getEnrichmentEntity().getOwlSameAs().get(0));
    assertTrue(objectIdOptional.isPresent());

    //Empty result with non existent ENTITY_ABOUT_FIELD
    objectIdOptional = enrichmentMongoDao
        .getEnrichmentTermObjectIdByField(EnrichmentDao.ENTITY_ABOUT_FIELD, "NonExistentAbout");
    assertTrue(objectIdOptional.isEmpty());
  }

  @Test
  void getAllEnrichmentTermsByFields() throws Exception {
    //Search on simple fields
    final Pair<String, String> parameterAbout = new ImmutablePair<>(
        EnrichmentDao.ENTITY_ABOUT_FIELD,
        enrichmentObjectUtils.customAgentTerm.getEnrichmentEntity().getAbout());
    final Pair<String, String> parameterEntityType = new ImmutablePair<>(
        EnrichmentDao.ENTITY_TYPE_FIELD, EntityType.AGENT.name());
    final List<Pair<String, String>> parametersAbout = new ArrayList<>();
    parametersAbout.add(parameterAbout);
    parametersAbout.add(parameterEntityType);
    final HashMap<String, List<Pair<String, String>>> fieldNameMap = new HashMap<>();
    fieldNameMap.put(null, parametersAbout);
    List<EnrichmentTerm> allEnrichmentTermsByFields = enrichmentMongoDao
        .getAllEnrichmentTermsByFields(fieldNameMap);
    assertEquals(1, allEnrichmentTermsByFields.size());
    assertEnrichmentTerm(enrichmentObjectUtils.customAgentTerm, allEnrichmentTermsByFields.get(0),
        EntityType.AGENT);

    //Search also on list field with internal fields
    final List<Pair<String, String>> labelInfosFields = new ArrayList<>();
    labelInfosFields.add(new ImmutablePair<>(EnrichmentDao.LABEL_FIELD, "margaret lee"));
    labelInfosFields.add(new ImmutablePair<>(EnrichmentDao.LANG_FIELD, "en"));
    fieldNameMap.put(EnrichmentDao.LABEL_INFOS_FIELD, labelInfosFields);
    allEnrichmentTermsByFields = enrichmentMongoDao.getAllEnrichmentTermsByFields(fieldNameMap);
    assertEquals(1, allEnrichmentTermsByFields.size());
    assertEnrichmentTerm(enrichmentObjectUtils.customAgentTerm, allEnrichmentTermsByFields.get(0),
        EntityType.AGENT);
  }

  @Test
  void getAllEnrichmentTermsByFieldsInList() throws Exception {
    //Search one term
    List<String> owlSameAsValuesList = new ArrayList<>();
    owlSameAsValuesList.add("http://pl.dbpedia.org/resource/Kartka_pocztowa");

    final Pair<String, List<String>> parameterOwlSameAs = new ImmutablePair<>(
        EnrichmentDao.ENTITY_OWL_SAME_AS_FIELD, owlSameAsValuesList);
    List<Pair<String, List<String>>> fieldNameList = new ArrayList<>();
    fieldNameList.add(parameterOwlSameAs);
    List<EnrichmentTerm> allEnrichmentTermsByFieldsInList = enrichmentMongoDao
        .getAllEnrichmentTermsByFieldsInList(fieldNameList);
    assertEquals(1, allEnrichmentTermsByFieldsInList.size());
    assertEnrichmentTerm(enrichmentObjectUtils.customConceptTerm,
        allEnrichmentTermsByFieldsInList.get(0), EntityType.CONCEPT);

    //Search for two terms
    owlSameAsValuesList.add("http://wikidata.dbpedia.org/resource/Q162530");
    owlSameAsValuesList.add("NonExistentValue");
    allEnrichmentTermsByFieldsInList = enrichmentMongoDao
        .getAllEnrichmentTermsByFieldsInList(fieldNameList);
    assertEquals(2, allEnrichmentTermsByFieldsInList.size());
    final Optional<EnrichmentTerm> agentEnrichmentTerm = allEnrichmentTermsByFieldsInList.stream()
        .filter(enrichmentTerm -> enrichmentTerm.getEntityType() == EntityType.AGENT).findFirst();
    assertTrue(agentEnrichmentTerm.isPresent());
    assertEnrichmentTerm(enrichmentObjectUtils.agentTerm1, agentEnrichmentTerm.get(),
        EntityType.AGENT);

    final Optional<EnrichmentTerm> conceptEnrichmentTerm = allEnrichmentTermsByFieldsInList.stream()
        .filter(enrichmentTerm -> enrichmentTerm.getEntityType() == EntityType.CONCEPT).findFirst();
    assertTrue(conceptEnrichmentTerm.isPresent());
    assertEnrichmentTerm(enrichmentObjectUtils.customConceptTerm, conceptEnrichmentTerm.get(),
        EntityType.CONCEPT);
  }

  @Test
  void getDateOfLastUpdatedEnrichmentTerm() {
    //Find latest organization(dates are only present in organizations)
    final Date dateOfLastUpdatedEnrichmentTerm = enrichmentMongoDao
        .getDateOfLastUpdatedEnrichmentTerm(EntityType.ORGANIZATION);
    Instant i = Instant.parse("2018-11-19T18:45:34.000Z");
    Date expectedDate = Date.from(i);
    assertEquals(expectedDate, dateOfLastUpdatedEnrichmentTerm);
  }

  @Test
  void deleteEnrichmentTerms() {
    //Fake the connection between two items
    final String aboutTerm1 = enrichmentObjectUtils.agentTerm1.getEnrichmentEntity().getAbout();
    final List<String> owlSameAs = Optional.ofNullable(enrichmentObjectUtils.customAgentTerm.getEnrichmentEntity()
        .getOwlSameAs()).orElse(new ArrayList<>());
    owlSameAs.add(aboutTerm1);
    enrichmentObjectUtils.customAgentTerm.getEnrichmentEntity().setOwlSameAs(owlSameAs);
    enrichmentMongoDao.saveEnrichmentTerm(enrichmentObjectUtils.customAgentTerm);
    long totalDocuments = enrichmentMongoDao.count();
    assertEquals(10, totalDocuments);

    //Now both items should be removed
    assertEquals(1, enrichmentMongoDao.deleteEnrichmentTerms(EntityType.AGENT,
        List.of(enrichmentObjectUtils.agentTerm1.getEnrichmentEntity().getAbout())).size());
    totalDocuments = enrichmentMongoDao.count();
    assertEquals(8, totalDocuments);
  }

  private void saveEnrichmentTerms() {
    enrichmentMongoDao.saveEnrichmentTerm(enrichmentObjectUtils.conceptTerm1);
    enrichmentMongoDao.saveEnrichmentTerm(enrichmentObjectUtils.timespanTerm1);
    enrichmentMongoDao.saveEnrichmentTerm(enrichmentObjectUtils.agentTerm1);
    enrichmentMongoDao.saveEnrichmentTerm(enrichmentObjectUtils.placeTerm1);
    enrichmentMongoDao.saveEnrichmentTerm(enrichmentObjectUtils.organizationTerm1);
    enrichmentMongoDao.saveEnrichmentTerm(enrichmentObjectUtils.customConceptTerm);
    enrichmentMongoDao.saveEnrichmentTerm(enrichmentObjectUtils.customTimespanTerm);
    enrichmentMongoDao.saveEnrichmentTerm(enrichmentObjectUtils.customAgentTerm);
    enrichmentMongoDao.saveEnrichmentTerm(enrichmentObjectUtils.customPlaceTerm);
    enrichmentMongoDao.saveEnrichmentTerm(enrichmentObjectUtils.customOrganizationTerm);
  }

  void assertProvidedWithStoredEnrichmentTerm(EnrichmentTerm enrichmentTerm, EntityType entityType)
      throws Exception {
    Optional<EnrichmentTerm> storedEnrichmentTerm = enrichmentMongoDao
        .getEnrichmentTermByField(EnrichmentDao.ENTITY_ABOUT_FIELD,
            enrichmentTerm.getEnrichmentEntity().getAbout());
    assertTrue(storedEnrichmentTerm.isPresent());
    assertEnrichmentTerm(enrichmentTerm, storedEnrichmentTerm.get(), entityType);
  }

  void assertEnrichmentTerm(EnrichmentTerm expected, EnrichmentTerm actual, EntityType entityType)
      throws Exception {
    assertNotNull(actual.getId());
    assertEquals(expected.getEntityType(), actual.getEntityType());
    assertEquals(expected.getParent(), actual.getParent());
    assertEquals(expected.getUpdated(), actual.getUpdated());
    assertEquals(expected.getCreated(), actual.getCreated());
    assertEquals(expected.getLabelInfos(), actual.getLabelInfos());
    final AbstractEnrichmentEntity expectedAbstract = expected.getEnrichmentEntity();
    final AbstractEnrichmentEntity actualAbstract = actual.getEnrichmentEntity();
    //Check abstract level content
    assertAbstractEnrichmentEntity(expectedAbstract, actualAbstract);
    switch (entityType) {
      case CONCEPT:
        assertConcept((ConceptEnrichmentEntity) expectedAbstract,
            (ConceptEnrichmentEntity) actualAbstract);
        break;
      case TIMESPAN:
        assertTimespan((TimespanEnrichmentEntity) expectedAbstract,
            (TimespanEnrichmentEntity) actualAbstract);
        break;
      case AGENT:
        assertAgent((AgentEnrichmentEntity) expectedAbstract,
            (AgentEnrichmentEntity) actualAbstract);
        break;
      case PLACE:
        assertPlace((PlaceEnrichmentEntity) expectedAbstract,
            (PlaceEnrichmentEntity) actualAbstract);
        break;
      case ORGANIZATION:
        assertOrganization((OrganizationEnrichmentEntity) expectedAbstract,
            (OrganizationEnrichmentEntity) actualAbstract);
        break;
      default:
        throw new Exception("Invalid entity type value: " + entityType);
    }
  }

  void assertConcept(ConceptEnrichmentEntity expected, ConceptEnrichmentEntity actual) {
    assertArrayEquals(expected.getBroader(), actual.getBroader());
    assertArrayEquals(expected.getNarrower(), actual.getNarrower());
    assertArrayEquals(expected.getRelated(), actual.getRelated());
    assertArrayEquals(expected.getBroadMatch(), actual.getBroadMatch());
    assertArrayEquals(expected.getNarrowMatch(), actual.getNarrowMatch());
    assertArrayEquals(expected.getRelatedMatch(), actual.getRelatedMatch());
    assertArrayEquals(expected.getExactMatch(), actual.getExactMatch());
    assertArrayEquals(expected.getCloseMatch(), actual.getCloseMatch());
    assertTrue(areHashMapsWithListValuesEqual(expected.getNotation(), actual.getNotation()));
    assertArrayEquals(expected.getInScheme(), actual.getInScheme());
  }

  void assertTimespan(TimespanEnrichmentEntity expected, TimespanEnrichmentEntity actual) {
    assertTrue(areHashMapsWithListValuesEqual(expected.getBegin(), actual.getBegin()));
    assertTrue(areHashMapsWithListValuesEqual(expected.getEnd(), actual.getEnd()));
    assertTrue(
        areHashMapsWithListValuesEqual(expected.getDctermsHasPart(), actual.getDctermsHasPart()));
    assertEquals(expected.getIsNextInSequence(), actual.getIsNextInSequence());
  }

  void assertAgent(AgentEnrichmentEntity expected, AgentEnrichmentEntity actual) {
    assertTrue(areHashMapsWithListValuesEqual(expected.getBegin(), actual.getBegin()));
    assertTrue(areHashMapsWithListValuesEqual(expected.getEnd(), actual.getEnd()));
    assertArrayEquals(expected.getEdmWasPresentAt(), actual.getEdmWasPresentAt());
    assertArrayEquals(expected.getEdmHasMet(), actual.getEdmHasMet());
    assertTrue(
        areHashMapsWithListValuesEqual(expected.getEdmIsRelatedTo(), actual.getEdmIsRelatedTo()));
    assertTrue(areHashMapsWithListValuesEqual(expected.getFoafName(), actual.getFoafName()));
    assertTrue(areHashMapsWithListValuesEqual(expected.getDcDate(), actual.getDcDate()));
    assertTrue(
        areHashMapsWithListValuesEqual(expected.getDcIdentifier(), actual.getDcIdentifier()));
    assertTrue(areHashMapsWithListValuesEqual(expected.getRdaGr2DateOfBirth(),
        actual.getRdaGr2DateOfBirth()));
    assertTrue(areHashMapsWithListValuesEqual(expected.getRdaGr2DateOfDeath(),
        actual.getRdaGr2DateOfDeath()));
    assertTrue(areHashMapsWithListValuesEqual(expected.getRdaGr2PlaceOfBirth(),
        actual.getRdaGr2PlaceOfBirth()));
    assertTrue(areHashMapsWithListValuesEqual(expected.getRdaGr2PlaceOfDeath(),
        actual.getRdaGr2PlaceOfDeath()));
    assertTrue(areHashMapsWithListValuesEqual(expected.getRdaGr2DateOfEstablishment(),
        actual.getRdaGr2DateOfEstablishment()));
    assertTrue(areHashMapsWithListValuesEqual(expected.getRdaGr2DateOfTermination(),
        actual.getRdaGr2DateOfTermination()));
    assertTrue(
        areHashMapsWithListValuesEqual(expected.getRdaGr2Gender(), actual.getRdaGr2Gender()));
    assertTrue(areHashMapsWithListValuesEqual(expected.getRdaGr2ProfessionOrOccupation(),
        actual.getRdaGr2ProfessionOrOccupation()));
    assertTrue(areHashMapsWithListValuesEqual(expected.getRdaGr2BiographicalInformation(),
        actual.getRdaGr2BiographicalInformation()));
  }

  void assertPlace(PlaceEnrichmentEntity expected, PlaceEnrichmentEntity actual) {
    assertEquals(expected.getLatitude(), actual.getLatitude());
    assertEquals(expected.getLongitude(), actual.getLongitude());
    assertEquals(expected.getAltitude(), actual.getAltitude());
    assertEquals(expected.getPosition(), actual.getPosition());
    assertTrue(
        areHashMapsWithListValuesEqual(expected.getDcTermsHasPart(), actual.getDcTermsHasPart()));
  }

  void assertOrganization(OrganizationEnrichmentEntity expected,
      OrganizationEnrichmentEntity actual) {

    assertEquals(expected.getRdfType(), actual.getRdfType());
    assertTrue(
        areHashMapsWithListValuesEqual(expected.getDcIdentifier(), actual.getDcIdentifier()));
    assertEquals(expected.getDcDescription(), actual.getDcDescription());
    assertTrue(areHashMapsWithListValuesEqual(expected.getEdmAcronym(), actual.getEdmAcronym()));

    assertEquals(expected.getFoafLogo(), actual.getFoafLogo());
    assertEquals(expected.getFoafHomepage(), actual.getFoafHomepage());
    assertEquals(expected.getFoafPhone(), actual.getFoafPhone());
    assertEquals(expected.getFoafMbox(), actual.getFoafMbox());
    assertTrue(areHashMapsWithListValuesEqual(expected.getEdmEuropeanaRole(),
        actual.getEdmEuropeanaRole()));

    assertEquals(expected.getEdmOrganizationDomain(), actual.getEdmOrganizationDomain());
    assertEquals(expected.getEdmOrganizationSector(), actual.getEdmOrganizationSector());
    assertEquals(expected.getEdmOrganizationScope(), actual.getEdmOrganizationScope());
    assertEquals(expected.getEdmGeographicLevel(), actual.getEdmGeographicLevel());
    assertEquals(expected.getEdmCountry(), actual.getEdmCountry());
    assertEquals(expected.getAddress(), actual.getAddress());
  }

  void assertAbstractEnrichmentEntity(AbstractEnrichmentEntity expectedEnrichmentEntity,
      AbstractEnrichmentEntity actualEnrichmentEntity) {
    assertEquals(expectedEnrichmentEntity.getAbout(), actualEnrichmentEntity.getAbout());
    assertTrue(areHashMapsWithListValuesEqual(expectedEnrichmentEntity.getPrefLabel(),
        actualEnrichmentEntity.getPrefLabel()));
    assertTrue(areHashMapsWithListValuesEqual(expectedEnrichmentEntity.getAltLabel(),
        actualEnrichmentEntity.getAltLabel()));
    assertTrue(areHashMapsWithListValuesEqual(expectedEnrichmentEntity.getHiddenLabel(),
        actualEnrichmentEntity.getHiddenLabel()));
    assertTrue(areHashMapsWithListValuesEqual(expectedEnrichmentEntity.getNote(),
        actualEnrichmentEntity.getNote()));
    assertEquals(expectedEnrichmentEntity.getOwlSameAs(), actualEnrichmentEntity.getOwlSameAs());
    assertEquals(expectedEnrichmentEntity.getIsPartOf(), actualEnrichmentEntity.getIsPartOf());
    assertEquals(expectedEnrichmentEntity.getFoafDepiction(),
        actualEnrichmentEntity.getFoafDepiction());
  }
}