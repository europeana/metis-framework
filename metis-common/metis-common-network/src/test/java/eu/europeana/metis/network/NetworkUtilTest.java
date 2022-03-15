package eu.europeana.metis.network;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.net.InetAddress;
import javax.net.ServerSocketFactory;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link NetworkUtil}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class NetworkUtilTest {

  @Test
  void getAvailableLocalPort() throws IOException {
    int availableLocalPort = new NetworkUtil().getAvailableLocalPort();
    assertTrue(availableLocalPort > 0);
  }

  @Test
  void getAvailableLocalPortWithException() throws IOException {
    final int BACKLOG = 100;
    final ServerSocketFactory sslServerSocketFactory = mock(ServerSocketFactory.class);
    when(sslServerSocketFactory.createServerSocket(0, BACKLOG, InetAddress.getByName("localhost"))).thenThrow(IOException.class);
    final NetworkUtil networkUtil = spy(NetworkUtil.class);
    when(networkUtil.getServerSocketFactory()).thenReturn(sslServerSocketFactory);
    assertThrows(IOException.class, networkUtil::getAvailableLocalPort);
  }
}