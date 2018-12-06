package eu.europeana.metis.mediaprocessing.model;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.metis.mediaprocessing.RdfConverter.Writer;
import eu.europeana.metis.mediaprocessing.RdfConverterFactory;
import eu.europeana.metis.mediaprocessing.RdfDeserializer;
import eu.europeana.metis.mediaprocessing.RdfSerializer;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestEnrichedRdf {

  public static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
  public static DocumentBuilder dBuilder;

  public static final String EXAMPLE_URL = "http://example.com";

  private static RdfDeserializer deserializer;
  private static RdfSerializer serializer;

  @BeforeAll
  public static void setUp() throws ParserConfigurationException, MediaProcessorException {
    deserializer = new RdfConverterFactory().createRdfDeserializer();
    serializer = new RdfConverterFactory().createRdfSerializer();
    dBuilder = dbFactory.newDocumentBuilder();
  }

  @Test
  public void shouldPreviewdBeSetIfNotExistsInEuropeanaAggregationList()
      throws ParserConfigurationException, SAXException, IOException,
      TransformerException, TransformerFactoryConfigurationError, MediaProcessorException {
    EnrichedRdfImpl edm = getEdmObjectWithoutPreview();
    edm.updateEdmPreview(EXAMPLE_URL);
    assertEquals(EXAMPLE_URL, getPreviewContent(edm));
  }

  @Test
  public void shouldNewPreviewBeSet()
      throws MediaProcessorException, SAXException, IOException, ParserConfigurationException {
    EnrichedRdfImpl edm = getEnrichedRdf("image1-input.xml");
    String oldPreview = getPreviewContent(edm);
    edm.updateEdmPreview(EXAMPLE_URL);
    assertEquals(EXAMPLE_URL, getPreviewContent(edm));
    assertThat(EXAMPLE_URL, not(equalTo(oldPreview)));
  }

  private EnrichedRdfImpl getEdmObjectWithoutPreview()
      throws SAXException, IOException, TransformerException, TransformerFactoryConfigurationError, MediaProcessorException {

    Document doc = dBuilder.parse(getClass().getClassLoader().getResourceAsStream("image1-input.xml"));

    Node eAggregation = doc.getElementsByTagName("edm:EuropeanaAggregation").item(0);

    NodeList nodeList = eAggregation.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node.getNodeName().equals("edm:preview")) {
        eAggregation.removeChild(node);
        break;
      }
    }

    ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
    TransformerFactory.newInstance().newTransformer()
        .transform(new DOMSource(doc), new StreamResult(outputStream));
    return (EnrichedRdfImpl) deserializer.getRdfForResourceEnriching(new ByteArrayInputStream(outputStream.toByteArray()));
  }

  private EnrichedRdfImpl getEnrichedRdf(String resource) throws MediaProcessorException {
    return (EnrichedRdfImpl) deserializer
        .getRdfForResourceEnriching(getClass().getClassLoader().getResourceAsStream(resource));
  }

  private String getPreviewContent(EnrichedRdfImpl edm)
      throws ParserConfigurationException, SAXException, IOException, MediaProcessorException {
    byte[] source = ((Writer) serializer).serialize(edm.getRdf());

    DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
    DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();

    ByteArrayInputStream bis = new ByteArrayInputStream(source);
    Document doc = dBuilder.parse(bis);

    NodeList nl = doc.getDocumentElement().getElementsByTagName("edm:EuropeanaAggregation");
    Node item = nl.item(0);

    String textContent = "";
    NodeList nodeList = item.getChildNodes();
    for (int i = 0; i < nodeList.getLength(); i++) {
      Node node = nodeList.item(i);
      if (node.getNodeName().equals("edm:preview")) {
        textContent = node.getAttributes().getNamedItem("rdf:resource").getTextContent();
        break;
      }
    }

    return textContent;
  }

}
