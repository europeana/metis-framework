package eu.europeana.enrichment.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.service.dao.EnrichmentDao;
import eu.europeana.enrichment.api.external.model.EnrichmentTerm;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.metis.mongo.MongoClientProvider;
import eu.europeana.metis.mongo.MongoProperties;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityService implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityService.class);

  private final EnrichmentDao enrichmentDao;

  /**
   * Constructor.
   *
   * @param mongoHost The host to connect to.
   * @param mongoPort The port to connect to.
   * @param mongoDatabase The database to connect to.
   */
  public EntityService(String mongoHost, int mongoPort, String mongoDatabase) {
    this.enrichmentDao = getEnrichmentDao(mongoHost, mongoPort, mongoDatabase);
  }

  /**
   * Constructor.
   *
   * @param mongoConnectionUrl A valid mongo connection URL.
   * @param mongoDatabase The database to connect to.
   */
  public EntityService(String mongoConnectionUrl, String mongoDatabase) {
    this.enrichmentDao = new EnrichmentDao(new MongoClient(new MongoClientURI(mongoConnectionUrl)),
        mongoDatabase);
  }

  private EnrichmentDao getEnrichmentDao(String mongoHost, int mongoPort, String mongoDatabase) {
    final MongoProperties<IllegalArgumentException> mongoProperties = new MongoProperties<>(
        IllegalArgumentException::new);
    mongoProperties.setMongoHosts(new String[]{mongoHost}, new int[]{mongoPort});
    final MongoClient mongoClient = new MongoClientProvider<>(mongoProperties).createMongoClient();
    return new EnrichmentDao(mongoClient, mongoDatabase);
  }

  @Override
  public void close() {
    this.enrichmentDao.close();
  }

  public OrganizationImpl storeOrganization(OrganizationImpl organization,
      Date created, Date updated) {

    final EnrichmentTerm enrichmentTerm = organizationImplToEnrichmentTerm(organization, created,
        updated);

    // TODO: 8/6/20 Use only id projection here.
    final EnrichmentTerm storedEnrichmentTerm = enrichmentDao
        .getEnrichmentTermByField(EnrichmentDao.CODE_URI_FIELD,
            organization.getAbout());

    if (storedEnrichmentTerm != null) {
      enrichmentTerm.setId(storedEnrichmentTerm.getId());
    }

    //Save term list
    final String id = enrichmentDao.saveEnrichmentTerm(enrichmentTerm);
    return (OrganizationImpl) enrichmentDao.getEnrichmentTermByField(EnrichmentDao.ID_FIELD, id)
        .getContextualEntity();
  }

  /**
   * This method retrieves organization roles from given organization object
   *
   * @param organization The OrganizationImpl object
   * @return list of organization roles
   */
  public List<String> getOrganizationRoles(OrganizationImpl organization) {
    return organization.getEdmEuropeanaRole().get(Locale.ENGLISH.toString());
  }


  /**
   * This method returns organization stored in database by given organization
   *
   * @param uri The EDM organization uri (codeUri)
   * @return OrganizationImpl object
   */
  public OrganizationImpl getOrganizationByUri(String uri) {
    final EnrichmentTerm enrichmentTerm = enrichmentDao
        .getEnrichmentTermByField(EnrichmentDao.CODE_URI_FIELD, uri);
    return enrichmentTerm == null ? null : (OrganizationImpl) enrichmentTerm.getContextualEntity();
  }

  /**
   * This method returns the list of ids for existing organizations from database
   *
   * @param organizationIds The organization IDs to search for
   * @return list of ids for existing organization
   */
  public List<String> findExistingOrganizations(List<String> organizationIds) {
    List<String> res = new ArrayList<>();
    for (String id : organizationIds) {
      OrganizationImpl organization = getOrganizationByUri(id);
      if (organization != null) {
        res.add(organization.getAbout());
      }
    }
    return res;
  }

  /**
   * This method removes organizations from database by given URL
   *
   * @param organizationIds The organization IDs
   */
  public void deleteOrganizations(List<String> organizationIds) {
    enrichmentDao.deleteEnrichmentTerms(EntityType.ORGANIZATION, organizationIds);
  }

  /**
   * This method removes organization from database by given URL
   *
   * @param organizationId The organization ID
   */
  public void deleteOrganization(String organizationId) {
    enrichmentDao.deleteEnrichmentTerms(EntityType.ORGANIZATION,
        Collections.singletonList(organizationId));
  }

  private EnrichmentTerm organizationImplToEnrichmentTerm(OrganizationImpl organization,
      Date created, Date updated) {
    final EnrichmentTerm enrichmentTerm = new EnrichmentTerm();
    enrichmentTerm.setCodeUri(organization.getAbout());
    enrichmentTerm.setContextualEntity(organization);
    enrichmentTerm.setEntityType(EntityType.ORGANIZATION);
    enrichmentTerm.setCreated(Objects.requireNonNullElseGet(created, Date::new));
    enrichmentTerm.setUpdated(updated);

    return enrichmentTerm;
  }

  /**
   * This method returns last modified date for organizations.
   *
   * @return the last modified date
   */
  public Date getLastOrganizationImportDate() {
    return enrichmentDao.getDateOfLastUpdatedEnrichmentTerm(EntityType.ORGANIZATION);
  }
}
