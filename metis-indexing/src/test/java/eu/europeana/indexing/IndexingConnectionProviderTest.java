package eu.europeana.indexing;

import static org.junit.Assert.assertEquals;
import java.net.InetSocketAddress;
import org.junit.Test;

public class IndexingConnectionProviderTest {

  @Test
  public void testInetSocketAddressToString() {

    final String ipAddressInput = "8.8.8.8";
    final String domainNameInput = "europeana.eu";
    final int port = 1234;

    final InetSocketAddress domainName = new InetSocketAddress(domainNameInput, port);
    assertEquals(domainNameInput + ":" + port,
        IndexingConnectionProvider.toZookeeperAddressString(domainName));

    final InetSocketAddress ipAddress = new InetSocketAddress(ipAddressInput, port);
    assertEquals(ipAddressInput + ":" + port,
        IndexingConnectionProvider.toZookeeperAddressString(ipAddress));
  }

}
