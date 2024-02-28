package eu.europeana.enrichment.api.external.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

/**
 * Resource model class. Basically encapsulating a resource field.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Resource implements WebResource {

  @XmlAttribute(name = "resource", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
  private String resource;

  /**
   * Constructor with initial field value.
   *
   * @param resource the initial resource value
   */
  public Resource(String resource) {
    this.resource = resource;
  }

  public Resource() {
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  @Override
  public String getResourceUri() {
    return getResource();
  }

  @Override
  public void setResourceUri(String resource) {
    setResource(resource);
  }
}
