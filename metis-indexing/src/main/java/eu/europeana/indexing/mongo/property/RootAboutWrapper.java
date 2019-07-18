package eu.europeana.indexing.mongo.property;

/**
 * Ancestor information consisting of the about value of the root (i.e. the full bean).
 */
public class RootAboutWrapper {

  private final String rootAbout;

  /**
   * Constructor.
   * 
   * @param rootAbout The about of the full bean.
   */
  public RootAboutWrapper(String rootAbout) {
    this.rootAbout = rootAbout;
  }

  public String getRootAbout() {
    return rootAbout;
  }
}
