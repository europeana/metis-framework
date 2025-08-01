package eu.europeana.enrichment.rest.client;

import java.util.Arrays;
import java.util.Properties;
import org.apache.hc.client5.http.classic.HttpClient;
import org.apache.hc.client5.http.impl.classic.HttpClients;
import org.apache.hc.client5.http.impl.io.PoolingHttpClientConnectionManager;
import org.apache.hc.core5.http.io.SocketConfig;
import org.apache.hc.core5.util.Timeout;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.converter.xml.Jaxb2RootElementHttpMessageConverter;
import org.springframework.web.client.RestTemplate;

/**
 * Contains settings for creation of {@link RestTemplate}s.
 */
public class ConnectionProvider {

  /**
   * The default value of the maximum amount of time, in milliseconds, we wait for a connection to a resource before timing out.
   */
  public static final int DEFAULT_CONNECT_TIMEOUT = 10_000;

  /**
   * The default value of the maximum amount of time, in milliseconds, we wait for the response before timing out.
   */
  public static final int DEFAULT_RESPONSE_TIMEOUT = 60_000;

  private int connectTimeout = DEFAULT_CONNECT_TIMEOUT;
  private int responseTimeout = DEFAULT_RESPONSE_TIMEOUT;

  /**
   * Set the maximum amount of time, in milliseconds, we wait for a connection before timing out. The default (when not calling
   * this method) is {@value ConnectionProvider#DEFAULT_CONNECT_TIMEOUT} milliseconds.
   *
   * @param connectTimeout The maximum amount of time, in milliseconds, we wait for a connection before timing out. If not
   * positive, this signifies that the connection does not time out.
   */
  public void setConnectTimeout(int connectTimeout) {
    this.connectTimeout = connectTimeout;
  }

  /**
   * Set the maximum amount of time, in milliseconds, we wait for the response. The default (when not calling this method is
   * {@value ConnectionProvider#DEFAULT_RESPONSE_TIMEOUT} milliseconds.
   *
   * @param responseTimeout The maximum amount of time, in milliseconds, we wait for the response before timing out. If not
   * positive, this signifies that the response does not time out.
   */
  public void setResponseTimeout(int responseTimeout) {
    this.responseTimeout = responseTimeout;
  }

  /**
   * Creates a new Http connection using the values set up previously for the connection timeout and response time out
   *
   * @return a {@link RestTemplate} instance with all the information set up previously
   */
  protected RestTemplate createRestTemplate() {
    SocketConfig socketConfig = SocketConfig.custom().setSoTimeout(Timeout.ofMilliseconds(Math.max(responseTimeout, 0))).build();
    PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager();
    connectionManager.setDefaultSocketConfig(socketConfig);
    HttpClient httpClient = HttpClients.custom().setConnectionManager(connectionManager).build();

    final HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(httpClient);
    requestFactory.setConnectTimeout(Math.max(connectTimeout, 0));
    final RestTemplate restTemplate = new RestTemplate(requestFactory);
    restTemplate.setMessageConverters(Arrays.asList(new Jaxb2RootElementHttpMessageConverter(),
        new MappingJackson2HttpMessageConverter()));
    return restTemplate;
  }

  /**
   * Build entity api client properties properties.
   *
   * @param entityManagementUrl the entity management url
   * @param entityApiUrl the entity api url
   * @param entityApiTokenEndpoint the entity api token endpoint
   * @param entityApiGrantParams the entity api grant params
   * @return the properties
   */
  protected static Properties buildEntityApiClientProperties(String entityManagementUrl,
      String entityApiUrl,
      String entityApiTokenEndpoint,
      String entityApiGrantParams) {
    final Properties properties = new Properties();
    properties.put("entity.management.url", entityManagementUrl);
    properties.put("entity.api.url", entityApiUrl);
    properties.put("token_endpoint", entityApiTokenEndpoint);
    properties.put("grant_params", entityApiGrantParams);
    return properties;
  }
}
