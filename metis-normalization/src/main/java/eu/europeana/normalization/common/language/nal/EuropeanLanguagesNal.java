package eu.europeana.normalization.common.language.nal;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import eu.europeana.normalization.common.language.LanguagesVocabulary;
import eu.europeana.normalization.util.NormalizationConfigurationException;
import eu.europeana.normalization.util.XmlException;
import eu.europeana.normalization.util.XmlUtil;

/**
 * Holds data from the European Languages NAL dump, which is used to support the normalization of
 * language values.
 * 
 * TODO JOCHEN load this statically, so that we don't have to read the file every time we want to do
 * normalization? Or give the option to the callers of the library?
 *
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 15/03/2016
 */
public class EuropeanLanguagesNal {

  private static final Logger LOGGER = LoggerFactory.getLogger(EuropeanLanguagesNal.class);

  private final List<NalLanguage> languages = new ArrayList<>();
  private final List<NalLanguage> deprecatedLanguages = new ArrayList<>();

  private LanguagesVocabulary targetVocabulary;

  private Map<String, NalLanguage> normalizedIndex;
  private Map<String, NalLanguage> isoCodeIndex;

  /**
   * Creates a new instance of this class.
   * 
   * @throws NormalizationConfigurationException
   */
  public EuropeanLanguagesNal() throws NormalizationConfigurationException {
    InputStream nalFileIn = getClass().getClassLoader().getResourceAsStream("languages.xml");
    Document langNalDom;
    try {
      langNalDom = XmlUtil.parseDom(new InputStreamReader(nalFileIn, "UTF-8"));
    } catch (XmlException | UnsupportedEncodingException e) {
      LOGGER.error("Unexpected error while setting up the language repository.", e);
      throw new NormalizationConfigurationException(
          "Unexpected error while setting up the language repository: " + e.getMessage(), e);
    }
    processDom(langNalDom);
    initIsoCodeIndex();
  }

  /**
   * @param langNalDom
   */
  private void processDom(Document langNalDom) {
    Iterable<Element> records = XmlUtil.elements(langNalDom.getDocumentElement(), "record");
    for (Element recordEl : records) {
      NalLanguage l = new NalLanguage(recordEl.getAttribute("id"));
      l.setIso6391(XmlUtil.getElementTextByTagName(recordEl, "iso-639-1"));
      l.setIso6392b(XmlUtil.getElementTextByTagName(recordEl, "iso-639-2b"));
      l.setIso6392t(XmlUtil.getElementTextByTagName(recordEl, "iso-639-2t"));
      l.setIso6393(XmlUtil.getElementTextByTagName(recordEl, "iso-639-3"));
      Element nameEl = XmlUtil.getElementByTagName(recordEl, "name");

      for (Element nameSubEl : XmlUtil.elements(nameEl)) {
        if (nameSubEl.getNodeName().equals("original.name")) {
          l.getOriginalNames().addAll(createLabels(nameSubEl));
        } else if (nameSubEl.getNodeName().equals("alternative.name")) {
          l.getAlternativeNames().addAll(createLabels(nameSubEl));
        }
      }
      Element labelEl = XmlUtil.getElementByTagName(recordEl, "label");
      l.getLabels().addAll(createLabels(labelEl));

      if (!recordEl.getAttribute("deprecated").equals("true")) {
        languages.add(l);
      } else {
        deprecatedLanguages.add(l);
      }
    }
  }

  private List<Label> createLabels(Element labelEl) {
    List<Label> list = new ArrayList<>();
    for (Element nameVersionEl : XmlUtil.elements(labelEl, "lg.version")) {
      list.add(createLabel(nameVersionEl));
    }
    return list;
  }

  private Label createLabel(Element nameVersionEl) {
    return new Label(nameVersionEl.getTextContent(), nameVersionEl.getAttribute("lg"),
        nameVersionEl.getAttribute("script"));
  }

  public List<NalLanguage> getLanguages() {
    return Collections.unmodifiableList(languages);
  }

  public List<NalLanguage> getDeprecatedLanguages() {
    return Collections.unmodifiableList(deprecatedLanguages);
  }

  public synchronized void initNormalizedIndex() {
    if (normalizedIndex == null) {
      normalizedIndex = new Hashtable<>();
      for (NalLanguage l : getLanguages()) {
        String normalizedLanguageId = l.getNormalizedLanguageId(targetVocabulary);
        if (normalizedLanguageId != null) {
          normalizedIndex.put(normalizedLanguageId, l);
        }
      }
    }
  }

  private void initIsoCodeIndex() {
    isoCodeIndex = new Hashtable<>();
    for (NalLanguage l : getLanguages()) {
      if (l.getIso6391() != null) {
        isoCodeIndex.put(l.getIso6391(), l);
      }
      if (l.getIso6392b() != null) {
        isoCodeIndex.put(l.getIso6392b(), l);
      }
      if (l.getIso6392t() != null) {
        isoCodeIndex.put(l.getIso6392t(), l);
      }
      if (l.getIso6393() != null) {
        isoCodeIndex.put(l.getIso6393(), l);
      }
    }
  }

  public NalLanguage lookupNormalizedLanguageId(String normalizedLanguageId) {
    return normalizedIndex.get(normalizedLanguageId);
  }

  public LanguagesVocabulary getTargetVocabulary() {
    return targetVocabulary;
  }

  public synchronized void setTargetVocabulary(LanguagesVocabulary target) {
    targetVocabulary = target;
  }

  public NalLanguage lookupIsoCode(String code) {
    return isoCodeIndex.get(code);
  }

}
