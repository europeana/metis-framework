package eu.europeana.metis.network;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import eu.europeana.metis.network.AbstractHttpClient.ContentRetriever;
import eu.europeana.metis.network.StringHttpClient.StringContent;
import java.io.ByteArrayInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.List;
import org.apache.hc.core5.http.ContentType;
import org.apache.hc.core5.http.HttpEntity;
import org.apache.hc.core5.http.io.entity.BasicHttpEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link StringHttpClient}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class StringHttpClientTest {

  private StringHttpClient stringHttpClient;

  @BeforeEach
  void setUp() {
    stringHttpClient = new StringHttpClient(2, 2, 2, 2);
  }

  @Test
  void getResourceUrl() throws URISyntaxException {
    final String actualResourceUrl = stringHttpClient.getResourceUrl(new URI("/resource/test.url"));
    assertEquals("/resource/test.url", actualResourceUrl);
  }

  @Test
  void getResourceUrlWithException() {
    assertThrows(URISyntaxException.class, () -> {
      final String actualResourceUrl = stringHttpClient.getResourceUrl(new URI("\\resource/test.url"));

      assertEquals("/resource/test.url", actualResourceUrl);
    });
  }

  @Test
  void createResult() throws URISyntaxException, IOException {
    List<Closeable> closeables = new ArrayList<>();
    HttpEntity responseEntity = new BasicHttpEntity(new ByteArrayInputStream("content".getBytes()), ContentType.TEXT_PLAIN);
    final ContentRetriever contentRetriever = ContentRetriever.forNonCloseableContent(responseEntity::getContent,
        closeables::add);

    StringContent actualContent = stringHttpClient.createResult(new URI("/resource/provided"), new URI("/resource/actual"), null,
        "text/plain", 7L, contentRetriever);

    assertEquals("content", actualContent.getContent());
    assertEquals("text/plain", actualContent.getContentType());
    assertEquals(1, closeables.size());
  }

  @Test
  void createResultWithException() throws IOException {
    final ContentRetriever contentRetriever = mock(ContentRetriever.class);
    when(contentRetriever.getContent()).thenThrow(IOException.class);

    assertThrows(IOException.class,
        () -> stringHttpClient.createResult(new URI("/resource/provided"), new URI("/resource/actual"), null,
            "text/plain", 7L, contentRetriever));
  }

  @Test
  void stringContent() {
    StringContent stringContent = new StringContent("content", "text/plain");

    assertEquals("content", stringContent.getContent());
    assertEquals("text/plain", stringContent.getContentType());
  }
}
