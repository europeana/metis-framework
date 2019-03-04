package eu.europeana.indexing.mongo.property;

/**
 * Ancestor information consisting of the about value of the root (i.e. the full bean).
 */
class RootAboutWrapper {

  private final String rootAbout;

  /**
   * Constructor.
   * 
   * @param rootAbout The about of the full bean.
   */
  RootAboutWrapper(String rootAbout) {
    this.rootAbout = rootAbout;
  }

  String getRootAbout() {
    return rootAbout;
  }
}
