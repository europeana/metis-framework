package eu.europeana.metis.mediaprocessing.http;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import org.apache.tika.Tika;

/**
 * An {@link AbstractHttpClient} that tries to determine the mime type of a link. It does so based
 * on content only: so not using the URI or file name. It does not support redirects: it looks at
 * the content of the link it is given only. For specific information on how this type detection
 * functions, see {@link Tika#detect(URL)}.
 *
 * The advantage of using this class over the {@link Tika} methods directly is that this provides
 * all the customary timeout options for connections within the media service.
 *
 * the URL itself as resource entry input.
 */
public class MimeTypeDetectHttpClient extends AbstractHttpClient<URL, String> {

  private final Tika tika = new Tika();

  /**
   * Constructor.
   *
   * @param connectTimeout The connection timeout in milliseconds.
   * @param socketTimeout The socket timeout in milliseconds.
   * @param requestTimeout The time after which the request will be aborted (if it hasn't finished
   */
  public MimeTypeDetectHttpClient(int connectTimeout, int socketTimeout, int requestTimeout) {
    super(0, connectTimeout, socketTimeout, requestTimeout);
  }

  @Override
  protected String getResourceUrl(URL resourceEntry) {
    return resourceEntry.toString();
  }

  @Override
  protected String createResult(URL resourceEntry, URI actualUri, String mimeType, Long fileSize,
      ContentRetriever contentRetriever) throws IOException {
    try (final InputStream inputStream = contentRetriever.getContent()) {
      return tika.detect(inputStream);
    }
  }
}
