package eu.europeana.metis.mediaprocessing.model;

/**
 * This class defines the input for media extraction.
 */
public class MediaExtractorInput {

  private final RdfResourceEntry resourceEntry;
  private final boolean mainThumbnailAvailable;

  /**
   * Constructor.
   *
   * @param resourceEntry The resource entry to perform extraction on.
   * @param mainThumbnailAvailable Whether the main thumbnail for this record is available. This may
   * influence the decision on whether to generate a thumbnail for this resource.
   */
  public MediaExtractorInput(RdfResourceEntry resourceEntry, boolean mainThumbnailAvailable) {
    this.resourceEntry = resourceEntry;
    this.mainThumbnailAvailable = mainThumbnailAvailable;
  }

  public RdfResourceEntry getResourceEntry() {
    return resourceEntry;
  }

  public boolean isMainThumbnailAvailable() {
    return mainThumbnailAvailable;
  }
}
