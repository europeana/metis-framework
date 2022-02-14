package eu.europeana.metis.network;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Unit test for {@link NetworkUtil}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class NetworkUtilTest {

  @Test
  void getAvailableLocalPort() throws IOException {
    int availableLocalPort = NetworkUtil.getAvailableLocalPort();

    assertTrue(availableLocalPort > 0);
  }

  @Test
  void getAvailableLocalPortWithException() throws IOException {
    final int BACKLOG = 100;
    try (MockedStatic<SSLServerSocketFactory> sslServerSocketFactory = mockStatic(SSLServerSocketFactory.class)) {
      ServerSocketFactory serverSocketFactory = mock(ServerSocketFactory.class);
      sslServerSocketFactory.when(SSLServerSocketFactory::getDefault).thenReturn(serverSocketFactory);
      when(serverSocketFactory.createServerSocket(0, BACKLOG, InetAddress.getByName("localhost"))).thenThrow(IOException.class);

      assertThrows(IOException.class, () -> NetworkUtil.getAvailableLocalPort());
    }
  }
}