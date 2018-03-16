package eu.europeana.normalization.settings;

import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.XpathQuery;

/**
 * This enum contains those XML elements that may contain language identifiers.
 */
public enum LanguageElement {

  /** The tag dc:language **/
  DC_LANGUAGE(XpathQuery.create("//%s", Namespace.DC.getElement("language"))),

  /** The tag edm:language **/
  EDM_LANGUAGE(XpathQuery.create("//%s", Namespace.EDM.getElement("language"))),

  /** The attribute xml:lang **/
  XML_LANG(XpathQuery.create("//*[@%s]/@%s", Namespace.XML.getElement("lang"),
      Namespace.XML.getElement("lang")));

  private final XpathQuery elementQuery;

  private LanguageElement(XpathQuery languageQuery) {
    this.elementQuery = languageQuery;
  }

  /**
   * 
   * @return An instance of {@link XpathQuery} that obtains the elements of this type.
   */
  public XpathQuery getElementQuery() {
    return elementQuery;
  }
}
