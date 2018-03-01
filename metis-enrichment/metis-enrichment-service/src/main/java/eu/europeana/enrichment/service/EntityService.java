package eu.europeana.enrichment.service;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.mongojack.DBRef;

import eu.europeana.corelib.definitions.edm.entity.Organization;
import eu.europeana.corelib.solr.entity.ContextualClassImpl;
import eu.europeana.corelib.solr.entity.OrganizationImpl;
import eu.europeana.enrichment.api.internal.MongoTerm;
import eu.europeana.enrichment.api.internal.MongoTermList;
import eu.europeana.enrichment.api.internal.OrganizationTermList;
import eu.europeana.enrichment.utils.EntityClass;
import eu.europeana.enrichment.utils.MongoDatabaseUtils;

public class EntityService {

	private final String mongoHost;
    private final int mongoPort;
    
	public EntityService(String mongoHost, int mongoPort){
		this.mongoHost=mongoHost;
		this.mongoPort=mongoPort;
	}
	
	public OrganizationTermList storeOrganization(Organization org){
		MongoDatabaseUtils.dbExists(mongoHost, mongoPort);
		
		//delete old references
		MongoDatabaseUtils.deleteOrganizations(org.getAbout());
		
		//build term list
		OrganizationTermList termList = organizationToOrganizationTermList((OrganizationImpl) org);
		Date now = new Date();
		termList.setCreated(now);
		termList.setModified(now);
		
		//store labels
		List<DBRef<? extends MongoTerm, String>> terms = MongoDatabaseUtils.storeEntityLabels((OrganizationImpl)org, EntityClass.ORGANIZATION);
		termList.setTerms(terms);
		//store term list
		OrganizationTermList res = (OrganizationTermList) MongoDatabaseUtils.insertMongoTermList(termList);
		return res;
	}
	
	private OrganizationTermList organizationToOrganizationTermList(OrganizationImpl organization){
		OrganizationTermList termList = new OrganizationTermList();
		if (organization.getPrefLabel() == null || organization.getPrefLabel().entrySet().size()==0)
			return null;
		termList.setCodeUri(organization.getAbout());
		termList.setRepresentation(organization);
		termList.setEntityType(OrganizationImpl.class.getSimpleName());
		return termList;
	}
	
}
