package eu.europeana.metis.authentication.rest.client;


import static com.github.tomakehurst.wiremock.client.WireMock.aResponse;
import static com.github.tomakehurst.wiremock.client.WireMock.equalTo;
import static com.github.tomakehurst.wiremock.client.WireMock.get;
import static com.github.tomakehurst.wiremock.client.WireMock.urlEqualTo;
import static com.github.tomakehurst.wiremock.core.WireMockConfiguration.wireMockConfig;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import com.github.tomakehurst.wiremock.WireMockServer;
import eu.europeana.metis.authentication.user.MetisUser;
import eu.europeana.metis.exception.UserUnauthorizedException;
import eu.europeana.metis.utils.NetworkUtil;
import java.io.IOException;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-02
 */
class TestAuthenticationClient {

  private static int portForWireMock = 9999;

  static {
    try {
      portForWireMock = NetworkUtil.getAvailableLocalPort();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private static WireMockServer wireMockServer;
  private static AuthenticationClient authenticationClient;

  @BeforeAll
  static void setUp() {
    wireMockServer = new WireMockServer(wireMockConfig().port(portForWireMock));
    wireMockServer.start();
    authenticationClient = new AuthenticationClient("http://127.0.0.1:" + portForWireMock);
  }

  @AfterAll
  static void destroy() {
    wireMockServer.stop();
  }

  @Test
  void testGetUserByAccessTokenInHeader() throws Exception {
    wireMockServer.stubFor(get(urlEqualTo("/authentication/user_by_access_token"))
        .withHeader("Authorization", equalTo("Bearer vq6V1YJIOfLC0pSTeb1plANiopyVlwrx"))
        .withHeader("Accept", equalTo("application/json"))
        .willReturn(aResponse()
            .withStatus(200)
            .withHeader("Content-Type", "application/json")
            .withBodyFile("MetisUser.json")));

    MetisUser userByAccessTokenInHeader = authenticationClient
        .getUserByAccessTokenInHeader("Bearer vq6V1YJIOfLC0pSTeb1plANiopyVlwrx");
    assertNotNull(userByAccessTokenInHeader);
  }

  @Test
  void testGetUserByAccessTokenInHeaderHttpClientErrorException() {
    wireMockServer.stubFor(get(urlEqualTo("/authentication/user_by_access_token"))
        .withHeader("Authorization", equalTo("Bearer OUwbCoeELS28sF"))
        .withHeader("Accept", equalTo("application/json"))
        .willReturn(aResponse()
            .withStatus(401)
            .withHeader("Content-Type", "application/json")
            .withBody("{\"errorMessage\":\"Wrong access token\"}")));

    assertThrows(UserUnauthorizedException.class,
        () -> authenticationClient.getUserByAccessTokenInHeader("Bearer OUwbCoeELS28sF"));
  }
}
