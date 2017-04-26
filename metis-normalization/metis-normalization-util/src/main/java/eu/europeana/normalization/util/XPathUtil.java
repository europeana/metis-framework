package eu.europeana.normalization.util;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.namespace.NamespaceContext;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class XPathUtil {

	private static XPathFactory factory = XPathFactory.newInstance();
	
	static class SimpleNamespaceContext implements NamespaceContext {
	    private final Map<String, String> PREF_MAP = new HashMap<String, String>();

	    public SimpleNamespaceContext(final Map<String, String> prefMap) {
	        PREF_MAP.putAll(prefMap);       
	    }

	    public String getNamespaceURI(String prefix) {
	        return PREF_MAP.get(prefix);
	    }

	    public String getPrefix(String uri) {
	        throw new UnsupportedOperationException();
	    }

	    public Iterator<String> getPrefixes(String uri) {
	        throw new UnsupportedOperationException();
	    }
	}
	
	
    public static XPathExpression newXPath(Map<String, String> prefixMap, String xpathExpression) throws XPathExpressionException {
        XPath xpath = factory.newXPath();
        SimpleNamespaceContext namespaces = new SimpleNamespaceContext(prefixMap);
    	xpath.setNamespaceContext(namespaces);
    	XPathExpression expr = xpath
    	        .compile(xpathExpression);
		return expr;
    }
	
    
    public static NodeList queryDom(Map<String, String> prefixMap, String xpathExpression, Document dom) throws XPathExpressionException {
    	XPathExpression expr = newXPath(prefixMap, xpathExpression);
    	
    	NodeList result = (NodeList) expr.evaluate(dom, XPathConstants.NODESET);
    	
    	return result;
    }


	public static Element queryDomForElement(Map<String, String> namespacesPrefixes, String expression, Document dom) throws XPathExpressionException {
		NodeList resultSet = queryDom(namespacesPrefixes, expression, dom);
		if(resultSet.getLength()==0)
			return null;
		return (Element) resultSet.item(1);
		
	}

}
