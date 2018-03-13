package eu.europeana.normalization.settings;

import eu.europeana.normalization.util.Namespace;
import eu.europeana.normalization.util.XpathQuery;

/**
 * This enum contains those XML elements that may contain language identifiers.
 */
public enum LanguageElements {

  /** The tag dc:language **/
  DC_LANGUAGE(XpathQuery.create("//%s/%s", Namespace.ORE.getElement("Proxy"),
      Namespace.DC.getElement("language"))),

  /** The attribute xml:lang **/
  XML_LANG(XpathQuery.create("//*[@%s]/@%s", Namespace.XML.getElement("lang"),
      Namespace.XML.getElement("lang"))),

  /** The combination of all elements. **/
  ALL(XpathQuery.combine(DC_LANGUAGE.getElementQuery(), XML_LANG.getElementQuery()));

  private final XpathQuery elementQuery;

  private LanguageElements(XpathQuery languageQuery) {
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
