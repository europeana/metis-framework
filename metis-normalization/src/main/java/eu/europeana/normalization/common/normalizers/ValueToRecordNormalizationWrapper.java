package eu.europeana.normalization.common.normalizers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import eu.europeana.normalization.common.NormalizeDetails;
import eu.europeana.normalization.common.RecordNormalization;
import eu.europeana.normalization.common.ValueNormalization;
import eu.europeana.normalization.model.ConfidenceLevel;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.XmlUtil;
import eu.europeana.normalization.util.XpathQuery;

public class ValueToRecordNormalizationWrapper implements RecordNormalization {

  private static final XpathQuery EUROPEANA_PROXY_QUERY =
      XpathQuery.create("/%s/%s[%s='true']", XpathQuery.RDF_TAG, Namespace.ORE.getElement("Proxy"),
          Namespace.EDM.getElement("europeanaProxy"));

  private static final XpathQuery PROVIDER_PROXY_QUERY =
      XpathQuery.create("/%s/%s[not(%s='true')]", Namespace.RDF.getElement("RDF"),
          Namespace.ORE.getElement("Proxy"), Namespace.EDM.getElement("europeanaProxy"));

  private boolean normalizeToEuropeanaProxy;
  private List<XpathQuery> targetElements;
  private ValueNormalization normalization;

  public ValueToRecordNormalizationWrapper(ValueNormalization normalization,
      boolean normalizeToEuropeanaProxy, XpathQuery... targetElements) {
    this.normalization = normalization;
    this.targetElements = new ArrayList<>();
    Collections.addAll(this.targetElements, targetElements);
  }

  @Override
  public NormalizationReport normalize(Document edm) {
    Element europeanaProxy;
    Element providerProxy;
    try {
      europeanaProxy = EUROPEANA_PROXY_QUERY.executeForSingleElement(edm);
      providerProxy = PROVIDER_PROXY_QUERY.executeForSingleElement(edm);
    } catch (XPathExpressionException e) {
      throw new RuntimeException(e.getMessage(), e);
    }
    NormalizationReport report = new NormalizationReport();

    for (XpathQuery q : targetElements) {
      NodeList elements;
      try {
        elements = q.execute(edm);
      } catch (XPathExpressionException e) {
        throw new RuntimeException(e.getMessage(), e);
      }
      for (int i = 0; i < elements.getLength(); i++) {
        Node node = elements.item(i);
        if (node instanceof Attr) {
          Attr at = (Attr) node;
          String value = at.getValue();
          List<NormalizeDetails> normalizedValue = normalization.normalizeDetailed(value);

          if (normalizedValue.isEmpty() || normalizedValue.size() > 1) {
            at.getParentNode().getAttributes().removeNamedItemNS(at.getNamespaceURI(),
                at.getLocalName());
            report.increment(normalization.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
          } else {
            boolean valueChanged = !normalizedValue.get(0).getNormalizedValue().equals(value);
            if (valueChanged) {
              at.setValue(normalizedValue.get(0).getNormalizedValue());
              report.increment(normalization.getClass().getSimpleName(),
                  normalizedValue.get(0).getConfidenceClass());
            }
          }
        } else {
          Element el = (Element) node;
          String value = XmlUtil.getElementText(el);
          List<NormalizeDetails> normalizedValue = normalization.normalizeDetailed(value);

          if (normalizedValue.isEmpty()) {
            if (el.getAttributes().getLength() == 0 || (el.getAttributes().getLength() == 1
                && !StringUtils.isEmpty(el.getAttributeNS(Namespace.XML.getUri(), "lang")))) {
              el.getParentNode().removeChild(el);
            } else {
              NodeList childNodes = el.getChildNodes();
              for (int j = 0; j < childNodes.getLength(); j++) {
                Node childNode = childNodes.item(j);
                if (childNode.getNodeType() == Node.TEXT_NODE) {
                  el.removeChild(childNode);
                }
              }
            }
            report.increment(normalization.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
          } else {
            boolean valueChanged = normalizedValue.size() > 1
                || !normalizedValue.get(0).getNormalizedValue().equals(value);
            if (valueChanged) {
              if (normalizeToEuropeanaProxy && el.getParentNode() == providerProxy) {
                if (europeanaProxy == null) {
                  europeanaProxy = edm.createElementNS(Namespace.ORE.getUri(), "Proxy");
                  edm.getDocumentElement().appendChild(europeanaProxy);
                  Element europeanaProxyProp =
                      edm.createElementNS(Namespace.EDM.getUri(), "europeanaProxy");
                  europeanaProxyProp.appendChild(edm.createTextNode("true"));
                  europeanaProxy.appendChild(europeanaProxyProp);
                }
                for (NormalizeDetails aNormalizedValue : normalizedValue) {
                  report.increment(normalization.getClass().getSimpleName(),
                      aNormalizedValue.getConfidenceClass());

                  Node newEl = el.cloneNode(false);
                  newEl.appendChild(
                      el.getOwnerDocument().createTextNode(aNormalizedValue.getNormalizedValue()));
                  europeanaProxy.appendChild(newEl);
                }
              } else {
                NodeList childNodes = el.getChildNodes();
                for (int j = 0; j < childNodes.getLength(); j++) {
                  Node childNode = childNodes.item(j);
                  if (childNode.getNodeType() == Node.TEXT_NODE) {
                    el.removeChild(childNode);
                  }
                }
                el.appendChild(el.getOwnerDocument()
                    .createTextNode(normalizedValue.get(0).getNormalizedValue()));
                report.increment(normalization.getClass().getSimpleName(),
                    normalizedValue.get(0).getConfidenceClass());

                if (normalizedValue.size() > 1) {
                  for (int k = 1; k < normalizedValue.size(); k++) {
                    Node newEl = el.cloneNode(false);
                    report.increment(normalization.getClass().getSimpleName(),
                        normalizedValue.get(k).getConfidenceClass());
                    newEl.appendChild(el.getOwnerDocument()
                        .createTextNode(normalizedValue.get(k).getNormalizedValue()));
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
