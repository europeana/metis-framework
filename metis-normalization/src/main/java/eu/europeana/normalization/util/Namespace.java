package eu.europeana.normalization.util;

/**
 * Instances of this class represent an XML namespace (with tag prefix and URI).
 */
public enum Namespace {

  XML("http://www.w3.org/XML/1998/namespace"),

  RDF("http://www.w3.org/1999/02/22-rdf-syntax-ns#"),

  EDM("http://www.europeana.eu/schemas/edm/"),

  ORE("http://www.openarchives.org/ore/terms/"),

  SKOS("http://www.w3.org/2004/02/skos/core#"),

  DC("http://purl.org/dc/elements/1.1/"),

  DCTERMS("http://purl.org/dc/terms/");

  private final String uri;

  Namespace(String uri) {
    this.uri = uri;
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
  public static final class Element {

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

    public String getElementName() {
      return elementName;
    }
  }
}
