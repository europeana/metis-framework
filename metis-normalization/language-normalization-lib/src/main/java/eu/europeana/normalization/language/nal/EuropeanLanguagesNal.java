/* EuropaEuLanguagesNal.java - created on 15/03/2016, Copyright (c) 2011 The European Library, all rights reserved */
package eu.europeana.normalization.language.nal;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import eu.europeana.normalization.language.nlp.IndexUtilUnicode;
import eu.europeana.normalization.language.util.MapOfLists;
import eu.europeana.normalization.language.util.XmlUtil;

/**
 * Holds data from the European Languages NAL dump, which is used to support the normalization of language values. 
 * 
 * @author Nuno Freire (nfreire@gmail.com)
 * @since 15/03/2016
 */
public class EuropeanLanguagesNal {
    private static java.util.logging.Logger log                 = java.util.logging.Logger.getLogger(EuropeanLanguagesNal.class.getName());

    List<NalLanguage>                       languages           = new ArrayList<NalLanguage>();
    List<NalLanguage>                       deprecatedLanguages = new ArrayList<NalLanguage>();

    /**
     * Creates a new instance of this class.
     */
    public EuropeanLanguagesNal(File xmlSourceFile) {
        Document langNalDom = XmlUtil.parseDomFromFile(xmlSourceFile);
        processDom(langNalDom);
    }

    /**
     * Creates a new instance of this class.
     */
    public EuropeanLanguagesNal() {
        InputStream nalFileIn = getClass().getClassLoader().getResourceAsStream("languages.xml");
        Document langNalDom = XmlUtil.parseDom(new InputStreamReader(nalFileIn));
        processDom(langNalDom);
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
                    for (Element nameVersionEl : XmlUtil.elements(nameSubEl, "lg.version")) {
                        Label origLabel = new Label(nameVersionEl.getTextContent(),
                                nameVersionEl.getAttribute("lg"),
                                nameVersionEl.getAttribute("script"));
                        l.getOriginalNames().add(origLabel);
                    }
                } else if (nameSubEl.getNodeName().equals("alternative.name")) {
                    for (Element nameVersionEl : XmlUtil.elements(nameSubEl, "lg.version")) {
                        Label origLabel = new Label(nameVersionEl.getTextContent(),
                                nameVersionEl.getAttribute("lg"),
                                nameVersionEl.getAttribute("script"));
                        l.getAlternativeNames().add(origLabel);
                    }

                }
            }
            Element labelEl = XmlUtil.getElementByTagName(recordEl, "label");
            for (Element nameVersionEl : XmlUtil.elements(labelEl, "lg.version")) {
                Label origLabel = new Label(nameVersionEl.getTextContent(),
                        nameVersionEl.getAttribute("lg"), nameVersionEl.getAttribute("script"));
                l.getLabels().add(origLabel);
            }
            if (!recordEl.getAttribute("deprecated").equals("true"))
                languages.add(l);
            else
                deprecatedLanguages.add(l);
// log.info("Missing language code for: "+recordEl.getAttribute("id"));
        }
    }

    public List<NalLanguage> getLanguages() {
        return languages;
    }

    public List<NalLanguage> getDeprecatedLanguages() {
        return deprecatedLanguages;
    }

}
