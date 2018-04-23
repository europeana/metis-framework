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
 * <p>
 * This normalizer removes duplicate statements from within the proxy nodes. Only certain statements
 * are considered:
 * <ul>
 * <li>The title(s) and its alternative(s)</li>
 * <li>The subject(s)</li>
 * <li>The identifier(s)</li>
 * <li>The type(s)</li>
 * </ul>
 * Note that only duplicates <b>within</b> these groups are removed. For instance, it will look for
 * duplicates among all subject statements, but it will not compare any subject statement with a
 * type statement.
 * </p>
 * 
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

    // Create the new report.
    final InternalNormalizationReport report = new InternalNormalizationReport();

    // Allocate lists here and clear in the loop: for performance reasons.
    final List<Element> elements = new ArrayList<>();
    final Set<TextAttributesPair> foundPairs = new HashSet<>();

    // Try each combination separately: we don't want to compare across sets.
    for (XpathQuery[] fieldSet : FIELD_SETS_TO_EVALUATE) {

      // Collect all the elements.
      elements.clear();
      for (XpathQuery query : fieldSet) {
        try {
          elements.addAll(XmlUtil.getAsElementList(query.execute(edm)));
        } catch (XPathExpressionException e) {
          throw new NormalizationException("Xpath query issue: " + e.getMessage(), e);
        }
      }

      // If there are no duplicates because the list is too small, we are done.
      if (elements.size() <= 1) {
        continue;
      }

      // Make inventory of elements for uniqueness. Delete duplicate elements.
      foundPairs.clear();
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

    // Return report.
    return report;
  }

  /**
   * Class that represents an XML element with its attributes. This class can check for equality: an
   * element is considered equal if it has the same text, as well as exactly the same attributes
   * with the same values.
   * 
   * @author jochen
   *
   */
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
