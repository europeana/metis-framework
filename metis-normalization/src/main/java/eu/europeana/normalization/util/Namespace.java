package eu.europeana.normalization.util;

import java.util.Formattable;
import java.util.Formatter;

/**
 * Instances of this class represent an XML namespace (with tag prefix and URI).
 */
public enum Namespace {

  XML("xml", "http://www.w3.org/XML/1998/namespace"),

  RDF("rdf", "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),

  EDM("edm", "http://www.europeana.eu/schemas/edm/"),

  ORE("ore", "http://www.openarchives.org/ore/terms/"),

  SKOS("skos", "http://www.w3.org/2004/02/skos/core#"),

  DC("dc", "http://purl.org/dc/elements/1.1/"),

  DCTERMS("dcterms", "http://purl.org/dc/terms/");

  private final String tagPrefix;
  private final String uri;

  Namespace(String tagPrefix, String uri) {
    this.tagPrefix = tagPrefix;
    this.uri = uri;
  }

  /**
   * 
   * @return The tag prefix of this namespace.
   */
  public String getTagPrefix() {
    return tagPrefix;
  }

  /**
   * 
   * @return The URI of this namespace.
   */
  public String getUri() {
    return uri;
  }

  /**
   * This method creates an instance of {@link Element} for this namespace and the given element
   * name.
   * 
   * @param elementName The element name.
   * @return The element.
   */
  public Element getElement(String elementName) {
    return new Element(elementName, this);
  }

  /**
   * This class represents an XML element.
   * 
   * @author jochen
   *
   */
  public static final class Element implements Formattable {

    private final String elementName;
    private final Namespace namespace;

    private Element(String elementName, Namespace namespace) {
      this.elementName = elementName;
      this.namespace = namespace;
    }

    /**
     * 
     * @return The namespace of the element.
     */
    public Namespace getNamespace() {
      return namespace;
    }

    @Override
    public void formatTo(Formatter formatter, int flags, int width, int precision) {
      formatter.format("%s:%s", this.namespace.tagPrefix, this.elementName);
    }

    @Override
    public String toString() {
      final StringBuilder result = new StringBuilder();
      formatTo(new Formatter(result), 0, 0, 0);
      return result.toString();
    }
  }
}
