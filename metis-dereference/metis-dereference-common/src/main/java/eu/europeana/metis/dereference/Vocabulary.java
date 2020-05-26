package eu.europeana.metis.dereference;

import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import javax.xml.bind.annotation.XmlElement;

/**
 * A controlled vocabulary representation Created by ymamakis on 2/11/16.
 */

@Entity("Vocabulary")
public class Vocabulary implements Serializable {

  /**
   * Required for implementations of {@link Serializable}.
   **/
  private static final long serialVersionUID = 2946293185967000824L;

  @Id
  private String id;

  /**
   * The URI of the controlled vocabulary.
   *
   * @deprecated Is present because it may be searched by old implementations. Should always be
   * equal to the server (without path) so that it is always found. This field will be removed.
   */
  @Indexed
  @Deprecated
  private String uri;

  /**
   * The URIs of the controlled vocabulary
   */
  @Indexed
  private Set<String> uris;

  /**
   * The suffix of the vocabulary: needs to be added after the variable bit of the URI.
   */
  private String suffix;

  /**
   * Rules that take into account the rdf:type attribute of an rdf:Description to specify whether
   * @deprecated Is no longer used. Should always be null. This field will be removed.
   */
  @Deprecated
  private Set<String> typeRules;

  /**
   * Rules by URL
   *
   * @deprecated Should be equal to the URI paths. This means that together (concatenated) with the
   * deprecated field {@link #uri} it will contain all possible URIs, as before. This field will be
   * removed.
   */
  @Deprecated
  private Set<String> rules;

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
  @Indexed(options = @IndexOptions(unique = true))
  private String name;

  @XmlElement
  public Set<String> getUris() {
    return Collections.unmodifiableSet(this.uris);
  }

  public void setUris(Collection<String> uris) {
    this.uris = new HashSet<>(uris);

    // Also take care of backwards compatibility
    final String sampleUrl = uris.iterator().next();
    final String server;
    try {
      final URL convertedUrl = new URL(sampleUrl);
      server = convertedUrl.getProtocol() + "://" + convertedUrl.getAuthority() + "/";
    } catch (MalformedURLException e) {
      throw new IllegalStateException("Shouldn't happen: problem with URL " + sampleUrl);
    }
    this.uri = server;
    this.typeRules = null;
    this.rules = uris.stream().map(uri -> uri.substring(server.length())).collect(Collectors.toSet());
  }

  @XmlElement
  public String getSuffix() {
    return suffix;
  }

  public void setSuffix(String suffix) {
    this.suffix = suffix;
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
  public String getId() {
    return id;
  }

  public void setId(String id) {
    this.id = id;
  }
}
