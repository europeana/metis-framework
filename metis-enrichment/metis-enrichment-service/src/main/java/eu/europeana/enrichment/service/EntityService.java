package eu.europeana.enrichment.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.ContextualClassImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.utils.EnrichmentEntityDao;
import eu.europeana.enrichment.utils.EntityClass;

public class EntityService implements Closeable {

    private static final Logger LOGGER = LoggerFactory.getLogger(EntityService.class);
  
    private final EnrichmentEntityDao entityDao;

	public EntityService(String mongoHost, int mongoPort) {
	    this.entityDao = new EnrichmentEntityDao(mongoHost, mongoPort);
	}

    @Override
    public void close() throws IOException {
        this.entityDao.close();
    }
	  
	public OrganizationTermList storeOrganization(Organization org,
			Date created, Date modified) {

		// build term list
		OrganizationTermList termList = organizationToOrganizationTermList(
				(OrganizationImpl) org, created, modified);

		MongoTermList<ContextualClassImpl> storedOrg = entityDao
				.findByCode(org.getAbout(), EntityClass.ORGANIZATION);

		// it is an update
		if (storedOrg != null) {
			// set database id
			termList.setId(storedOrg.getId());
		}

		// delete old terms (labels), also when the termList is not found to
		// will avoid problems when manually deleting the entries in the
		// database
		entityDao.deleteOrganizationTerms(org.getAbout());

		// store labels
		int newLabels = entityDao.storeEntityLabels(
				(OrganizationImpl) org, EntityClass.ORGANIZATION);
		LOGGER.trace("Stored new lables: {}", newLabels);

		// store term list
    return (OrganizationTermList) entityDao.storeMongoTermList(termList);

  }

  /**
   * This method retrieves organization roles from given organization object
   * 
   * @param org The OrganizationImpl object
   * @return list of organization roles
   */
  public List<String> getOrganizationRoles(Organization org) {

    return ((OrganizationImpl) org).getEdmEuropeanaRole().get(Locale.ENGLISH.toString());
  }

  
  /**
   * This method returns organization stored in database by given organization
   * 
   * @param uri The EDM organization uri (codeUri)
   * @return OrganizationImpl object
   */
  public Organization getOrganizationById(String uri) {

    MongoTermList<ContextualClassImpl> storedOrg =
        entityDao.findByCode(uri, EntityClass.ORGANIZATION);
    if (storedOrg == null)
      return null;
    return ((OrganizationImpl) storedOrg.getRepresentation());
  }

  /**
   * This method returns the list of ids for existing organizations from database
   * 
   * @param organizationIds The organization IDs to search for
   * @return list of ids for existing organization
   */
  public List<String> findExistingOrganizations(List<String> organizationIds) {
    List<String> res = new ArrayList<String>();
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
    entityDao.delete(organizationIds);
  }

  /**
   * This method removes organization from database by given URL
   * 
   * @param organizationId The organization ID
   */
  public void deleteOrganization(String organizationId) {
    entityDao.deleteOrganizations(organizationId);
    entityDao.deleteOrganizationTerms(organizationId);
  }

  private OrganizationTermList organizationToOrganizationTermList(OrganizationImpl organization,
      Date created, Date modified) {
		OrganizationTermList termList = new OrganizationTermList();

		termList.setCodeUri(organization.getAbout());
		termList.setRepresentation(organization);
		termList.setEntityType(OrganizationImpl.class.getSimpleName());

		// enforce created not null
		if (created != null)
			termList.setCreated(created);
		else
			termList.setCreated(new Date());

		termList.setModified(modified);

		return termList;
	}

	/**
	 * This method returns last modified date for organizations.
	 * 
	 * @return the last modified date
	 */
	public Date getLastOrganizationImportDate() {
		return entityDao.getLastModifiedDate(EntityClass.ORGANIZATION);
	}
}
