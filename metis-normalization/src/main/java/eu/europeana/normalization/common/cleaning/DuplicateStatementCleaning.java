package eu.europeana.normalization.common.cleaning;

import java.util.ArrayList;
import java.util.HashSet;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import eu.europeana.normalization.common.RecordNormalization;
import eu.europeana.normalization.model.ConfidenceLevel;
import eu.europeana.normalization.model.NormalizationReport;
import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.XmlUtil;
import eu.europeana.normalization.util.XpathQuery;

public class DuplicateStatementCleaning implements RecordNormalization {

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
  public NormalizationReport normalize(Document edm) {
    NormalizationReport report = new NormalizationReport();
    for (XpathQuery[] fieldSet : FIELD_SETS_TO_EVALUATE) {
      ArrayList<Element> elements = new ArrayList<>();
      for (XpathQuery query : fieldSet) {
        NodeList nodes;
        try {
          nodes = query.execute(edm);
        } catch (XPathExpressionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
        for (int i = 0; i < nodes.getLength(); i++) {
          elements.add((Element) nodes.item(i));
        }
      }
      if (elements.size() > 1) {
        HashSet<String> dupDetectorWithLang = new HashSet<>();

        for (Element el : elements) {
          String lang = el.getAttributeNS(Namespace.XML.getUri(), "lang");
          String key = XmlUtil.getElementText(el);
          String keyLang = key;
          if (!StringUtils.isEmpty(lang)) {
            keyLang = key + "@" + lang;
          }
          boolean dupLang = dupDetectorWithLang.contains(keyLang);

          if (dupLang) {
            el.getParentNode().removeChild(el);
            report.increment(this.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
          } else {
            dupDetectorWithLang.add(keyLang);
          }
        }
      }
    }
    return report;
  }

}
