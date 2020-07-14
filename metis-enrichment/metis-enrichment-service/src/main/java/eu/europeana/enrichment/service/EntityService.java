package eu.europeana.enrichment.service;

import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.utils.EntityType;
import eu.europeana.enrichment.utils.EntityDao;
import eu.europeana.metis.mongo.MongoClientProvider;
import eu.europeana.metis.mongo.MongoProperties;
import java.io.Closeable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EntityService implements Closeable {

  private static final Logger LOGGER = LoggerFactory.getLogger(EntityService.class);

  private final EntityDao entityDao;

  public EntityService(String mongoHost, int mongoPort, String mongoDatabase) {
    this.entityDao = getEntityDao(mongoHost, mongoPort, mongoDatabase);
  }

  public EntityService(String mongoConnectionUrl, String mongoDatabase) {
    this.entityDao = new EntityDao(new MongoClient(new MongoClientURI(mongoConnectionUrl)),
        mongoDatabase);
  }

  private EntityDao getEntityDao(String mongoHost, int mongoPort, String mongoDatabase) {
    final MongoProperties<IllegalArgumentException> mongoProperties = new MongoProperties<>(
        IllegalArgumentException::new);
    mongoProperties
        .setAllProperties(new String[]{mongoHost}, new int[]{mongoPort}, null,
            null, null, false, null);

    final MongoClient mongoClient = new MongoClientProvider<>(mongoProperties).createMongoClient();
    return new EntityDao(mongoClient, mongoDatabase);
  }

  @Override
  public void close() {
    this.entityDao.close();
  }

  public OrganizationTermList storeOrganization(Organization org,
      Date created, Date modified) {

    // build term list
    OrganizationTermList termList = organizationToOrganizationTermList(
        (OrganizationImpl) org, created, modified);

    final MongoTermList<OrganizationImpl> storedOrg = entityDao
        .findTermListByField(OrganizationTermList.class, EntityDao.CODE_URI_FIELD, org.getAbout());

    // it is an update
    if (storedOrg != null) {
      // set database id
      termList.setId(storedOrg.getId());
    }

    // delete old terms (labels), also when the termList is not found to
    // will avoid problems when manually deleting the entries in the
    // database
    entityDao.deleteMongoTerm(EntityDao.ORGANIZATION_TABLE, org.getAbout());

    // store labels
    int countOfStoredMongoTerms = entityDao.storeMongoTermsFromEntity(
        (OrganizationImpl) org, EntityType.ORGANIZATION);
    LOGGER.trace("Stored {} new mongo terms", countOfStoredMongoTerms);

    // store term list
    final String id = entityDao.saveTermList(termList);
    final MongoTermList<OrganizationImpl> storedOrganizationMongoTermList = entityDao
        .findTermListByField(OrganizationTermList.class, EntityDao.ID_FIELD, id);
    return (OrganizationTermList) storedOrganizationMongoTermList;

  }

  /**
   * This method retrieves organization roles from given organization object
   *
   * @param org The OrganizationImpl object
   * @return list of organization roles
   */
  public List<String> getOrganizationRoles(Organization org) {

    return org.getEdmEuropeanaRole().get(Locale.ENGLISH.toString());
  }


  /**
   * This method returns organization stored in database by given organization
   *
   * @param uri The EDM organization uri (codeUri)
   * @return OrganizationImpl object
   */
  public Organization getOrganizationById(String uri) {
    MongoTermList<OrganizationImpl> storedOrg =
        entityDao.findTermListByField(OrganizationTermList.class, EntityDao.CODE_URI_FIELD, uri);
    return storedOrg == null ? null : storedOrg.getRepresentation();
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
      Organization organization = getOrganizationById(id);
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
    entityDao.deleteAllEntitiesMatching(organizationIds);
  }

  /**
   * This method removes organization from database by given URL
   *
   * @param organizationId The organization ID
   */
  public void deleteOrganization(String organizationId) {
    entityDao
        .deleteEntities(EntityDao.ORGANIZATION_TABLE, EntityDao.ORGANIZATION_TYPE, organizationId);
  }

  private OrganizationTermList organizationToOrganizationTermList(OrganizationImpl organization,
      Date created, Date modified) {
    OrganizationTermList termList = new OrganizationTermList();

    termList.setCodeUri(organization.getAbout());
    termList.setRepresentation(organization);
    termList.setEntityType(OrganizationImpl.class.getSimpleName());

    // enforce created not null
    if (created == null) {
      termList.setCreated(new Date());
    } else {
      termList.setCreated(created);
    }

    termList.setModified(modified);

    return termList;
  }

  /**
   * This method returns last modified date for organizations.
   *
   * @return the last modified date
   */
  public Date getLastOrganizationImportDate() {
    return entityDao.getDateOfLastModifiedEntity(EntityType.ORGANIZATION);
  }
}
