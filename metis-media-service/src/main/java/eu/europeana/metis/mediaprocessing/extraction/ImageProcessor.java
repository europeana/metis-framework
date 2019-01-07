package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.ImageResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

/**
 * Implementation of {@link MediaProcessor} that is designed to handle resources of type
 * {@link ResourceType#IMAGE}.
 */
class ImageProcessor implements MediaProcessor {

  private final ThumbnailGenerator thumbnailGenerator;

  /**
   * Constructor.
   * 
   * @param thumbnailGenerator An object that can generate thumbnails.
   */
  ImageProcessor(ThumbnailGenerator thumbnailGenerator) {
    this.thumbnailGenerator = thumbnailGenerator;
  }

  @Override
  public ResourceExtractionResult process(String url, Set<UrlType> urlTypes, String mimeType,
      File content) throws MediaExtractionException {

    // Create the thumbnails for this image.
    final Pair<ImageMetadata, List<Thumbnail>> thumbnailsAndMetadata =
        thumbnailGenerator.generateThumbnails(url, ResourceType.IMAGE, content);

    // Set the metadata in the web resource.
    final ImageResourceMetadata resourceMetadata;
    if (UrlType.shouldExtractMetadata(urlTypes)) {
      final ImageMetadata imageMetadata = thumbnailsAndMetadata.getLeft();
      resourceMetadata = new ImageResourceMetadata(mimeType, url, content.length(),
          imageMetadata.getWidth(), imageMetadata.getHeight(), imageMetadata.getColorSpace(),
          imageMetadata.getDominantColors(), thumbnailsAndMetadata.getRight());
    } else {
      resourceMetadata = null;
    }

    // Done
    return new ResourceExtractionResult(resourceMetadata, thumbnailsAndMetadata.getRight());
  }
}
