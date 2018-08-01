package eu.europeana.indexing.mongo.property;

/**
 * Ancestor information consisting of the about value of the root (i.e. the full bean).
 */
public class RootAbout {

  private final String rootAbout;

  /**
   * Constructor.
   * 
   * @param rootAbout The about of the full bean.
   */
  public RootAbout(String rootAbout) {
    this.rootAbout = rootAbout;
  }

  public String getRootAbout() {
    return rootAbout;
  }
}
