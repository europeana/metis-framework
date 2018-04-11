package eu.europeana.normalization.languages;

import eu.europeana.normalization.util.NormalizationConfigurationException;
import eu.europeana.normalization.util.XmlException;
import eu.europeana.normalization.util.XmlUtil;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

/**
 * Holds data from the European Languages NAL dump, which is used to support the normalization of
 * language values.
 *
 * @author Nuno Freire (nfreire@gmail.com)
 */
public class Languages {

  private static final Logger LOGGER = LoggerFactory.getLogger(Languages.class);

  private static Languages instance;

  private final List<Language> activeLanguages = new ArrayList<>();
  private final List<Language> deprecatedLanguages = new ArrayList<>();

  private Languages() throws NormalizationConfigurationException {

    // Read the languages file into a DOM tree.
    InputStream nalFileIn =
        Thread.currentThread().getContextClassLoader().getResourceAsStream("languages.xml");
    Document langNalDom;
    try {
      langNalDom = XmlUtil.parseDom(new InputStreamReader(nalFileIn, StandardCharsets.UTF_8));
    } catch (XmlException e) {
      LOGGER.error("Unexpected error while setting up the language repository.", e);
      throw new NormalizationConfigurationException(
          "Unexpected error while setting up the language repository: " + e.getMessage(), e);
    }

    // Read the DOM tree into this object's variables.
    processDom(langNalDom);
  }

  private void processDom(Document langNalDom) {

    // Go by all records.
    Iterable<Element> records = XmlUtil.elements(langNalDom.getDocumentElement(), "record");
    for (Element recordEl : records) {

      // Set the standardized codes.
      Language l = new Language();
      l.setIso6391(XmlUtil.getElementTextByTagName(recordEl, "iso-639-1"));
      l.setIso6392b(XmlUtil.getElementTextByTagName(recordEl, "iso-639-2b"));
      l.setIso6392t(XmlUtil.getElementTextByTagName(recordEl, "iso-639-2t"));
      l.setIso6393(XmlUtil.getElementTextByTagName(recordEl, "iso-639-3"));
      l.setAuthorityCode(XmlUtil.getElementTextByTagName(recordEl, "authority-code"));

      // Read the name tag with the original and alternative names.
      Element nameEl = XmlUtil.getElementByTagName(recordEl, "name");
      for (Element nameSubEl : XmlUtil.elements(nameEl)) {
        if ("original.name".equals(nameSubEl.getNodeName())) {
          l.addOriginalNames(createLabels(nameSubEl));
        } else if ("alternative.name".equals(nameSubEl.getNodeName())) {
          l.addAlternativeNames(createLabels(nameSubEl));
        }
      }

      // Read the label tag with language labels.
      Element labelEl = XmlUtil.getElementByTagName(recordEl, "label");
      l.addLabels(createLabels(labelEl));

      // Add the language to either the deprecated or active language list.
      if ("true".equals(recordEl.getAttribute("deprecated"))) {
        deprecatedLanguages.add(l);
      } else {
        activeLanguages.add(l);
      }
    }
  }

  private List<LanguageLabel> createLabels(Element labelEl) {
    return XmlUtil.elements(labelEl, "lg.version").stream().map(this::createLabel)
        .collect(Collectors.toList());
  }

  private LanguageLabel createLabel(Element nameVersionEl) {
    return new LanguageLabel(nameVersionEl.getTextContent(), nameVersionEl.getAttribute("lg"),
        nameVersionEl.getAttribute("script"));
  }

  /**
   * This method provides access to the language list.
   * 
   * @return The language list.
   * @throws NormalizationConfigurationException In case the languages list could not be configured.
   */
  public static Languages getLanguages() throws NormalizationConfigurationException {
    synchronized (Languages.class) {
      if (instance == null) {
        instance = new Languages();
      }
      return instance;
    }
  }

  public List<Language> getActiveLanguages() {
    return Collections.unmodifiableList(activeLanguages);
  }

  public List<Language> getDeprecatedLanguages() {
    return Collections.unmodifiableList(deprecatedLanguages);
  }
}
