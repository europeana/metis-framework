package eu.europeana.normalization.util;

/**
 * Instances of this class represent an XML namespace (with tag prefix and URI).
 */
public enum Namespace {

  XML("http://www.w3.org/XML/1998/namespace", "xml"),

  RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#", "rdf"),

  EDM("http://www.europeana.eu/schemas/edm/", "edm"),

  ORE("http://www.openarchives.org/ore/terms/", "ore"),

  SKOS("http://www.w3.org/2004/02/skos/core#", "skos"),

  DC("http://purl.org/dc/elements/1.1/", "dc"),

  DCTERMS("http://purl.org/dc/terms/", "dcterms");

  private final String uri;
  private final String prefix;

  Namespace(String uri, String prefix) {
    this.uri = uri;
    this.prefix = prefix;
  }

  /**
   * The URI of this namespace.
   *
   * @return the uri
   */
  public String getUri() {
    return uri;
  }


  /**
   * Get the prefix for this namespace
   *
   * @return the prefix
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * This method creates an instance of {@link Element} for this namespace and the given element name.
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
   */
  public static final class Element {

    private final String elementName;
    private final Namespace namespace;

    private Element(String elementName, Namespace namespace) {
      this.elementName = elementName;
      this.namespace = namespace;
    }

    /**
     * @return The namespace of the element.
     */
    public Namespace getNamespace() {
      return namespace;
    }

    /**
     * Get the element name.
     *
     * @return the element name
     */
    public String getElementName() {
      return elementName;
    }

    /**
     * Get an element name that contains the namespace prefix e.g. prefix:elementName.
     *
     * @return the prefixed element name
     */
    public String getPrefixedElementName() {
      return String.format("%s:%s", namespace.getPrefix(), elementName);
    }
  }
}
