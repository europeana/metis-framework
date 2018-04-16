package eu.europeana.normalization.normalizers;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import eu.europeana.normalization.model.ConfidenceLevel;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.NormalizationException;
import eu.europeana.normalization.util.XmlUtil;
import eu.europeana.normalization.util.XpathQuery;

/**
 * This normalizer removes duplicate statements from within the proxy nodes. Only certain statements
 * are considered.
 */
public class RemoveDuplicateStatementNormalizer implements RecordNormalizeAction {

  private static final XpathQuery PROXY_QUERY_TITLE =
      getProxySubtagQuery(Namespace.DC.getElement("title"));

  private static final XpathQuery PROXY_QUERY_ALTERNATIVE =
      getProxySubtagQuery(Namespace.DCTERMS.getElement("alternative"));

  private static final XpathQuery PROXY_QUERY_SUBJECT =
      getProxySubtagQuery(Namespace.DC.getElement("subject"));

  private static final XpathQuery PROXY_QUERY_IDENTIFIER =
      getProxySubtagQuery(Namespace.DC.getElement("identifier"));

  private static final XpathQuery PROXY_QUERY_TYPE =
      getProxySubtagQuery(Namespace.DC.getElement("type"));

  private static final XpathQuery[][] FIELD_SETS_TO_EVALUATE =
      new XpathQuery[][] {{PROXY_QUERY_TITLE, PROXY_QUERY_ALTERNATIVE}, {PROXY_QUERY_SUBJECT},
          {PROXY_QUERY_IDENTIFIER}, {PROXY_QUERY_TYPE}};

  private static final XpathQuery getProxySubtagQuery(Namespace.Element subtag) {
    return XpathQuery.create("/%s/%s/%s", XpathQuery.RDF_TAG, Namespace.ORE.getElement("Proxy"),
        subtag);
  }

  @Override
  public NormalizationReport normalize(Document edm) throws NormalizationException {
    final InternalNormalizationReport report = new InternalNormalizationReport();
    for (XpathQuery[] fieldSet : FIELD_SETS_TO_EVALUATE) {

      final List<Element> elements = new ArrayList<>();
      for (XpathQuery query : fieldSet) {
        try {
          elements.addAll(XmlUtil.getAsElementList(query.execute(edm)));
        } catch (XPathExpressionException e) {
          throw new NormalizationException("Xpath query issue: " + e.getMessage(), e);
        }
      }

      if (elements.size() <= 1) {
        continue;
      }

      final Set<TextAttributesPair> foundPairs = new HashSet<>();
      for (Element element : elements) {
        final TextAttributesPair pair = new TextAttributesPair(element);
        if (foundPairs.contains(pair)) {
          element.getParentNode().removeChild(element);
          report.increment(this.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
        } else {
          foundPairs.add(pair);
        }
      }
    }

    return report;
  }

  static final class TextAttributesPair {

    private final String text;
    private Map<String, String> attributes;

    TextAttributesPair(Element element) {
      this(XmlUtil.getElementText(element), convertToMap(element.getAttributes()));
    }

    TextAttributesPair(String text, Map<String, String> attributes) {
      this.text = text == null ? "" : text;
      this.attributes = attributes == null ? Collections.emptyMap() : attributes;
    }

    static Map<String, String> convertToMap(NamedNodeMap attributeNodeMap) {
      return IntStream.range(0, attributeNodeMap.getLength()).mapToObj(attributeNodeMap::item)
          .map(node -> (Attr) node).filter(node -> StringUtils.isNotBlank(node.getNodeValue()))
          .collect(Collectors.toMap(Attr::getNodeName, Attr::getNodeValue));
    }

    @Override
    public boolean equals(Object otherObject) {
      if (otherObject == this) {
        return true;
      }
      if (!(otherObject instanceof TextAttributesPair)) {
        return false;
      }
      final TextAttributesPair otherPair = (TextAttributesPair) otherObject;
      return this.attributes.equals(otherPair.attributes) && this.text.equals(otherPair.text);
    }

    @Override
    public int hashCode() {
      return Objects.hash(attributes, text);
    }
  }
}
