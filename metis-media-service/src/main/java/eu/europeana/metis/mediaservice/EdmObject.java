package eu.europeana.metis.mediaservice;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import eu.europeana.metis.mediaservice.WebResource.NS;

public class EdmObject implements Serializable {
	
	private final Document document;
	private boolean namespacesAdded = false;
	
	private EdmObject(Document document) {
		if (!(document instanceof Serializable))
			throw new IllegalArgumentException("XML document must be serializable");
		this.document = document;
	}
	
	public Map<String, List<UrlType>> getResourceUrls(Collection<UrlType> urlTypes) {
		Map<String, List<UrlType>> urls = new HashMap<>();
		for (UrlType urlType : urlTypes) {
			NodeList list = document.getElementsByTagName(urlType.tagName);
			for (int i = 0; i < list.getLength(); i++) {
				String url = ((Element) list.item(i)).getAttribute("rdf:resource");
				urls.computeIfAbsent(url, k -> new ArrayList<>()).add(urlType);
			}
		}
		return urls;
	}
	
	WebResource getWebResource(String url) {
		Element resourceElement = null;
		NodeList nList = document.getElementsByTagName("edm:WebResource");
		for (int i = 0; i < nList.getLength(); i++) {
			Element node = (Element) nList.item(i);
			if (node.getAttributes().getNamedItem("rdf:about").getNodeValue().equals(url)) {
				resourceElement = node;
				break;
			}
		}
		if (resourceElement == null) {
			resourceElement = document.createElement("edm:WebResource");
			resourceElement.setAttribute("rdf:about", url);
			document.getDocumentElement().appendChild(resourceElement);
		}
		
		if (!namespacesAdded) {
			for (NS ns : NS.values())
				document.getDocumentElement().setAttribute("xmlns:" + ns.prefix(), ns.uri);
			namespacesAdded = true;
		}
		return new WebResource(resourceElement);
	}
	
	/**
	 * Creates {@code EdmObject}s from xml.
	 * <p>
	 * It's recommended to keep an instance for reuse.
	 * <p>
	 * It's not thread safe.
	 */
	public static class Parser {
		private final DocumentBuilder documentBuilder;
		
		public Parser() {
			try {
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				dbFactory.setNamespaceAware(true);
				documentBuilder = dbFactory.newDocumentBuilder();
			} catch (ParserConfigurationException e) {
				throw new RuntimeException("xml doc builder problem", e);
			}
		}
		
		public EdmObject parseXml(InputStream inputStream) throws MediaException {
			try {
				return new EdmObject(documentBuilder.parse(inputStream));
			} catch (SAXException | IOException e) {
				throw new MediaException("Edm parsing error", "EDM PARSE", e);
			}
		}
	}
	
	/**
	 * Creates xml from {@code EdmObject}s.
	 * <p>
	 * It's recommended to keep an instance for reuse.
	 * <p>
	 * It's not thread safe.
	 */
	public static class Writer {
		private final Transformer xmlTtransformer;
		
		public Writer() {
			try {
				xmlTtransformer = TransformerFactory.newInstance().newTransformer();
			} catch (TransformerConfigurationException | TransformerFactoryConfigurationError e) {
				throw new AssertionError("xml transformer problem", e);
			}
		}
		
		public byte[] toXmlBytes(EdmObject edm) {
			try (ByteArrayOutputStream byteStream = new ByteArrayOutputStream()) {
				xmlTtransformer.transform(new DOMSource(edm.document), new StreamResult(byteStream));
				return byteStream.toByteArray();
			} catch (IOException | TransformerException e) {
				throw new RuntimeException("Result EDM XML generation error", e);
			}
		}
	}
}
