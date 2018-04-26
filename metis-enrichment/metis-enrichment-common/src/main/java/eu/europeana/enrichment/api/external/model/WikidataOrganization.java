package eu.europeana.enrichment.api.external.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * This class stores result of the parsing of XSLT/XML 
 * organization file with Wikidata content. It starts with <rdf:RDF> tag
 * and comprises EDM organization object.
 * 
 * @author GrafR
 *
 */
@XmlRootElement(namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#", name = "RDF")
@XmlAccessorType(XmlAccessType.FIELD)
public class WikidataOrganization {

  @XmlElement(name = "Organization", namespace = "http://xmlns.com/foaf/0.1/")
  private EdmOrganization organization;

  public EdmOrganization getOrganization() {
    return organization;
  }

  public void setOrganization(EdmOrganization organization) {
    this.organization = organization;
  }
 
}
