package eu.europeana.metis.utils;

import static eu.europeana.metis.utils.RdfNamespaceContext.EDM_NAMESPACE_PREFIX;
import static eu.europeana.metis.utils.RdfNamespaceContext.ORE_NAMESPACE_PREFIX;
import static eu.europeana.metis.utils.RdfNamespaceContext.RDF_NAMESPACE_PREFIX;
import static javax.xml.XMLConstants.DEFAULT_NS_PREFIX;
import static javax.xml.XMLConstants.NULL_NS_URI;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE;
import static javax.xml.XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
import static javax.xml.XMLConstants.XML_NS_PREFIX;
import static javax.xml.XMLConstants.XML_NS_URI;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Iterator;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Unit test for {@link RdfNamespaceContext}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class RdfNamespaceContextTest {

  private RdfNamespaceContext rdfNamespaceContext;

  private static Stream<Arguments> provideTestData() {
    return Stream.of(
        Arguments.of(DEFAULT_NS_PREFIX, NULL_NS_URI),
        Arguments.of(XML_NS_PREFIX, XML_NS_URI),
        Arguments.of(XMLNS_ATTRIBUTE, XMLNS_ATTRIBUTE_NS_URI),
        Arguments.of(RDF_NAMESPACE_PREFIX, "http://www.w3.org/1999/02/22-rdf-syntax-ns#"),
        Arguments.of(ORE_NAMESPACE_PREFIX, "http://www.openarchives.org/ore/terms/"),
        Arguments.of(EDM_NAMESPACE_PREFIX, "http://www.europeana.eu/schemas/edm/"));
  }

  @ParameterizedTest
  @MethodSource("provideTestData")
  void getNamespaceURI(String namespaceURI, String expectedContext) {
    rdfNamespaceContext = new RdfNamespaceContext();

    final String actualContext = rdfNamespaceContext.getNamespaceURI(namespaceURI);

    assertEquals(expectedContext, actualContext);
  }

  @Test
  void getNamespaceURINull() {
    rdfNamespaceContext = new RdfNamespaceContext();

    assertThrows(IllegalArgumentException.class, () -> rdfNamespaceContext.getNamespaceURI(null));
  }

  @ParameterizedTest
  @MethodSource("provideTestData")
  void getPrefix(String expectedPrefix, String namespaceURI) {
    rdfNamespaceContext = new RdfNamespaceContext();

    final String actualPrefix = rdfNamespaceContext.getPrefix(namespaceURI);

    assertEquals(expectedPrefix, actualPrefix);
  }

  @ParameterizedTest
  @MethodSource("provideTestData")
  void getPrefixes(String expectedPrefix, String namespaceURI) {
    rdfNamespaceContext = new RdfNamespaceContext();

    final Iterator<String> iterator = rdfNamespaceContext.getPrefixes(namespaceURI);

    assertEquals(expectedPrefix, iterator.next());
  }
}