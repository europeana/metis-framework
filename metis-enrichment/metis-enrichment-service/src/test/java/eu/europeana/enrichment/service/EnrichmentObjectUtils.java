package eu.europeana.enrichment.service;

import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import eu.europeana.enrichment.internal.model.EnrichmentTerm;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import eu.europeana.metis.mongo.embedded.EmbeddedLocalhostMongo;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.apache.commons.collections.MapUtils;
import org.bson.Document;
import org.springframework.util.CollectionUtils;

public class EnrichmentObjectUtils {

  private static final EnrichmentDao enrichmentMongoDao;

  public static final String DIRECTORY_WITH_EXAMPLES = "example_enrichment_entities/";
  public static final String SUBDIRECTORY_AGENTS = "agents/";
  public static final String SUBDIRECTORY_PLACES = "places/";
  public static final String SUBDIRECTORY_CONCEPTS = "concepts/";
  public static final String SUBDIRECTORY_TIMESPANS = "timespans/";
  public static final String SUBDIRECTORY_ORGANIZATIONS = "organizations/";

  public static EnrichmentTerm conceptTerm1;
  public static EnrichmentTerm timespanTerm1;
  public static EnrichmentTerm agentTerm1;
  public static EnrichmentTerm placeTerm1;
  public static EnrichmentTerm organizationTerm1;
  public static EnrichmentTerm customConceptTerm;
  public static EnrichmentTerm customTimespanTerm;
  public static EnrichmentTerm customAgentTerm;
  public static EnrichmentTerm customPlaceTerm;
  public static EnrichmentTerm customOrganizationTerm;

  static {
    EmbeddedLocalhostMongo embeddedLocalhostMongo = new EmbeddedLocalhostMongo();
    embeddedLocalhostMongo.start();
    String mongoHost = embeddedLocalhostMongo.getMongoHost();
    int mongoPort = embeddedLocalhostMongo.getMongoPort();
    MongoClient mongoClient = MongoClients
        .create(String.format("mongodb://%s:%s", mongoHost, mongoPort));
    enrichmentMongoDao = new EnrichmentDao(mongoClient, "enrichment-test");
    //One time read all examples terms from files
    try {
      readEnrichmentTermsFromFile();
    } catch (IOException e) {
      throw new IllegalStateException(e);
    }
    embeddedLocalhostMongo.stop();
  }

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

  public static <T, S> boolean areHashMapsWithListValuesEqual(Map<T, List<S>> first,
      Map<T, List<S>> second) {
    if (MapUtils.isEmpty(first) && MapUtils.isEmpty(second)) {
      return true;
    }
    if (first == null || second == null || first.size() != second.size()) {
      return false;
    }
    return first.entrySet().stream()
        .allMatch(e -> areListsEqual(e.getValue(), second.get(e.getKey())));
  }

  public static <S> boolean areListsEqual(List<S> first, List<S> second) {
    if (CollectionUtils.isEmpty(first) && CollectionUtils.isEmpty(second)) {
      return true;
    }
    if (first == null || second == null || first.size() != second.size()) {
      return false;
    }
    return first.equals(second);
  }

  public static void readEnrichmentTermsFromFile() throws IOException {
    conceptTerm1 = getEnrichmentTermFromJsonFile(SUBDIRECTORY_CONCEPTS, "concept1.json");
    timespanTerm1 = getEnrichmentTermFromJsonFile(SUBDIRECTORY_TIMESPANS, "timespan1.json");
    agentTerm1 = getEnrichmentTermFromJsonFile(SUBDIRECTORY_AGENTS, "agent1.json");
    placeTerm1 = getEnrichmentTermFromJsonFile(SUBDIRECTORY_PLACES, "place1.json");
    organizationTerm1 = getEnrichmentTermFromJsonFile(SUBDIRECTORY_ORGANIZATIONS,
        "organization1.json");
    customConceptTerm = getEnrichmentTermFromJsonFile(SUBDIRECTORY_CONCEPTS, "custom_concept.json");
    customTimespanTerm = getEnrichmentTermFromJsonFile(SUBDIRECTORY_TIMESPANS, "custom_timespan.json");
    customAgentTerm = getEnrichmentTermFromJsonFile(SUBDIRECTORY_AGENTS, "custom_agent.json");
    customPlaceTerm = getEnrichmentTermFromJsonFile(SUBDIRECTORY_PLACES, "custom_place.json");
    customOrganizationTerm = getEnrichmentTermFromJsonFile(SUBDIRECTORY_ORGANIZATIONS,
        "custom_organization.json");
  }

  static EnrichmentTerm getEnrichmentTermFromJsonFile(String directory, String filename)
      throws IOException {
    final Document conceptTerm = getDocument(directory + filename);
    return enrichmentMongoDao.getMapper().fromDocument(EnrichmentTerm.class, conceptTerm);
  }
}
