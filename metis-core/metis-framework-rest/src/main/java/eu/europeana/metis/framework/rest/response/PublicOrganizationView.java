package eu.europeana.metis.framework.rest.response;

import eu.europeana.metis.framework.common.*;

import java.util.List;

/**
 * Created by ymamakis on 11/11/16.
 */
public class PublicOrganizationView {

    private String organisationName;
    private String organisationZohoId;
    private String organisationURI;
    private String acronym;
    private List<PrefLabel> prefLabel;
    private List<AltLabel> altLabel;
    private String[] sameAs;
    private String description;
    private String logo;
    private Domain domain;
    private Sector sector;
    private GeographicLevel geographicLevel;
    private String website;
    private Country europeanaCountry;
    private Language language;
    private List<Role> role;
    private Scope scope;

    public String getOrganisationName() {
        return organisationName;
    }

    public void setOrganisationName(String organisationName) {
        this.organisationName = organisationName;
    }

    public String getOrganisationZohoId() {
        return organisationZohoId;
    }

    public void setOrganisationZohoId(String organisationZohoId) {
        this.organisationZohoId = organisationZohoId;
    }

    public String getOrganisationURI() {
        return organisationURI;
    }

    public void setOrganisationURI(String organisationURI) {
        this.organisationURI = organisationURI;
    }

    public String getAcronym() {
        return acronym;
    }

    public void setAcronym(String acronym) {
        this.acronym = acronym;
    }

    public List<PrefLabel> getPrefLabel() {
        return prefLabel;
    }

    public void setPrefLabel(List<PrefLabel> prefLabel) {
        this.prefLabel = prefLabel;
    }

    public List<AltLabel> getAltLabel() {
        return altLabel;
    }

    public void setAltLabel(List<AltLabel> altLabel) {
        this.altLabel = altLabel;
    }

    public String[] getSameAs() {
        return sameAs;
    }

    public void setSameAs(String[] sameAs) {
        this.sameAs = sameAs;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getLogo() {
        return logo;
    }

    public void setLogo(String logo) {
        this.logo = logo;
    }

    public Domain getDomain() {
        return domain;
    }

    public void setDomain(Domain domain) {
        this.domain = domain;
    }

    public Sector getSector() {
        return sector;
    }

    public void setSector(Sector sector) {
        this.sector = sector;
    }

    public GeographicLevel getGeographicLevel() {
        return geographicLevel;
    }

    public void setGeographicLevel(GeographicLevel geographicLevel) {
        this.geographicLevel = geographicLevel;
    }

    public String getWebsite() {
        return website;
    }

    public void setWebsite(String website) {
        this.website = website;
    }

    public Country getEuropeanaCountry() {
        return europeanaCountry;
    }

    public void setEuropeanaCountry(Country europeanaCountry) {
        this.europeanaCountry = europeanaCountry;
    }

    public Language getLanguage() {
        return language;
    }

    public void setLanguage(Language language) {
        this.language = language;
    }

    public List<Role> getRole() {
        return role;
    }

    public void setRole(List<Role> role) {
        this.role = role;
    }

    public Scope getScope() {
        return scope;
    }

    public void setScope(Scope scope) {
        this.scope = scope;
    }


}
