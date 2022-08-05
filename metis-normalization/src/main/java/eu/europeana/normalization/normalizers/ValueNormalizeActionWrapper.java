package eu.europeana.normalization.normalizers;

import eu.europeana.normalization.model.ConfidenceLevel;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.NormalizationException;
import eu.europeana.normalization.util.XmlUtil;
import eu.europeana.normalization.util.XpathQuery;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.IntStream;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

/**
 * This class represents a wrapper around a value normalize action (instance of {@link
 * ValueNormalizeAction}) in order to make it a record normalize action (instance of {@link
 * RecordNormalizeAction}). This wrapper can handle elements and attributes but not other kinds of
 * XML nodes.
 */
public class ValueNormalizeActionWrapper implements RecordNormalizeAction {

  private final List<XpathQuery> targetElements = new ArrayList<>();
  private final ValueNormalizeAction normalization;
  private final CopySettings copySettings;

  /**
   * Constructor.
   *
   * @param normalization The value normalization to wrap.
   * @param copySettings Where to store the normalized value. If this is null, the values are
   * overwritten.
   * @param targetElements The target nodes (elements or attributes) to which to apply this
   * normalization.
   */
  ValueNormalizeActionWrapper(ValueNormalizeAction normalization, CopySettings copySettings,
      XpathQuery... targetElements) {
    this.normalization = normalization;
    this.copySettings = copySettings;
    Collections.addAll(this.targetElements, targetElements);
  }

  /**
   * Constructor.
   *
   * @param normalization The value normalization to wrap.
   * @param targetElements The target nodes (elements or attributes) to which to apply this
   * normalization.
   */
  ValueNormalizeActionWrapper(ValueNormalizeAction normalization, XpathQuery... targetElements) {
    this(normalization, null, targetElements);
  }

  private void normalizeAttribute(Element copyTarget, Set<String> valuesAlreadyPresent,
      Attr attribute, InternalNormalizationReport report) {
    final String originalValue = attribute.getValue();
    final List<NormalizedValueWithConfidence> normalizedValue = normalization
        .normalizeValue(originalValue);
    if (normalizedValue.size() != 1) {
      if (copyTarget == null) {
        attribute.getOwnerElement().getAttributes().removeNamedItemNS(attribute.getNamespaceURI(),
            attribute.getLocalName());
        report.increment(normalization.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
      }
    } else if (copyTarget == null) {
      boolean valueChanged = !normalizedValue.get(0).getNormalizedValue().equals(originalValue);
      if (valueChanged) {
        updateAttribute(attribute, normalizedValue.get(0), report);
      }
    } else {
      copyValue(copyTarget, valuesAlreadyPresent, normalizedValue.get(0), report);
    }
  }

  private void normalizeElement(Element copyTarget, Set<String> valuesAlreadyPresent,
      Element element, InternalNormalizationReport report) {
    final String originalValue = XmlUtil.getElementText(element);
    final List<NormalizedValueWithConfidence> normalizedValues = normalization
        .normalizeValue(originalValue);
    if (normalizedValues.isEmpty()) {
      if (copyTarget == null) {
        removeTextFromElement(element);
        removeElementIfEmpty(element);
        report.increment(normalization.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
      }
    } else if (copyTarget == null) {
      boolean valueChanged = normalizedValues.size() > 1
          || !normalizedValues.get(0).getNormalizedValue().equals(originalValue);
      if (valueChanged) {
        updateElements(element, normalizedValues, report);
      }
    } else {
      normalizedValues.forEach(
          normalizedValue -> copyValue(copyTarget, valuesAlreadyPresent, normalizedValue, report));
    }
  }

  private void copyValue(Element copyTarget, Set<String> valuesAlreadyPresent,
      NormalizedValueWithConfidence value, InternalNormalizationReport report) {
    final boolean valueAdded = valuesAlreadyPresent.add(value.getNormalizedValue());
    if (valueAdded) {
      final Element newElement = XmlUtil.createElement(copySettings.getDestinationElement(),
          copyTarget, List.of(copySettings.getAfterElement()));
      addTextToElement(newElement, value, report);
    }
  }

  private static void removeElementIfEmpty(Element element) {
    if (element.hasChildNodes()) {
      return;
    }
    if (!element.hasAttributes() || (element.getAttributes().getLength() == 1
        && !StringUtils.isEmpty(element.getAttributeNS(Namespace.XML.getUri(), "lang")))) {
      element.getParentNode().removeChild(element);
    }
  }

  private void updateElements(Element element, List<NormalizedValueWithConfidence> normalizedValues,
      InternalNormalizationReport report) {

    // Remove all text and add one new text node with the first new value.
    removeTextFromElement(element);
    addTextToElement(element, normalizedValues.get(0), report);

    // Add the other values to shallow copies of the original node (attributes are copied, but text
    // isn't - this would not work if there are other kinds of child elements).
    normalizedValues.stream().skip(1).forEach(normalizedValue -> {
      final Element newElement = (Element) element.cloneNode(false);
      addTextToElement(newElement, normalizedValue, report);
      element.getParentNode().insertBefore(newElement, element.getNextSibling());
    });
  }

  private static void removeTextFromElement(Element element) {
    final NodeList childNodes = element.getChildNodes();
    IntStream.range(0, childNodes.getLength()).mapToObj(childNodes::item)
        .filter(childNode -> childNode.getNodeType() == Node.TEXT_NODE)
        .forEach(element::removeChild);
  }

  private void addTextToElement(Element element, NormalizedValueWithConfidence normalizedValue,
      InternalNormalizationReport report) {
    element.appendChild(
        element.getOwnerDocument().createTextNode(normalizedValue.getNormalizedValue()));
    report.increment(normalization.getClass().getSimpleName(),
        normalizedValue.getConfidenceClass());
  }

  private void updateAttribute(Attr attribute, NormalizedValueWithConfidence normalizedValue,
      InternalNormalizationReport report) {
    attribute.setValue(normalizedValue.getNormalizedValue());
    report.increment(normalization.getClass().getSimpleName(),
        normalizedValue.getConfidenceClass());
  }

  @Override
  public NormalizationReport normalize(Document edm) throws NormalizationException {

    // Create the report and analyze the copy settings
    final InternalNormalizationReport report = new InternalNormalizationReport();
    final Element copyTarget =
        copySettings == null ? null : getCopyTargetFromSettings(edm, copySettings);

    // Determine which values are already present.
    final Set<String> valuesAlreadyPresent = new HashSet<>();
    if (copyTarget != null) {
      final NodeList childNodes = copyTarget
          .getElementsByTagNameNS(copySettings.getDestinationElement().getNamespace().getUri(),
              copySettings.getDestinationElement().getElementName());
      XmlUtil.getAsElementList(childNodes).stream().map(XmlUtil::getElementText)
          .forEach(valuesAlreadyPresent::add);
    }

    // Normalize all required elements.
    for (XpathQuery query : targetElements) {

      // Find the values.
      final NodeList queryResult;
      try {
        queryResult = query.execute(edm);
      } catch (XPathExpressionException e) {
        throw new NormalizationException("Xpath query issue: " + e.getMessage(), e);
      }

      // Normalize the values.
      for (int i = 0; i < queryResult.getLength(); i++) {
        Node node = queryResult.item(i);
        if (node instanceof Attr) {
          normalizeAttribute(copyTarget, valuesAlreadyPresent, (Attr) node, report);
        } else if (node instanceof Element) {
          normalizeElement(copyTarget, valuesAlreadyPresent, (Element) node, report);
        }
      }
    }

    // Done: return the report.
    return report;
  }

  private static Element getCopyTargetFromSettings(Document edm, CopySettings copySettings)
      throws NormalizationException {
    return XmlUtil.getUniqueElement(copySettings.getDestinationParent(), edm);
  }

  static class CopySettings {

    private final XpathQuery destinationParent;
    private final Namespace.Element destinationElement;
    private final Namespace.Element afterElement;

    CopySettings(XpathQuery destinationParent,
        Namespace.Element destinationElement, Namespace.Element afterElement) {
      if (destinationElement == null || destinationParent == null) {
        throw new IllegalArgumentException();
      }
      this.destinationParent = destinationParent;
      this.destinationElement = destinationElement;
      this.afterElement = afterElement;
    }

    Namespace.Element getDestinationElement() {
      return destinationElement;
    }

    Namespace.Element getAfterElement() {
      return afterElement;
    }

    XpathQuery getDestinationParent() {
      return destinationParent;
    }
  }
}
