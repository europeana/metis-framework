package eu.europeana.metis.mediaprocessing.wrappers;

import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.getOEmbedModelFromXml;
import static eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedValidation.isValidOEmbedPhotoOrVideo;

import eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedModel;
import java.io.IOException;
import java.io.InputStream;
import java.io.Serial;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The type OEmbed xml file detector.
 */
public class OEmbedXmlFileDetector implements Detector {

  private static final Logger LOGGER = LoggerFactory.getLogger(OEmbedXmlFileDetector.class);
  private static final MediaType OEMBED_XML = MediaType.application("xml+oembed");
  @Serial
  private static final long serialVersionUID = -4502227849462535039L;

  /**
   * Detects the content type of the given input document. Returns
   * <code>application/octet-stream</code> if the type of the document
   * can not be detected.
   * <p>
   * If the document input stream is not available, then the first argument may be <code>null</code>. Otherwise the detector may
   * read bytes from the start of the stream to help in type detection. The given stream is guaranteed to support the
   * {@link InputStream#markSupported() mark feature} and the detector is expected to {@link InputStream#mark(int) mark} the
   * stream before reading any bytes from it, and to {@link InputStream#reset() reset} the stream before returning. The stream
   * must not be closed by the detector.
   * <p>
   * The given input metadata is only read, not modified, by the detector.
   *
   * @param input document input stream, or <code>null</code>
   * @param metadata input metadata for the document
   * @return detected media type, or <code>application/octet-stream</code>
   * @throws IOException exception if the document input stream could not be read
   */
  @Override
  public MediaType detect(InputStream input, Metadata metadata) throws IOException {
    try {
      input.mark(Integer.MAX_VALUE);
      OEmbedModel embedModel = getOEmbedModelFromXml(input.readAllBytes());
      if (isValidOEmbedPhotoOrVideo(embedModel)) {
        return OEMBED_XML;
      }
    } catch (IOException e) {
      LOGGER.warn("unable to read xml returning octet stream: ", e);
      return MediaType.OCTET_STREAM;
    } finally {
      input.reset();
    }
    return MediaType.OCTET_STREAM;
  }
}