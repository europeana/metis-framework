package eu.europeana.metis.dereference;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Field;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Index;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexes;
import eu.europeana.metis.mongo.utils.ObjectIdSerializer;
import java.io.Serial;
import java.io.Serializable;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import jakarta.xml.bind.annotation.XmlElement;
import org.bson.types.ObjectId;

/**
 * A controlled vocabulary representation Created by ymamakis on 2/11/16.
 */

@Entity
@Indexes({
    @Index(fields = {@Field("uris")}),
    @Index(fields = {@Field("name")}, options = @IndexOptions(unique = true))
})
public class Vocabulary implements Serializable {

  /**
   * Required for implementations of {@link Serializable}.
   **/
  @Serial
  private static final long serialVersionUID = 2946293185967000824L;

  @Id
  @JsonSerialize(using = ObjectIdSerializer.class)
  private ObjectId id;

  /**
   * The URIs of the controlled vocabulary
   */
  private Set<String> uris;

  /**
   * The suffix of the vocabulary: needs to be added after the variable bit of the URI.
   * Note: if a value for <code>resourceUrlTemplate</code> is set, this suffix is ignored.
   */
  private String suffix;

  /**
   * The Resource URL template of the vocabulary: defines how to resolve resource IDs.
   */
  private String resourceUrlTemplate;

  /**
   * The value of the User Agent HTTP header to be used with the vocabulary. If null, the default
   * user agent will be set.
   */
  private String userAgent;

  /**
   * The XSLT to convert an external entity to an internal entity
   */
  private String xslt;

  /**
   * The iterations (broader) that we need to retrieve
   */
  private int iterations;

  /**
   * The name of the vocabulary
   */
  private String name;

  @XmlElement
  public Set<String> getUris() {
    return Collections.unmodifiableSet(this.uris);
  }

  public void setUris(Collection<String> uris) {
    this.uris = new HashSet<>(uris);
  }

  @XmlElement
  @Deprecated
  public String getSuffix() {
    return suffix;
  }

  @Deprecated
  public void setSuffix(String suffix) {
    this.suffix = suffix;
  }

  @XmlElement
  public String getResourceUrlTemplate() {
    return resourceUrlTemplate;
  }

  public void setResourceUrlTemplate(String resourceUrlTemplate) {
    this.resourceUrlTemplate = resourceUrlTemplate;
  }

  @XmlElement
  public String getUserAgent() {
    return userAgent;
  }

  public void setUserAgent(String userAgent) {
    this.userAgent = userAgent;
  }

  @XmlElement
  public String getXslt() {
    return xslt;
  }

  public void setXslt(String xslt) {
    this.xslt = xslt;
  }

  @XmlElement
  public int getIterations() {
    return iterations;
  }

  public void setIterations(int iterations) {
    this.iterations = iterations;
  }

  @XmlElement
  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  @XmlElement
  public ObjectId getId() {
    return id;
  }

  public void setId(ObjectId id) {
    this.id = id;
  }
}
