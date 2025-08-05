package eu.europeana.indexing.record.v2.entity;

import eu.europeana.corelib.edm.model.metainfo.WebResourceMetaInfoImpl;
import eu.europeana.indexing.record.v2.property.RootAboutWrapper;

/**
 * Ancestor information for {@link WebResourceMetaInfoImpl}, that contains the about values of the
 * root (i.e. full bean) and the web resource the meta info belongs to.
 */
public class WebResourceInformation extends RootAboutWrapper {

  private final String webResourceAbout;

  /**
   * Constructor.
   * 
   * @param rootAbout The about of the full bean.
   * @param webResourceAbout The about of the parent web resource.
   */
  public WebResourceInformation(String rootAbout, String webResourceAbout) {
    super(rootAbout);
    this.webResourceAbout = webResourceAbout;
  }

  public String getWebResourceAbout() {
    return webResourceAbout;
  }
}
