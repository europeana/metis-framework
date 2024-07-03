package eu.europeana.metis.mediaprocessing.wrappers;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.dataformat.xml.XmlMapper;
import eu.europeana.metis.mediaprocessing.extraction.oembed.OEmbedModel;
import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.detect.Detector;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.mime.MediaType;

public class OEmbedXmlFileDetector implements Detector {

  private static final MediaType OEMBED_XML = MediaType.application("xml+oembed");

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
   * @throws IOException if the document input stream could not be read
   */
  @Override
  public MediaType detect(InputStream input, Metadata metadata) {
    try {
      XmlMapper xmlMapper = new XmlMapper();
      xmlMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
      input.reset();
      OEmbedModel embedModel = xmlMapper.readValue(input.readAllBytes(), OEmbedModel.class);
      if (embedModel != null
          && (embedModel.getVersion() != null
          && embedModel.getVersion().startsWith("1.0"))
          && (embedModel.getType() != null
          && (embedModel.getType().equalsIgnoreCase("photo")
          || embedModel.getType().equalsIgnoreCase("video")))
          && (embedModel.getWidth() > 0 && embedModel.getHeight() > 0)) {
        return OEMBED_XML;
      }
    } catch (Exception e) {
      return MediaType.OCTET_STREAM;
    }
    return MediaType.OCTET_STREAM;
  }
}
