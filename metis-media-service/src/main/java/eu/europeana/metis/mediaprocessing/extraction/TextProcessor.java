package eu.europeana.metis.mediaprocessing.extraction;

import eu.europeana.metis.mediaprocessing.exception.MediaExtractionException;
import eu.europeana.metis.mediaprocessing.extraction.pdf.PdfProcessor;
import eu.europeana.metis.mediaprocessing.extraction.pdf.PdfProcessor.PdfResult;
import eu.europeana.metis.mediaprocessing.model.Resource;
import eu.europeana.metis.mediaprocessing.model.ResourceExtractionResultImpl;
import eu.europeana.metis.mediaprocessing.model.TextResourceMetadata;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import eu.europeana.metis.schema.model.MediaType;
import java.io.IOException;

/**
 * <p>
 * Implementation of {@link MediaProcessor} that is designed to handle resources of type {@link
 * MediaType#TEXT}.
 * </p>
 * <p>
 * Note: if we don't have metadata, we don't return thumbnails either.
 * </p>
 */
class TextProcessor implements MediaProcessor {

  private static final String PDF_MIME_TYPE = "application/pdf";

  private final PdfProcessor pdfProcessor;

  TextProcessor(PdfProcessor pdfProcessor) {
    this.pdfProcessor = pdfProcessor;
  }

  @Override
  public boolean downloadResourceForFullProcessing() {
    return true;
  }

  @Override
  public ResourceExtractionResultImpl copyMetadata(Resource resource, String detectedMimeType) {
    return new ResourceExtractionResultImpl(new TextResourceMetadata(detectedMimeType,
        resource.getResourceUrl(), resource.getProvidedFileSize()));
  }

  boolean generateThumbnailForPdf(Resource resource, boolean mainThumbnailAvailable) {
    return !(mainThumbnailAvailable && resource.getUrlTypes().contains(UrlType.IS_SHOWN_BY));
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
    } catch (RuntimeException | IOException e) {
      throw new MediaExtractionException(
              "Could not determine the size of the resource " + resource.getResourceUrl(), e);
    }

    if (PDF_MIME_TYPE.equals(detectedMimeType)) {
      final PdfResult pdfResult = pdfProcessor.process(resource.getContentPath(), resource.getResourceUrl(),
          generateThumbnailForPdf(resource, mainThumbnailAvailable));
      final TextResourceMetadata textResourceMetadata = new TextResourceMetadata(detectedMimeType,
          resource.getResourceUrl(), contentSize, pdfResult.containsText(), nullIfNegative(pdfResult.resolution()), pdfResult.thumbnails());
      return new ResourceExtractionResultImpl(textResourceMetadata, pdfResult.thumbnails());
    }

    final boolean hasText = detectedMimeType.startsWith("text/") || "application/xhtml+xml".equals(detectedMimeType);
    final TextResourceMetadata metadata = new TextResourceMetadata(detectedMimeType,
        resource.getResourceUrl(), contentSize, hasText, null, null);
    return new ResourceExtractionResultImpl(metadata, null);
  }
}
