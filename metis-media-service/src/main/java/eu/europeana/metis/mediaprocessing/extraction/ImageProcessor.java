package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
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

  /**
   * Decodes an image once, generates thumbnails, and analyses colours without upscaling raster images.
   */
  ResourceExtractionResultImpl processImage(String url, String detectedMimeType, File content, Long contentSize)
      throws MediaExtractionException {
    if (content == null) {
      throw new MediaExtractionException("File content is null");
    }
    if (MediaType.getMediaType(detectedMimeType) != MediaType.IMAGE) {
      throw new MediaExtractionException("Cannot perform thumbnail generation on mime type '" + detectedMimeType + "'.");
    }
    if (detectedMimeType.startsWith("image/vnd.djvu") || detectedMimeType.startsWith("image/x-djvu")
        || detectedMimeType.startsWith("image/x.djvu")) {
      throw new MediaExtractionException("Cannot generate thumbnails for DjVu file.");
    }

    final EnumMap<ThumbnailKind, ThumbnailImpl> thumbnails = thumbnailGenerator.prepareThumbnails(url, detectedMimeType);
    final ThumbnailKind largestKind = thumbnails.keySet().stream()
                                                .max(Comparator.comparingInt(ThumbnailKind::getImageSize)).orElseThrow();
    final int maximumWidth = largestKind.getImageSize();
    try (ImageDecoder.DecodedImage decoded = imageDecoder.decode(content, maximumWidth)) {
      // SVGs must be rasterized. Other small originals can be reused without resizing or encoding.
      final byte[] original = !detectedMimeType.startsWith("image/svg") && decoded.originalWidth() < maximumWidth
          ? Files.readAllBytes(content.toPath()) : null;
      final List<String> dominantColors = generateThumbnailsAndAnalyseColors(decoded, thumbnails, original, largestKind);
      final List<Thumbnail> generatedThumbnails = new ArrayList<>(thumbnails.values());
      final ImageResourceMetadata metadata = new ImageResourceMetadata(detectedMimeType, url, contentSize,
          nullIfNegative(decoded.originalWidth()), nullIfNegative(decoded.originalHeight()), decoded.colorSpace(),
          dominantColors, generatedThumbnails);
      return new ResourceExtractionResultImpl(metadata, generatedThumbnails);
    } catch (MediaExtractionException e) {
      thumbnails.values().forEach(ThumbnailImpl::close);
      throw e;
    } catch (IOException e) {
      thumbnails.values().forEach(ThumbnailImpl::close);
      throw new MediaExtractionException("Could not generate thumbnails in Java", e);
    } catch (RuntimeException e) {
      thumbnails.values().forEach(ThumbnailImpl::close);
      throw new MediaExtractionException("Unexpected error during processing", e);
    }
  }

  /**
   * Generates smaller thumbnails first, then analyses the original pixels or the largest thumbnail.
   */
  private List<String> generateThumbnailsAndAnalyseColors(ImageDecoder.DecodedImage decodedImage,
      EnumMap<ThumbnailKind, ThumbnailImpl> thumbnails, byte[] original, ThumbnailKind largestKind) throws IOException {
    for (var entry : thumbnails.entrySet()) {
      if (entry.getKey() == largestKind) {
        continue;
      }
      generateThumbnail(decodedImage, entry.getKey(), entry.getValue(), original);
    }

    final ThumbnailImpl thumbnail = thumbnails.get(largestKind);
    if (original != null) {
      thumbnail.markAsWithContent(original);
      return paletteAnalyzer.dominantColors(decodedImage.image());
    }

    final BufferedImage largestBufferedImage = thumbnailGenerator.resize(decodedImage.image(), decodedImage.originalWidth(),
        decodedImage.originalHeight(), largestKind);
    try {
      thumbnailGenerator.encodeThumbnail(largestBufferedImage, thumbnail);
      // Use the uncompressed resized pixels for colour analysis.
      return paletteAnalyzer.dominantColors(largestBufferedImage);
    } finally {
      largestBufferedImage.flush();
    }
  }

  /**
   * Reuses a small original, otherwise resizes and encodes the thumbnail.
   */
  private void generateThumbnail(ImageDecoder.DecodedImage decodedImage, ThumbnailKind kind, ThumbnailImpl thumbnail,
      byte[] originalImageBytes) throws IOException {
    if (originalImageBytes != null && decodedImage.originalWidth() < kind.getImageSize()) {
      thumbnail.markAsWithContent(originalImageBytes);
      return;
    }
    final BufferedImage resized = thumbnailGenerator.resize(decodedImage.image(), decodedImage.originalWidth(),
        decodedImage.originalHeight(), kind);
    try {
      thumbnailGenerator.encodeThumbnail(resized, thumbnail);
    } finally {
      resized.flush();
    }
  }
}
