package eu.europeana.metis.mediaprocessing.http;

import eu.europeana.metis.mediaprocessing.model.RemoteResourceMetadata;
import eu.europeana.metis.mediaprocessing.wrappers.TikaWrapper;
import eu.europeana.metis.network.AbstractHttpClient;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;
import org.apache.tika.Tika;
import org.springframework.http.ContentDisposition;

/**
 * An {@link AbstractHttpClient} that tries to determine the mime type of a link. It does so based
 * on content only: so not using the URI or file name. It does not support redirects: it looks at
 * the content of the link it is given only. For specific information on how this type detection
 * functions, see {@link Tika#detect(URL)}.
 * <p>
 * The advantage of using this class over the {@link Tika} methods directly is that this provides
 * all the customary timeout options for connections within the media service.
 * <p>
 * the URL itself as resource entry input.
 */
public class MimeTypeDetectHttpClient extends AbstractHttpClient<URL, String> {

    private final TikaWrapper tika = new TikaWrapper();

    /**
     * Constructor.
     *
     * @param connectTimeout  The connection timeout in milliseconds.
     * @param responseTimeout The response timeout in milliseconds.
     * @param requestTimeout  The time after which the request will be aborted (if it hasn't finished
     */
    public MimeTypeDetectHttpClient(int connectTimeout, int responseTimeout, int requestTimeout) {
        super(0, connectTimeout, responseTimeout, requestTimeout);
    }

    @Override
    protected String getResourceUrl(URL resourceEntry) {
        return resourceEntry.toString();
    }

    @Override
    protected String createResult(URL providedLink, URI actualUri, ContentDisposition contentDisposition,
                                  String mimeType, Long fileSize, ContentRetriever contentRetriever) throws IOException {
        try (final InputStream inputStream = contentRetriever.getContent()) {
            return tika.detect(new RemoteResourceMetadata() {
                @Override
                public String getProvidedMimeType() {
                    return mimeType;
                }

                @Override
                public Long getProvidedFileSize() {
                    return fileSize;
                }

                @Override
                public ContentDisposition getProvidedContentDisposition() {
                    return contentDisposition;
                }

                @Override
                public URI getActualLocation() {
                    return actualUri;
                }
            }, inputStream);
        }
    }
}
