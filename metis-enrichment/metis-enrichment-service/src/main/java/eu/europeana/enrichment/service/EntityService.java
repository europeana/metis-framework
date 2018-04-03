package eu.europeana.enrichment.service;

import java.io.Closeable;
import java.io.IOException;
import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.ContextualClassImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.utils.EntityClass;
import eu.europeana.enrichment.utils.EnrichmentEntityDao;

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
		return (OrganizationTermList) entityDao
				.storeMongoTermList(termList);

	}

	private OrganizationTermList organizationToOrganizationTermList(
			OrganizationImpl organization, Date created, Date modified) {
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
