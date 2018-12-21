package eu.europeana.metis.mediaprocessing.http;

import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import java.io.IOException;
import java.net.URI;

// TODO should we first try a HEAD request before a GET? Pro: it may be faster. Con: it is not definitive as maybe the HEAD works but the GET doesn't.
public class LinkCheckClient extends HttpClient<Void> {

  public LinkCheckClient(int followRedirects) {
    super(followRedirects, 2000, 5000);
  }

  @Override
  protected Void createResult(RdfResourceEntry resourceEntry, URI actualUri, String mimeType,
      ContentRetriever contentRetriever) throws IOException {
    return null;
  }
}
