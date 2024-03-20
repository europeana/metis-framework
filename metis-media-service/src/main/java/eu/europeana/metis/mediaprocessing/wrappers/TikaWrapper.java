package eu.europeana.metis.mediaprocessing.wrappers;

import java.io.IOException;
import java.io.InputStream;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;

public class TikaWrapper {

  private final Tika tika;

  public TikaWrapper() {
    this.tika = new Tika();
  }

  public String detect(InputStream inputStream, Metadata metadata) throws IOException {

    String detectedMimeType = tika.detect(inputStream, metadata);

    if(detectedMimeType.equals("application/vnd.ms-pki.stl")){
      return "model/x.stl-binary";
    }

    return detectedMimeType;
  }
}
