package eu.europeana.normalization.normalizers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import eu.europeana.normalization.model.ConfidenceLevel;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.NormalizationException;
import eu.europeana.normalization.util.XmlUtil;
import eu.europeana.normalization.util.XpathQuery;

/**
 * This class represents a wrapper around a value normalize action (instance of
 * {@link ValueNormalizeAction}) in order to make it a record normalize action (instance of
 * {@link RecordNormalizeAction}). This wrapper can handle elements and attributes but not other
 * kinds of XML nodes.
 */
public class ValueNormalizeActionWrapper implements RecordNormalizeAction {

  private final List<XpathQuery> targetElements = new ArrayList<>();
  private final ValueNormalizeAction normalization;

  /**
   * Constructor.
   * 
   * @param normalization The value normalization to wrap.
   * @param targetElements The target nodes (elements or attributes) to which to apply this
   *        normalization.
   */
  ValueNormalizeActionWrapper(ValueNormalizeAction normalization, XpathQuery... targetElements) {
    this.normalization = normalization;
    Collections.addAll(this.targetElements, targetElements);
  }

  private void normalizeAttribute(Attr attribute, InternalNormalizationReport report) {
    String value = attribute.getValue();
    List<NormalizedValueWithConfidence> normalizedValue = normalization.normalizeValue(value);
    if (normalizedValue.isEmpty() || normalizedValue.size() > 1) {
      attribute.getParentNode().getAttributes().removeNamedItemNS(attribute.getNamespaceURI(),
          attribute.getLocalName());
      report.increment(normalization.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
    } else {
      boolean valueChanged = !normalizedValue.get(0).getNormalizedValue().equals(value);
      if (valueChanged) {
        attribute.setValue(normalizedValue.get(0).getNormalizedValue());
        report.increment(normalization.getClass().getSimpleName(),
            normalizedValue.get(0).getConfidenceClass());
      }
    }
  }

  private void normalizeElement(Element element, InternalNormalizationReport report) {
    String value = XmlUtil.getElementText(element);
    List<NormalizedValueWithConfidence> normalizedValues = normalization.normalizeValue(value);
    if (normalizedValues.isEmpty()) {
      removeElement(element);
      report.increment(normalization.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
    } else {
      boolean valueChanged = normalizedValues.size() > 1
          || !normalizedValues.get(0).getNormalizedValue().equals(value);
      if (valueChanged) {
        updateElement(element, normalizedValues, report);
      }
    }
  }

  private void removeElement(Element element) {
    if (element.getAttributes().getLength() == 0 || (element.getAttributes().getLength() == 1
        && !StringUtils.isEmpty(element.getAttributeNS(Namespace.XML.getUri(), "lang")))) {
      element.getParentNode().removeChild(element);
    } else {
      NodeList childNodes = element.getChildNodes();
      for (int j = 0; j < childNodes.getLength(); j++) {
        Node childNode = childNodes.item(j);
        if (childNode.getNodeType() == Node.TEXT_NODE) {
          element.removeChild(childNode);
        }
      }
    }
  }

  private void updateElement(Element element, List<NormalizedValueWithConfidence> normalizedValues,
      InternalNormalizationReport report) {
    NodeList childNodes = element.getChildNodes();
    for (int j = 0; j < childNodes.getLength(); j++) {
      Node childNode = childNodes.item(j);
      if (childNode.getNodeType() == Node.TEXT_NODE) {
        element.removeChild(childNode);
      }
    }
    element.appendChild(
        element.getOwnerDocument().createTextNode(normalizedValues.get(0).getNormalizedValue()));
    report.increment(normalization.getClass().getSimpleName(),
        normalizedValues.get(0).getConfidenceClass());

    if (normalizedValues.size() > 1) {
      for (NormalizedValueWithConfidence normalizedValue : normalizedValues) {
        Node newEl = element.cloneNode(false);
        report.increment(normalization.getClass().getSimpleName(),
            normalizedValue.getConfidenceClass());
        newEl.appendChild(
            element.getOwnerDocument().createTextNode(normalizedValue.getNormalizedValue()));
        element.getParentNode().insertBefore(newEl, element.getNextSibling());
      }
    }
  }

  @Override
  public NormalizationReport normalize(Document edm) throws NormalizationException {
    InternalNormalizationReport report = new InternalNormalizationReport();
    for (XpathQuery query : targetElements) {
      NodeList queryResult;
      try {
        queryResult = query.execute(edm);
      } catch (XPathExpressionException e) {
        throw new NormalizationException("Xpath query issue: " + e.getMessage(), e);
      }
      for (int i = 0; i < queryResult.getLength(); i++) {
        Node node = queryResult.item(i);
        if (node instanceof Attr) {
          normalizeAttribute((Attr) node, report);
        } else if (node instanceof Element) {
          normalizeElement((Element) node, report);
        }
      }
    }
    return report;
  }
}
