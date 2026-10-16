package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.ImageDecoder.DecodedImageAndMetadata;
import eu.europeana.metis.mediaprocessing.model.ImageResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.Thumbnail;
import eu.europeana.metis.mediaprocessing.model.ThumbnailImpl;
import eu.europeana.metis.mediaprocessing.model.ThumbnailKind;
import eu.europeana.metis.schema.model.MediaType;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.List;

/**
 * <p>Implementation of {@link MediaProcessor} that is designed to handle resources of type
 * {@link MediaType#IMAGE}.
 * </p>
 * <p>
 * Note: if we don't have metadata, we still return the thumbnails. This is according to the specs: a thumbnail may be designated
 * by the source record that is not itself a source image (it may be derived from for instance a video).
 * </p>
 */
class ImageProcessor implements MediaProcessor {

  private static final String SVG_MIME_TYPE_PREFIX = "image/svg";

  private final ThumbnailGenerator thumbnailGenerator;
  private final ImageDecoder imageDecoder = new ImageDecoder();
  private final PaletteAnalyzer paletteAnalyzer = new PaletteAnalyzer();

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
  public ResourceExtractionResultImpl extractMetadata(Resource resource, String detectedMimeType,
      boolean mainThumbnailAvailable) throws MediaExtractionException {

    // Sanity check
    try {
      if (!resource.hasContent()) {
        throw new MediaExtractionException("File does not exist or does not have content.");
      }
    } catch (IOException e) {
      throw new MediaExtractionException("Could not determine whether resource has content.", e);
    }

    // Get the size of the resource
    final Long contentSize;
    try {
      contentSize = nullIfNegative(resource.getContentSize());
    } catch (IOException e) {
      throw new MediaExtractionException(
          "Could not determine the size of the resource " + resource.getResourceUrl(), e);
    }

    return processImage(resource.getResourceUrl(), detectedMimeType, resource.getContentFile(), contentSize);
  }

  ResourceExtractionResultImpl processImage(String url, String detectedMimeType, File content, Long contentSize)
      throws MediaExtractionException {
    if (content == null) {
      throw new MediaExtractionException("File content is null");
    }
    if (MediaType.getMediaType(detectedMimeType) != MediaType.IMAGE) {
      throw new MediaExtractionException("Cannot perform thumbnail generation on mime type '" + detectedMimeType + "'.");
    }

    final EnumMap<ThumbnailKind, ThumbnailImpl> thumbnails = thumbnailGenerator.prepareThumbnails(url, detectedMimeType);
    final ThumbnailKind largestKind = thumbnails.keySet().stream()
                                                .max(Comparator.comparingInt(ThumbnailKind::getImageSize)).orElseThrow();
    final int maximumWidth = largestKind.getImageSize();
    try (DecodedImageAndMetadata decoded = imageDecoder.decode(content, maximumWidth)) {
      // Load source bytes only when the original image can be reused as thumbnail content. SVGs must be rasterized.
      final byte[] reusableOriginalImageBytes =
          !detectedMimeType.startsWith(SVG_MIME_TYPE_PREFIX) && decoded.originalWidth() < maximumWidth
              ? Files.readAllBytes(content.toPath()) : null;
      final List<String> dominantColors =
          generateThumbnailsAndAnalyseColors(decoded, thumbnails, reusableOriginalImageBytes, largestKind);
      final List<Thumbnail> generatedThumbnails = new ArrayList<>(thumbnails.values());
      final ImageResourceMetadata imageResourceMetadata = new ImageResourceMetadata(detectedMimeType, url, contentSize,
          nullIfNegative(decoded.originalWidth()), nullIfNegative(decoded.originalHeight()), decoded.colorSpace(),
          dominantColors, generatedThumbnails);
      return new ResourceExtractionResultImpl(imageResourceMetadata, generatedThumbnails);
    } catch (MediaExtractionException e) {
      thumbnails.values().forEach(ThumbnailImpl::close);
      throw e;
    } catch (IOException e) {
      thumbnails.values().forEach(ThumbnailImpl::close);
      throw new MediaExtractionException("Could not process image", e);
    } catch (RuntimeException e) {
      thumbnails.values().forEach(ThumbnailImpl::close);
      throw new MediaExtractionException("Unexpected error during processing", e);
    }
  }

  private List<String> generateThumbnailsAndAnalyseColors(DecodedImageAndMetadata decodedImageAndMetadata,
      EnumMap<ThumbnailKind, ThumbnailImpl> thumbnails, byte[] reusableOriginalImageBytes, ThumbnailKind largestKind)
      throws IOException {
    //Skip generating thumbnails for the largest kind, we will resize it later if necessary
    for (var entry : thumbnails.entrySet()) {
      if (entry.getKey() == largestKind) {
        continue;
      }
      generateThumbnail(decodedImageAndMetadata, entry.getKey(), entry.getValue(), reusableOriginalImageBytes);
    }

    final ThumbnailImpl thumbnail = thumbnails.get(largestKind);
    if (reusableOriginalImageBytes != null) {
      thumbnail.markAsWithContent(reusableOriginalImageBytes);
      return paletteAnalyzer.dominantColors(decodedImageAndMetadata.image());
    }

    final BufferedImage largestBufferedImage =
        thumbnailGenerator.resize(decodedImageAndMetadata.image(), decodedImageAndMetadata.originalWidth(),
            decodedImageAndMetadata.originalHeight(), largestKind);
    try {
      thumbnailGenerator.encodeThumbnail(largestBufferedImage, thumbnail);
      return paletteAnalyzer.dominantColors(largestBufferedImage);
    } finally {
      largestBufferedImage.flush();
    }
  }

  private void generateThumbnail(DecodedImageAndMetadata decodedImageAndMetadata, ThumbnailKind thumbnailKind,
      ThumbnailImpl thumbnail, byte[] reusableOriginalImageBytes) throws IOException {
    if (reusableOriginalImageBytes != null && decodedImageAndMetadata.originalWidth() < thumbnailKind.getImageSize()) {
      thumbnail.markAsWithContent(reusableOriginalImageBytes);
      return;
    }
    final BufferedImage resizedBufferedImage = thumbnailGenerator.resize(decodedImageAndMetadata.image(),
        decodedImageAndMetadata.originalWidth(),
        decodedImageAndMetadata.originalHeight(), thumbnailKind);
    try {
      thumbnailGenerator.encodeThumbnail(resizedBufferedImage, thumbnail);
    } finally {
      resizedBufferedImage.flush();
    }
  }
}
