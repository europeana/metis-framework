package eu.europeana.enrichment.api.external.model;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlValue;


@XmlAccessorType(XmlAccessType.FIELD)
public class LabelResource {

  @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
  private String lang;
  @XmlValue
  private String value;
  @XmlAttribute(name = "resource", namespace = "http://www.w3.org/1999/02/22-rdf-syntax-ns#")
  private String resource;

  public LabelResource(String lang, String value) {
    this.lang = lang;
    this.value = value;
  }

  public LabelResource(String resource) {
    this.resource = resource;
  }

  public LabelResource() {
  }

  public String getResource() {
    return resource;
  }

  public void setResource(String resource) {
    this.resource = resource;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }
}
