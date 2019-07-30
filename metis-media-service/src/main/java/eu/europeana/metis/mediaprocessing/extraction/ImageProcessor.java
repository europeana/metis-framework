package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.corelib.definitions.jibx.ColorSpaceType;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.ImageResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import java.io.IOException;
import java.util.List;
import org.apache.commons.lang3.tuple.Pair;

/**
 * <p>Implementation of {@link MediaProcessor} that is designed to handle resources of type
 * {@link eu.europeana.metis.utils.MediaType#IMAGE}.
 * </p>
 * <p>
 * Note: if we don't have metadata, we still return the thumbnails. This is according to the specs:
 * a thumbnail may be designated by the source record that is not itself a source image (it may be
 * derived from for instance a video).
 * </p>
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
  public boolean downloadResourceForFullProcessing() {
    return true;
  }

  @Override
  public ResourceExtractionResultImpl copyMetadata(Resource resource, String detectedMimeType)
      throws MediaExtractionException {
    return new ResourceExtractionResultImpl(new ImageResourceMetadata(detectedMimeType,
        resource.getResourceUrl(), resource.getProvidedFileSize()));
  }

  @Override
  public ResourceExtractionResultImpl extractMetadata(Resource resource, String detectedMimeType)
      throws MediaExtractionException {

    // Sanity check
    try {
      if (!resource.hasContent()) {
        throw new MediaExtractionException("File does not exist or does not have content.");
      }
    } catch (IOException e) {
      throw new MediaExtractionException("Could not determine whether resource has content.", e);
    }

    // Get the size of the resource
    final long contentSize;
    try {
      contentSize = resource.getContentSize();
    } catch (IOException e) {
      throw new MediaExtractionException(
          "Could not determine the size of the resource " + resource.getResourceUrl(), e);
    }

    // Create the thumbnails for this image.
    final Pair<ImageMetadata, List<Thumbnail>> thumbnailsAndMetadata =
        thumbnailGenerator.generateThumbnails(resource.getResourceUrl(), detectedMimeType,
            resource.getContentFile());

    // Set the metadata in the web resource.
    final ImageResourceMetadata resourceMetadata;
    final ImageMetadata imageMetadata = thumbnailsAndMetadata.getLeft();
    final ColorSpaceType colorSpace = ColorSpaceMapping
        .getColorSpaceType(imageMetadata.getColorSpace());
    resourceMetadata =
        new ImageResourceMetadata(detectedMimeType, resource.getResourceUrl(), contentSize,
            imageMetadata.getWidth(), imageMetadata.getHeight(), colorSpace,
            imageMetadata.getDominantColors(), thumbnailsAndMetadata.getRight());

    // Done.
    return new ResourceExtractionResultImpl(resourceMetadata, thumbnailsAndMetadata.getRight());
  }
}
