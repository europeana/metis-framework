package eu.europeana.metis.mediaprocessing.http;

import eu.europeana.metis.network.AbstractHttpClient;
import org.apache.commons.lang3.StringUtils;
import org.apache.tika.Tika;
import org.apache.tika.metadata.Metadata;
import org.springframework.http.ContentDisposition;

import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URL;

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

    private final Tika tika = new Tika();

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

    private static String getResourceNameFromUrl(URI url) {
        final String resourcePath = url.getPath().trim();
        if (resourcePath.isEmpty() || resourcePath.endsWith("/")) {
            return null;
        }
        final int slashIndex = resourcePath.lastIndexOf('/');
        return slashIndex < 0 ? resourcePath : resourcePath.substring(slashIndex + 1);
    }

    /**
     * This method returns the resource name giving precedence to the information
     * that is contained in Content-Disposition header if exists.
     * If Content-Disposition resource name is empty then it gets it from the URI provided.
     *
     * @param contentDisposition content-disposition header information that can contain a resource name
     * @param actualUri          actual URI that can contain a resource name
     * @return String with the resource name
     */
    private static String getResourceNameFromContentDispositionOrFromActualURI(ContentDisposition contentDisposition, URI actualUri) {
        String extractedResourceName = contentDisposition != null &&
                (contentDisposition.isInline() || contentDisposition.isAttachment()) ? contentDisposition.getFilename() : "";
        if (StringUtils.isEmpty(extractedResourceName))
            extractedResourceName = getResourceNameFromUrl(actualUri);

        return extractedResourceName;
    }

    @Override
    protected String getResourceUrl(URL resourceEntry) {
        return resourceEntry.toString();
    }

    @Override
    protected String createResult(URL providedLink, URI actualUri, ContentDisposition contentDisposition,
                                  String mimeType, Long fileSize, ContentRetriever contentRetriever) throws IOException {
        try (final InputStream inputStream = contentRetriever.getContent()) {
            final Metadata metadata = new Metadata();
            final String resourceName = getResourceNameFromContentDispositionOrFromActualURI(contentDisposition, actualUri);
            if (resourceName != null) {
                metadata.set(Metadata.RESOURCE_NAME_KEY, resourceName);
            }
            if (mimeType != null) {
                final int separatorIndex = mimeType.indexOf(';');
                final String adjustedMimeType =
                        separatorIndex < 0 ? mimeType : mimeType.substring(0, separatorIndex);
                metadata.set(Metadata.CONTENT_TYPE, adjustedMimeType);
            }
            if (fileSize != null) {
                metadata.set(Metadata.CONTENT_LENGTH, fileSize.toString());
            }

            return tika.detect(inputStream, metadata);
        }
    }


}
