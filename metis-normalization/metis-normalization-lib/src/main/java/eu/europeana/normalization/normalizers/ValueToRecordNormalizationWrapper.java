package eu.europeana.normalization.normalizers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import eu.europeana.normalization.NormalizeDetails;
import eu.europeana.normalization.RecordNormalization;
import eu.europeana.normalization.ValueNormalization;
import eu.europeana.normalization.model.ConfidenceLevel;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.util.Namespaces;
import eu.europeana.normalization.util.XPathUtil;
import eu.europeana.normalization.util.XmlUtil;

public class ValueToRecordNormalizationWrapper implements RecordNormalization {

	public static class XpathQuery {
		Map<String, String> namespacesPrefixes;
		String expression;
		
		public XpathQuery(String expression) {
			super();
			this.expression = expression; 
		}

		public XpathQuery(Map<String, String> namespacesPrefixes, String expression) {
			super();
			this.namespacesPrefixes = namespacesPrefixes;
			this.expression = expression;
		}
		
		public Map<String, String> getNamespacesPrefixes() {
			return namespacesPrefixes;
		}
		public void setNamespacesPrefixes(Map<String, String> namespacesPrefixes) {
			this.namespacesPrefixes = namespacesPrefixes;
		}
		public String getExpression() {
			return expression;
		}
		public void setExpression(String expression) {
			this.expression = expression;
		}
		public void setPrefix(String prefix, String namespace) {
			if(namespacesPrefixes==null)
				namespacesPrefixes=new HashMap<>();
			namespacesPrefixes.put(prefix, namespace);
		}
	}

	protected static final XpathQuery EUROPEANA_PROXY_QUERY=new XpathQuery(
			new HashMap<String, String>() {{
				put("rdf",Namespaces.RDF);
				put("ore", Namespaces.ORE);
				put("edm", Namespaces.EDM);
			}}, "/rdf:RDF/ore:Proxy[edm:europeanaProxy='true']");
	
	protected static final XpathQuery PROVIDER_PROXY_QUERY=new XpathQuery(
			new HashMap<String, String>() {{
				put("rdf",Namespaces.RDF);
				put("ore", Namespaces.ORE);
				put("edm", Namespaces.EDM);
			}}, "/rdf:RDF/ore:Proxy[not(edm:europeanaProxy='true')]");
	
	List<XpathQuery> targetElements;
	ValueNormalization normalization;
	boolean normalizeToEuropeanaProxy;
	
	public ValueToRecordNormalizationWrapper(ValueNormalization normalization, boolean normalizeToEuropeanaProxy, XpathQuery... targetElements) {
		this.normalization = normalization;
		this.targetElements=new ArrayList<>();
		for(XpathQuery q: targetElements) {
			this.targetElements.add(q);
		}
	}
	
	@Override
	public NormalizationReport normalize(Document edm) {
		Element europeanaProxy;
		Element providerProxy;
		try {
			europeanaProxy = XPathUtil.queryDomForElement(EUROPEANA_PROXY_QUERY.getNamespacesPrefixes(), EUROPEANA_PROXY_QUERY.getExpression(), edm);
			providerProxy = XPathUtil.queryDomForElement(PROVIDER_PROXY_QUERY.getNamespacesPrefixes(), PROVIDER_PROXY_QUERY.getExpression(), edm);
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e.getMessage(), e);
		}
		NormalizationReport report=new NormalizationReport();
		
		for(XpathQuery q: targetElements) { 
			NodeList elements;
			try {
				elements = XPathUtil.queryDom(q.getNamespacesPrefixes(), q.getExpression(), edm);
			} catch (XPathExpressionException e) { throw new RuntimeException(e.getMessage(), e); }
			for(int i=0; i<elements.getLength(); i++) {
				Node node = elements.item(i);
				if(node instanceof Attr) {
					Attr at=(Attr) node;
					String value = at.getValue();
					List<NormalizeDetails> normalizedValue = normalization.normalizeDetailed(value);

					if(normalizedValue.isEmpty() || normalizedValue.size()>1) {
						at.getParentNode().getAttributes().removeNamedItemNS(at.getNamespaceURI(), at.getLocalName());
						report.increment(normalization.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
					}else {
						boolean valueChanged= !normalizedValue.get(0).getNormalizedValue().equals(value);
						if (valueChanged) {
					        at.setValue(normalizedValue.get(0).getNormalizedValue());
					        report.increment(normalization.getClass().getSimpleName(), normalizedValue.get(0).getConfidenceClass());
						}
					}
				} else {
					Element el=(Element) node;
					String value = XmlUtil.getElementText(el);
					List<NormalizeDetails> normalizedValue = normalization.normalizeDetailed(value);
			       
					if(normalizedValue.isEmpty()) {
						if(el.getAttributes().getLength()==0 || (el.getAttributes().getLength()==1 && !StringUtils.isEmpty(el.getAttributeNS(Namespaces.XML, "lang"))) )
							el.getParentNode().removeChild(el);
						else {
							NodeList childNodes = el.getChildNodes();
					        for (int j = 0; j < childNodes.getLength(); j++) {
					          Node childNode = childNodes.item(j);
					          if (childNode.getNodeType() == Node.TEXT_NODE) 
					        	  el.removeChild(childNode);
					        }
						}
						report.increment(normalization.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
					} else {
	//					System.out.println(value);
	//					System.out.println(normalizedValue.get(0).getNormalizedValue());
						
						boolean valueChanged=normalizedValue.size()>1 || !normalizedValue.get(0).getNormalizedValue().equals(value);
	//					System.out.println(valueChanged);
						if (valueChanged) {
							if(normalizeToEuropeanaProxy && el.getParentNode()==providerProxy) {
								if(europeanaProxy==null) {
									europeanaProxy=edm.createElementNS(Namespaces.ORE, "Proxy");
									edm.getDocumentElement().appendChild(europeanaProxy);
									Element europeanaProxyProp=edm.createElementNS(Namespaces.EDM, "europeanaProxy");
									europeanaProxyProp.appendChild(edm.createTextNode("true"));
									europeanaProxy.appendChild(europeanaProxyProp);
								}
					        	for(int k=0 ; k<normalizedValue.size() ; k++) {
					        		report.increment(normalization.getClass().getSimpleName(), normalizedValue.get(k).getConfidenceClass());
					        		
					        		Node newEl = el.cloneNode(false);
					        		newEl.appendChild(el.getOwnerDocument().createTextNode(normalizedValue.get(k).getNormalizedValue()));
					        		europeanaProxy.appendChild(newEl);
					        	}
							} else {
								NodeList childNodes = el.getChildNodes();
						        for (int j = 0; j < childNodes.getLength(); j++) {
						          Node childNode = childNodes.item(j);
						          if (childNode.getNodeType() == Node.TEXT_NODE) 
						        	  el.removeChild(childNode);
						        }
						        el.appendChild(el.getOwnerDocument().createTextNode(normalizedValue.get(0).getNormalizedValue()));
						        report.increment(normalization.getClass().getSimpleName(), normalizedValue.get(0).getConfidenceClass());
						        
						        if(normalizedValue.size()>1) {
						        	for(int k=1 ; k<normalizedValue.size() ; k++) {
						        		Node newEl = el.cloneNode(false);
						        		report.increment(normalization.getClass().getSimpleName(), normalizedValue.get(k).getConfidenceClass());
						        		newEl.appendChild(el.getOwnerDocument().createTextNode(normalizedValue.get(k).getNormalizedValue()));
						        		el.getParentNode().insertBefore(newEl, el.getNextSibling());
						        	}
						        }
							}
						}
					}
				}
			}
		}
		return report;
	}

}
