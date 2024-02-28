package eu.europeana.enrichment.api.external.model;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlValue;

/**
 * Label model class
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Label {

  @XmlAttribute(name = "lang", namespace = "http://www.w3.org/XML/1998/namespace")
  private String lang;

  @XmlValue
  private String value;

  public Label() {
  }

  /**
   * Constructor with initial label value without a language. Language is null.
   *
   * @param value1 the value of the label
   */
  public Label(String value1) {
    this(null, value1);
  }

  /**
   * Constructor with initial field values.
   *
   * @param lang1 the language of the corresponding label
   * @param value1 the value of the label
   */
  public Label(String lang1, String value1) {
    lang = lang1;
    value = value1;
  }

  public String getLang() {
    return lang;
  }

  public void setLang(String lang) {
    this.lang = lang;
  }

  public String getValue() {
    return value;
  }

  public void setValue(String value) {
    this.value = value;
  }
}
