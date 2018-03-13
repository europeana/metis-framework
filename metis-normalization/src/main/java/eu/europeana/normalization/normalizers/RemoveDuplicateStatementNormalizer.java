package eu.europeana.normalization.normalizers;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import javax.xml.xpath.XPathExpressionException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
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
public class RemoveDuplicateStatementNormalizer implements RecordNormalizer {

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

      final Set<TextLanguagePair> foundPairs = new HashSet<>();
      for (Element element : elements) {
        final TextLanguagePair pair = new TextLanguagePair(element);
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

  private static final class TextLanguagePair {

    private final String text;
    private final String language;

    private TextLanguagePair(Element element) {
      this.text = XmlUtil.getElementText(element);
      this.language = element.getAttributeNS(Namespace.XML.getUri(), "lang");
    }

    @Override
    public final boolean equals(Object otherObject) {
      if (otherObject == this) {
        return true;
      }
      if (!(otherObject instanceof TextLanguagePair)) {
        return false;
      }
      final TextLanguagePair otherPair = (TextLanguagePair) otherObject;
      final boolean languageEquals = this.language == null ? (otherPair.language == null)
          : this.language.equals(otherPair.language);
      return languageEquals && this.text.equals(otherPair.text);
    }

    @Override
    public int hashCode() {
      return Objects.hash(language, text);
    }
  }
}
