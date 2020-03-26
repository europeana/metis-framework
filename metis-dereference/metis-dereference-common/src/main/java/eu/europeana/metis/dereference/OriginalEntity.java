package eu.europeana.metis.dereference;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * An original Entity as downloaded from an online RDF repository Created by ymamakis on 2/11/16.
 */
@XmlRootElement
@Entity("OriginalEntity")
public class OriginalEntity {

  @Id
  private String id;
  /**
   * The URI it was downloaded from
   */
  @Indexed(options = @IndexOptions(unique = true))
  private String URI;

  /**
   * The RDF/XML that describes the entity
   */
  private String xml;

  @XmlElement
  public String getURI() {
    return URI;
  }

  public void setURI(String URI) {
    this.URI = URI;
  }

  @XmlElement
  public String getXml() {
    return xml;
  }

  public void setXml(String xml) {
    this.xml = xml;
  }

  @XmlElement
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
