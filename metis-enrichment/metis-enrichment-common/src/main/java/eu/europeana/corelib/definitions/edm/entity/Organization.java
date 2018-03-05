package eu.europeana.corelib.definitions.edm.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import eu.europeana.corelib.solr.entity.Address;

public interface Organization extends AbstractEdmEntity{
	
	Map<String, List<String>> getPrefLabel();
	
	void setPrefLabel(Map<String, List<String>> prefLabel);
	
	Map<String,List<String>> getEdmAcronym();
	
	void setEdmAcronym(Map<String,List<String>> edmAcronym);
	
	String getEdmOrganizationScope();
	
	void setEdmOrganizationScope(String edmOrganizationScope);
	
	String getEdmOrganizationDomain();
	
	void setEdmOrganizationDomain(String edmOrganizationDomain);
	
	String getEdmOrganizationSector();
	
	void setEdmOrganizationSector(String edmOrganizationSector);
	
	String getEdmGeographicLevel();
	
	void setEdmGeorgraphicLevel(String edmGeographicLevel);
	
	String getEdmCountry();
	
	void setEdmCountry(String edmCountry);
	
	void setFoafMbox(List<String> foafMbox);

	List<String> getFoafMbox();

	void setFoafPhone(List<String> foafPhone);

	List<String> getFoafPhone();

	void setDcDescription(Map<String, String> dcDescription);

	Map<String, String> getDcDescription();

	void setRdfType(String rdfType);

	String getRdfType();

	void setDcIdentifier(Map<String,List<String>> dcIdentifier);

	Map<String, List<String>> getDcIdentifier();

	void setFoafLogo(String foafLogo);

	String getFoafLogo();

	void setFoafHomepage(String foafHomePage);

	String getFoafHomepage();

	void setEdmEuropeanaRole(List<String> edmEuropeanaRole);

	void setCreated(Date created);

	Date getCreated();

	void setModified(Date modified);

	Date getModified();

	List<String> getEdmEuropeanaRole();

	void setAddress(Address address);

	Address getAddress();
	
	
}