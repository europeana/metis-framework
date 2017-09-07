package eu.europeana.normalization.common.cleaning;

import eu.europeana.normalization.common.RecordNormalization;
import eu.europeana.normalization.common.model.ConfidenceLevel;
import eu.europeana.normalization.common.model.NormalizationReport;
import eu.europeana.normalization.common.normalizers.ValueToRecordNormalizationWrapper.XpathQuery;
import eu.europeana.normalization.util.Namespaces;
import eu.europeana.normalization.util.XPathUtil;
import eu.europeana.normalization.util.XmlUtil;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import javax.xml.xpath.XPathExpressionException;
import org.apache.commons.lang.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

public class DuplicateStatementCleaning implements RecordNormalization {

  //	Europeana Proxy (dc:title, dcterms:alternative, dc:subject, dc:identifier, dc:type)
  XpathQuery PROXY_QUERY_TITLE = new XpathQuery(
      new HashMap<String, String>() {{
        put("rdf", Namespaces.RDF);
        put("ore", Namespaces.ORE);
        put("edm", Namespaces.EDM);
        put("dc", Namespaces.DC);
      }}, "/rdf:RDF/ore:Proxy/dc:title");
  XpathQuery PROXY_QUERY_ALTERNATIVE = new XpathQuery(
      new HashMap<String, String>() {{
        put("rdf", Namespaces.RDF);
        put("ore", Namespaces.ORE);
        put("edm", Namespaces.EDM);
        put("dcterms", Namespaces.DCTERMS);
      }}, "/rdf:RDF/ore:Proxy/dcterms:alternative");
  XpathQuery PROXY_QUERY_SUBJECT = new XpathQuery(
      new HashMap<String, String>() {{
        put("rdf", Namespaces.RDF);
        put("ore", Namespaces.ORE);
        put("edm", Namespaces.EDM);
        put("dc", Namespaces.DC);
      }}, "/rdf:RDF/ore:Proxy/dc:subject");
  XpathQuery PROXY_QUERY_IDENTIFIER = new XpathQuery(
      new HashMap<String, String>() {{
        put("rdf", Namespaces.RDF);
        put("ore", Namespaces.ORE);
        put("edm", Namespaces.EDM);
        put("dc", Namespaces.DC);
      }}, "/rdf:RDF/ore:Proxy/dc:identifier");
  XpathQuery PROXY_QUERY_TYPE = new XpathQuery(
      new HashMap<String, String>() {{
        put("rdf", Namespaces.RDF);
        put("ore", Namespaces.ORE);
        put("edm", Namespaces.EDM);
        put("dc", Namespaces.DC);
      }}, "/rdf:RDF/ore:Proxy/dc:type");

  XpathQuery[][] fieldSetsToValidate = new XpathQuery[][]{
      {PROXY_QUERY_TITLE, PROXY_QUERY_ALTERNATIVE}, {PROXY_QUERY_SUBJECT}, {PROXY_QUERY_IDENTIFIER},
      {PROXY_QUERY_TYPE}};

  @Override
  public NormalizationReport normalize(Document edm) {
    NormalizationReport report = new NormalizationReport();
    for (XpathQuery[] fieldSet : fieldSetsToValidate) {
      ArrayList<Element> elements = new ArrayList<>();
      for (XpathQuery query : fieldSet) {
        NodeList nodes;
        try {
          nodes = XPathUtil.queryDom(query.getNamespacesPrefixes(), query.getExpression(), edm);
        } catch (XPathExpressionException e) {
          throw new RuntimeException(e.getMessage(), e);
        }
        for (int i = 0; i < nodes.getLength(); i++) {
          elements.add((Element) nodes.item(i));
        }
      }
      if (elements.size() > 1) {
//				HashSet<String> dupDetector=new HashSet<>();
        HashSet<String> dupDetectorWithLang = new HashSet<>();

        for (int i = 0; i < elements.size(); i++) {
          Element el = elements.get(i);
          String lang = el.getAttributeNS(Namespaces.XML, "lang");
          String key = XmlUtil.getElementText(el);
          String keyLang = key;

//					boolean dup=dupDetector.contains(key);
          if (!StringUtils.isEmpty(lang)) {
            keyLang = key + "@" + lang;
          }
          boolean dupLang = dupDetectorWithLang.contains(keyLang);

          if (dupLang) {
            el.getParentNode().removeChild(el);
            report.increment(this.getClass().getSimpleName(), ConfidenceLevel.CERTAIN);
          } else {
            dupDetectorWithLang.add(keyLang);
            //				dupDetector.add(key);
          }
        }
      }
    }
    return report;
  }

}
