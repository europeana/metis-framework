package eu.europeana.enrichment.service;

import java.util.Date;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.utils.EntityClass;
import eu.europeana.enrichment.utils.MongoDatabaseUtils;

public class EntityService {

	private final String mongoHost;
	private final int mongoPort;
	private static final Logger LOGGER = LoggerFactory
			.getLogger(EntityService.class);

	public EntityService(String mongoHost, int mongoPort) {
		this.mongoHost = mongoHost;
		this.mongoPort = mongoPort;
	}

	public OrganizationTermList storeOrganization(Organization org) {
		MongoDatabaseUtils.dbExists(mongoHost, mongoPort);

		// delete old references
		MongoDatabaseUtils.deleteOrganizations(org.getAbout());

		// build term list
		OrganizationTermList termList = organizationToOrganizationTermList(
				(OrganizationImpl) org);
		Date now = new Date();
		termList.setCreated(now);
		termList.setModified(now);

		// store labels
		int newLabels = MongoDatabaseUtils.storeEntityLabels(
				(OrganizationImpl) org, EntityClass.ORGANIZATION);
		LOGGER.trace("{}", "Stored new lables: " + newLabels);

		// store term list
		return (OrganizationTermList) MongoDatabaseUtils
				.insertMongoTermList(termList);
	}

	private OrganizationTermList organizationToOrganizationTermList(
			OrganizationImpl organization) {
		OrganizationTermList termList = new OrganizationTermList();
		if (organization.getPrefLabel() == null
				|| organization.getPrefLabel().entrySet().size() == 0)
			return null;
		termList.setCodeUri(organization.getAbout());
		termList.setRepresentation(organization);
		termList.setEntityType(OrganizationImpl.class.getSimpleName());
		return termList;
	}

	/**
	 * This method returns last modified date for organizations.
	 * 
	 * @return the last modified date
	 */
	public Date getLastOrganizationImportDate() {
		MongoDatabaseUtils.dbExists(mongoHost, mongoPort);
		return MongoDatabaseUtils.getLastModifiedDate(EntityClass.ORGANIZATION);
	}

}
