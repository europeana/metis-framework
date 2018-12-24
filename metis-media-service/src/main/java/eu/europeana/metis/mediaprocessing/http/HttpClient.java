package eu.europeana.metis.mediaprocessing.http;

import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.List;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.protocol.HttpClientContext;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

abstract class HttpClient<R> implements Closeable {

  private final CloseableHttpClient client;

  HttpClient(int maxRedirectCount, int connectTimeout, int socketTimeout) {
    final RequestConfig requestConfig = RequestConfig.custom().setMaxRedirects(maxRedirectCount)
        .setConnectTimeout(connectTimeout).setSocketTimeout(socketTimeout).build();
    client = HttpClients.custom().setDefaultRequestConfig(requestConfig).build();
  }

  public final R download(RdfResourceEntry resourceEntry) throws IOException {

    // Set up the connection.
    final HttpGet httpGet = new HttpGet(resourceEntry.getResourceUrl());
    final HttpClientContext context = HttpClientContext.create();
    try (final CloseableHttpResponse response = client.execute(httpGet, context)) {

      // Check response code.
      final int status = response.getStatusLine().getStatusCode();
      if (status < 200 || status >= 300) {
        throw new IOException(
            "Download failed of resource " + resourceEntry.getResourceUrl() + ". Status code "
                + status + " (message: " + response.getStatusLine().getReasonPhrase() + ").");
      }

      // Obtain header information.
      final String mimeType = response.getEntity().getContentType().getValue();
      final List<URI> redirectUris = context.getRedirectLocations();
      final URI actualUri =
          redirectUris == null ? httpGet.getURI() : redirectUris.get(redirectUris.size() - 1);

      // Process the result.
      return createResult(resourceEntry, actualUri, mimeType, response.getEntity()::getContent);
    }
  }

  protected abstract R createResult(RdfResourceEntry resourceEntry, URI actualUri, String mimeType,
      ContentRetriever contentRetriever) throws IOException;

  @Override
  public void close() throws IOException {
    client.close();
  }

  @FunctionalInterface
  protected interface ContentRetriever {

    InputStream getContent() throws IOException;
  }
}
