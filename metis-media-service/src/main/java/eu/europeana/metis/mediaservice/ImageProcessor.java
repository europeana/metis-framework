package eu.europeana.metis.mediaservice;

import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.model.ImageResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResult;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import java.io.File;
import java.util.List;
import java.util.Set;
import org.apache.commons.lang3.tuple.Pair;

class ImageProcessor {

  private final ThumbnailGenerator thumbnailGenerator;

  ImageProcessor(ThumbnailGenerator thumbnailGenerator) {
    this.thumbnailGenerator = thumbnailGenerator;
  }

  ResourceExtractionResult processImage(String url, Set<UrlType> urlTypes, String mimeType,
      File content) throws MediaExtractionException {

    // Create the thumbnails for this image.
    final Pair<ImageMetadata, List<? extends Thumbnail>> thumbnailsAndMetadata =
        thumbnailGenerator.generateThumbnails(url, mimeType, content);

    // Set the metadata in the web resource.
    final ImageResourceMetadata resourceMetadata;
    if (UrlType.shouldExtractMetadata(urlTypes)) {
      final ImageMetadata imageMetadata = thumbnailsAndMetadata.getLeft();
      resourceMetadata = new ImageResourceMetadata(mimeType, url, content.length(),
          imageMetadata.getWidth(), imageMetadata.getHeight(),
          imageMetadata.getColorSpace(), imageMetadata.getDominantColors(),
          thumbnailsAndMetadata.getRight());
    } else {
      resourceMetadata = null;
    }

    // Done
    return new ResourceExtractionResult(resourceMetadata, thumbnailsAndMetadata.getRight());
  }
}
