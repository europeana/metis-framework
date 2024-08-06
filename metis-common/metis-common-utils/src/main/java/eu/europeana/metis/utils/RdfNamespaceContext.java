package eu.europeana.metis.utils;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Optional;
import java.util.Map.Entry;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;

/**
 * This class provides a {@link NamespaceContext} implementation for RDF documents that can be used
 * when parsing or compiling (serializing or deserializing) XML documents.
 */
public class RdfNamespaceContext implements NamespaceContext {

  public static final String RDF_NAMESPACE_PREFIX = "rdf";
  public static final String EDM_NAMESPACE_PREFIX = "edm";
  public static final String ORE_NAMESPACE_PREFIX = "ore";
  public static final String SVCS_NAMESPACE_PREFIX = "svcs";
  public static final String DCTERMS_NAMESPACE_PREFIX = "dcterms";
  
  private static final Map<String, String> PREFIX_TO_NAMESPACE_MAP = new HashMap<>();

  static {
    PREFIX_TO_NAMESPACE_MAP.put(XMLConstants.DEFAULT_NS_PREFIX, XMLConstants.NULL_NS_URI);
    PREFIX_TO_NAMESPACE_MAP.put(XMLConstants.XML_NS_PREFIX, XMLConstants.XML_NS_URI);
    PREFIX_TO_NAMESPACE_MAP
            .put(XMLConstants.XMLNS_ATTRIBUTE, XMLConstants.XMLNS_ATTRIBUTE_NS_URI);
    PREFIX_TO_NAMESPACE_MAP.put(RDF_NAMESPACE_PREFIX, "http://www.w3.org/1999/02/22-rdf-syntax-ns#");
    PREFIX_TO_NAMESPACE_MAP.put(ORE_NAMESPACE_PREFIX, "http://www.openarchives.org/ore/terms/");
    PREFIX_TO_NAMESPACE_MAP.put(EDM_NAMESPACE_PREFIX, "http://www.europeana.eu/schemas/edm/");
    PREFIX_TO_NAMESPACE_MAP.put(SVCS_NAMESPACE_PREFIX,"http://rdfs.org/sioc/services#");
    PREFIX_TO_NAMESPACE_MAP.put(DCTERMS_NAMESPACE_PREFIX, "http://purl.org/dc/terms/");
  }

  @Override
  public String getNamespaceURI(String s) {
    if (s == null) {
      throw new IllegalArgumentException();
    }
    return Optional.ofNullable(PREFIX_TO_NAMESPACE_MAP.get(s)).orElse(XMLConstants.NULL_NS_URI);
  }

  @Override
  public String getPrefix(String s) {
    if (s == null) {
      throw new IllegalArgumentException();
    }
    return PREFIX_TO_NAMESPACE_MAP.entrySet().stream().filter(entry -> entry.getValue().equals(s))
            .map(Entry::getKey).findAny().orElse(null);
  }

  @Override
  public Iterator<String> getPrefixes(String s) {
    return Optional.ofNullable(getPrefix(s)).map(Collections::singletonList)
            .orElseGet(Collections::emptyList).iterator();
  }
}
