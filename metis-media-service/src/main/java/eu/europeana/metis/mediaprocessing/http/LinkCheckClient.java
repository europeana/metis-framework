package eu.europeana.metis.mediaprocessing.http;

import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import java.io.IOException;
import java.net.URI;

public class LinkCheckClient extends HttpClient<Void> {

  public LinkCheckClient(int maxRedirectCount) {
    super(maxRedirectCount, 2000, 5000);
  }

  @Override
  protected Void createResult(RdfResourceEntry resourceEntry, URI actualUri, String mimeType,
      ContentRetriever contentRetriever) throws IOException {
    return null;
  }
}
