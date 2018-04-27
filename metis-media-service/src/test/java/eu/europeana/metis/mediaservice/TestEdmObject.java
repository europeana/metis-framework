package eu.europeana.metis.mediaservice;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.not;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.junit.BeforeClass;
import org.junit.Test;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class TestEdmObject {
	
	EdmObject.Parser parser = new EdmObject.Parser();
	EdmObject.Writer writer = new EdmObject.Writer();
	
	public static DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
	public static DocumentBuilder dBuilder;
	
	public static final String EXAMPLE_URL = "http://example.com";
	
	@BeforeClass
	public static void setUp() throws ParserConfigurationException {
		dBuilder = dbFactory.newDocumentBuilder();
	}
	
	@Test
	public void shouldPreviewdBeSetIfNotExistsInEuropeanaAggregationList()
			throws TransformerConfigurationException, ParserConfigurationException, SAXException, IOException,
			TransformerException, TransformerFactoryConfigurationError, MediaException {
		EdmObject edm = getEdmObjectWithoutPreview();
		edm.updateEdmPreview(EXAMPLE_URL);
		assertEquals(EXAMPLE_URL, getPreviewContent(edm));
	}
	
	@Test
	public void shouldNewPreviewBeSet() throws MediaException, SAXException, IOException, ParserConfigurationException {
		EdmObject edm = getEdmObject("image1-input.xml");
		String oldPreview = getPreviewContent(edm);
		edm.updateEdmPreview(EXAMPLE_URL);
		assertEquals(EXAMPLE_URL, getPreviewContent(edm));
		assertThat(EXAMPLE_URL, not(equalTo(oldPreview)));
	}
	
	private EdmObject getEdmObjectWithoutPreview()
			throws SAXException, IOException, TransformerConfigurationException,
			TransformerException, TransformerFactoryConfigurationError, MediaException {
		EdmObject edm = getEdmObject("image1-input.xml");
		ByteArrayInputStream bis = new ByteArrayInputStream(writer.toXmlBytes(edm));
		
		Document doc = dBuilder.parse(bis);
		
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
		TransformerFactory.newInstance().newTransformer().transform(new DOMSource(doc), new StreamResult(outputStream));
		return parser.parseXml(new ByteArrayInputStream(outputStream.toByteArray()));
	}
	
	private EdmObject getEdmObject(String resource) throws MediaException {
		return parser.parseXml(getClass().getClassLoader().getResourceAsStream(resource));
	}
	
	private String getPreviewContent(EdmObject edm) throws ParserConfigurationException, SAXException, IOException {
		byte[] source = writer.toXmlBytes(edm);
		
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
