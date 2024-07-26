package eu.europeana.metis.mediaprocessing.wrappers;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import org.apache.tika.Tika;
import org.apache.tika.detect.CompositeDetector;
import org.apache.tika.detect.DefaultDetector;
import org.apache.tika.metadata.Metadata;

/**
 * Wrapper class of Tika
 */
public class TikaWrapper {

  private final Tika tika;

  /**
   * It creates a new instance of Tika
   */
  public TikaWrapper() {
    OEmbedJsonFileDetector embedJsonFileDetector = new OEmbedJsonFileDetector();
    OEmbedXmlFileDetector embedXmlFileDetector = new OEmbedXmlFileDetector();
    CompositeDetector compositeDetector = new CompositeDetector(
        List.of(embedJsonFileDetector, embedXmlFileDetector, new DefaultDetector())
    );

    this.tika = new Tika(compositeDetector);
  }

  /**
   * It uses tika's own detect method
   *
   * @param inputStream The input stream to detect from
   * @param metadata The metadata associated with the input stream
   * @return The mime type detected from the input stream
   * @throws IOException
   */
  public String detect(InputStream inputStream, Metadata metadata) throws IOException {

    String detectedMimeType = tika.detect(inputStream, metadata);

    if (detectedMimeType.equals("application/vnd.ms-pki.stl")) {
      return "model/x.stl-binary";
    }

    return detectedMimeType;
  }
}
