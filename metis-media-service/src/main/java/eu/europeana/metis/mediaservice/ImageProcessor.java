package eu.europeana.metis.mediaservice;

import java.io.File;
import java.io.IOException;
import java.util.Collection;
import eu.europeana.metis.mediaservice.WebResource.Orientation;

class ImageProcessor {

  protected final ThumbnailGenerator thumbnailGenerator;

  ImageProcessor(ThumbnailGenerator thumbnailGenerator) {
    this.thumbnailGenerator = thumbnailGenerator;
  }

  static boolean isImage(String mimeType) {
    return mimeType.startsWith("image/");
  }

  void processImage(String url, Collection<UrlType> urlTypes, String mimeType, File content,
      EdmObject edm) throws MediaException, IOException {

    // Create the thumbnails for this image.
    final ImageMetadata imageMetadata =
        thumbnailGenerator.generateThumbnails(url, mimeType, content);

    // Set the metadata in the web resource.
    if (UrlType.shouldExtractMetadata(urlTypes)) {
      WebResource resource = edm.getWebResource(url);
      resource.setMimeType(mimeType);
      resource.setFileSize(content.length());
      resource.setWidth(imageMetadata.getWidth());
      resource.setHeight(imageMetadata.getHeight());
      resource.setOrientation(
          imageMetadata.getWidth() > imageMetadata.getHeight() ? Orientation.LANDSCAPE
              : Orientation.PORTRAIT);
      resource.setColorspace(imageMetadata.getColorSpace());
      resource.setDominantColors(imageMetadata.getDominantColors());
    }
  }
}
