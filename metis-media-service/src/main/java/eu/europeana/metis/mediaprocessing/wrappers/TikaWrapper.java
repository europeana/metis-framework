package eu.europeana.metis.mediaprocessing.wrappers;

import static org.apache.tika.metadata.TikaCoreProperties.RESOURCE_NAME_KEY;

import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import org.apache.tika.Tika;
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
    this.tika = new Tika();
  }

  /**
   * It uses tika's own detect method
   *
   * @param inputStream The input stream to detect from
   * @param metadata The metadata associated with the input stream
   * @return The mime type detected from the input stream
   * @throws IOException in case detection fails
   */
  public String detect(InputStream inputStream, Metadata metadata) throws IOException {
    String detect = tika.detect(inputStream, metadata);

    //LAS normalization. We have to do a code matching since the glob matching already exists in tika.mimetypes.xml
    if ("application/x-asprs".equals(detect)) {
      if (metadata.get(RESOURCE_NAME_KEY).toLowerCase(Locale.ROOT).endsWith(".laz")) {
        detect = "application/vnd.laszip";
      } else {
        detect = "application/vnd.las";
      }
    }
    return detect;
  }
}
