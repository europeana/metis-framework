package eu.europeana.corelib.solr.entity;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonInclude;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.codehaus.jackson.map.annotate.JsonSerialize.Inclusion;

import org.mongodb.morphia.annotations.Entity;

import eu.europeana.corelib.definitions.edm.entity.Organization;

import static com.fasterxml.jackson.annotation.JsonInclude.Include.NON_EMPTY;

@JsonSerialize(include = Inclusion.NON_EMPTY)
@JsonInclude(NON_EMPTY)
@Entity("Organization")
public class OrganizationImpl extends ContextualClassImpl implements Organization {

	private String rdfType;
	private Map<String,List<String>> dcIdentifier;
	private Map<String,String> dcDescription;
	private Map<String,List<String>> edmAcronym;
	
	private String foafLogo;
	private String foafHomepage;	
	private List<String> foafPhone;
	private List<String> foafMbox;
	
	private List<String> edmEuropeanaRole;
	private String edmOrganizationDomain;
	private String edmOrganizationSector;
	private String edmOrganizationScope;
	private String edmGeographicLevel;
	private String edmCountry;
	
	private Address address;
	
	private Date created;
	private Date modified;

	
	@Override
	public Map<String, List<String>> getEdmAcronym() {
		return this.edmAcronym;
	}

	@Override
	public void setEdmAcronym(Map<String, List<String>> edmAcronym) {
		this.edmAcronym = edmAcronym;
	}

	@Override
	public String getEdmOrganizationScope() {
		return this.edmOrganizationScope;
	}

	@Override
	public void setEdmOrganizationScope(String edmOrganizationScope) {
		this.edmOrganizationScope = edmOrganizationScope;
	}

	@Override
	public String getEdmOrganizationDomain() {
		return this.edmOrganizationDomain;
	}

	@Override
	public void setEdmOrganizationDomain(String edmOrganizationDomain) {
		this.edmOrganizationDomain = edmOrganizationDomain;
	}

	@Override
	public String getEdmOrganizationSector() {
		return this.edmOrganizationSector;
	}

	@Override
	public void setEdmOrganizationSector(String edmOrganizationSector) {
		this.edmOrganizationSector= edmOrganizationSector;
	}

	@Override
	public String getEdmGeographicLevel() {
		return this.edmGeographicLevel;
	}

	@Override
	public void setEdmGeorgraphicLevel(String edmGeographicLevel) {
		this.edmGeographicLevel = edmGeographicLevel;
	}

	@Override
	public String getEdmCountry() {
		return this.edmCountry;
	}

	@Override
	public void setEdmCountry(String edmCountry) {
		this.edmCountry = edmCountry;
	}

	@Override
	public List<String> getEdmEuropeanaRole() {
		return this.edmEuropeanaRole;
	}

	@Override
	public void setEdmEuropeanaRole(List<String> edmEuropeanaRole) {
		this.edmEuropeanaRole = edmEuropeanaRole;
	}

	@Override
	public String getFoafHomepage() {
		return this.foafHomepage;
	}

	@Override
	public void setFoafHomepage(String foafHomePage) {
		this.foafHomepage = foafHomePage;
	}

	@Override
	public String getFoafLogo() {
		return this.foafLogo;
	}

	@Override
	public void setFoafLogo(String foafLogo) {
		this.foafLogo = foafLogo;
	}

	@Override
	public Map<String, List<String>> getDcIdentifier() {
		return this.dcIdentifier;
	}

	@Override
	public void setDcIdentifier(Map<String,List<String>> dcIdentifier) {
		
		this.dcIdentifier= dcIdentifier;
	}

	@Override
	public String getRdfType() {
		return rdfType;
	}

	@Override
	public void setRdfType(String rdfType) {
		this.rdfType = rdfType;
	}

	@Override
	public Map<String, String> getDcDescription() {
		return dcDescription;
	}

	@Override
	public void setDcDescription(Map<String, String> dcDescription) {
		this.dcDescription = dcDescription;
	}

	@Override
	public List<String> getFoafPhone() {
		return foafPhone;
	}

	@Override
	public void setFoafPhone(List<String> foafPhone) {
		this.foafPhone = foafPhone;
	}

	@Override
	public List<String> getFoafMbox() {
		return foafMbox;
	}

	@Override
	public void setFoafMbox(List<String> foafMbox) {
		this.foafMbox = foafMbox;
	}

	@Override
	public Date getModified() {
		return modified;
	}

	@Override
	public void setModified(Date modified) {
		this.modified = modified;
	}

	@Override
	public Date getCreated() {
		return created;
	}

	@Override
	public void setCreated(Date created) {
		this.created = created;
	}

	@Override
	public Address getAddress() {
		return address;
	}

	@Override
	public void setAddress(Address address) {
		this.address = address;
	}

	

}
