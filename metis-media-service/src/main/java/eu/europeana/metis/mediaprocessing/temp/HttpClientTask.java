package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.metis.mediaprocessing.exception.MediaException;
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.conn.routing.HttpRoute;
import org.apache.http.impl.nio.client.CloseableHttpAsyncClient;
import org.apache.http.impl.nio.client.HttpAsyncClients;
import org.apache.http.impl.nio.conn.PoolingNHttpClientConnectionManager;
import org.apache.http.impl.nio.reactor.DefaultConnectingIOReactor;
import org.apache.http.nio.protocol.HttpAsyncRequestProducer;
import org.apache.http.nio.protocol.HttpAsyncResponseConsumer;
import org.apache.http.pool.ConnPoolControl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

abstract class HttpClientTask<O> implements Closeable {

  private static final Logger logger = LoggerFactory.getLogger(HttpClientTask.class);

  protected final RequestConfig requestConfig;

  protected CloseableHttpAsyncClient httpClient;
  private ConnPoolControl<HttpRoute> connPoolControl;
  private HashMap<String, Integer> connectionLimitsPerHost = new HashMap<>();
  private HashMap<HttpRoute, Integer> currentClientLimits = new HashMap<>();

  public HttpClientTask(int followRedirects, int generalConnectionLimit,
      int connectionLimitPerSource, int connectTimeout, int socketTimeout)
      throws MediaProcessorException {
    try {
      PoolingNHttpClientConnectionManager connMgr =
          new PoolingNHttpClientConnectionManager(new DefaultConnectingIOReactor());
      connMgr.setDefaultMaxPerRoute(connectionLimitPerSource);
      connMgr.setMaxTotal(generalConnectionLimit);
      httpClient = HttpAsyncClients.custom().setConnectionManager(connMgr).build();
      httpClient.start();
      connPoolControl = connMgr;
    } catch (IOException e) {
      throw new MediaProcessorException("Could not initialize http client", e);
    }
    requestConfig = RequestConfig.custom()
        .setMaxRedirects(followRedirects)
        .setConnectTimeout(connectTimeout)
        .setSocketTimeout(socketTimeout)
        .build();
  }

  @Override
  public void close() {
    try {
      httpClient.close();
    } catch (RuntimeException | IOException e) {
      logger.error("HttpClient could not close", e);
    }
  }

  // TODO triggering callback with null status means that the status is OK.
  public <I extends RdfResourceEntry> void execute(List<I> resourceLinks,
      Map<String, Integer> connectionLimitsPerSource, HttpClientCallback<I, O> callback)
      throws MediaProcessorException {

    updateConnectionLimits(resourceLinks, connectionLimitsPerSource);

    List<HttpAsyncResponseConsumer<Void>> responseConsumers = new ArrayList<>();
    try {
      for (I resourceLink : resourceLinks) {
        responseConsumers.add(createResponseConsumer(resourceLink, callback));
      }
    } catch (IOException | MediaException e) {
      logger.error("Disk error?", e);
      for (HttpAsyncResponseConsumer<Void> c : responseConsumers) {
        c.failed(e);
      }
      throw new MediaProcessorException(e);
    }
    for (int i = 0; i < resourceLinks.size(); i++) {
      // TODO use the callback function provided by httpClient.execute.
      httpClient.execute(createRequestProducer(resourceLinks.get(i).getResourceUrl()),
          responseConsumers.get(i), null);
    }
  }

  private <I extends RdfResourceEntry> void updateConnectionLimits(List<I> resourceLinks,
      Map<String, Integer> connectionLimitsPerSource) {
    connectionLimitsPerHost.putAll(connectionLimitsPerSource);
    for (I resourceLink : resourceLinks) {
      URI uri = URI.create(resourceLink.getResourceUrl());
      Integer limit = connectionLimitsPerHost.get(uri.getHost());
      if (limit == null) {
        continue;
      }
      boolean secure = "https".equals(uri.getScheme());
      HttpRoute route = new HttpRoute(new HttpHost(uri.getHost(), uri.getPort(), uri.getScheme()),
          null, secure);
      Integer currentLimit = currentClientLimits.get(route);
      if (!limit.equals(currentLimit)) {
        connPoolControl.setMaxPerRoute(route, limit);
        currentClientLimits.put(route, limit);
      }
    }
  }

  protected abstract HttpAsyncRequestProducer createRequestProducer(String resourceUrl);

  protected abstract <I extends RdfResourceEntry> HttpAsyncResponseConsumer<Void> createResponseConsumer(
      I input, HttpClientCallback<I, O> callback) throws IOException, MediaException;
}
