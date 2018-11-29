package eu.europeana.metis.mediaprocessing.temp;

import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
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
import eu.europeana.metis.mediaprocessing.exception.MediaProcessorException;

abstract class HttpClientTask implements Closeable {

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
  public void execute(List<FileInfo> files, Map<String, Integer> connectionLimitsPerSource,
      BiConsumer<FileInfo, String> callback) throws MediaProcessorException {

    updateConnectionLimits(files, connectionLimitsPerSource);

    List<HttpAsyncResponseConsumer<Void>> responseConsumers = new ArrayList<>();
    try {
      for (FileInfo fileInfo : files) {
        responseConsumers.add(createResponseConsumer(fileInfo, callback));
      }
    } catch (IOException e) {
      logger.error("Disk error?", e);
      for (HttpAsyncResponseConsumer<Void> c : responseConsumers) {
        c.failed(e);
      }
      throw new MediaProcessorException(e);
    }
    for (int i = 0; i < files.size(); i++) {
      // TODO use the callback function provided by httpClient.execute.
      httpClient.execute(createRequestProducer(files.get(i)), responseConsumers.get(i), null);
    }
  }

  private void updateConnectionLimits(List<FileInfo> fileInfos,
      Map<String, Integer> connectionLimitsPerSource) {
    connectionLimitsPerHost.putAll(connectionLimitsPerSource);
    for (FileInfo fileInfo : fileInfos) {
      URI uri = URI.create(fileInfo.getUrl());
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

  protected abstract HttpAsyncRequestProducer createRequestProducer(FileInfo fileInfo);

  protected abstract HttpAsyncResponseConsumer<Void> createResponseConsumer(FileInfo fileInfo,
      BiConsumer<FileInfo, String> callback) throws IOException;
}
