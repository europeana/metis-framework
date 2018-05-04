package eu.europeana.indexing;

import static org.junit.Assert.assertEquals;
import java.net.InetSocketAddress;
import org.junit.Test;

public class SettingsConnectionProviderTest {

  @Test
  public void testInetSocketAddressToString() {

    // Define input
    final String ipAddress = "8.8.8.8";
    final String domainName = "europeana.eu";
    final int port1 = 1234;
    final int port2 = 1234;

    // Test single domain name
    final InetSocketAddress domainInput = new InetSocketAddress(domainName, port1);
    final String domainOutput = domainName + ":" + port1;
    assertEquals(domainOutput,
        SettingsConnectionProvider.toCloudSolrClientAddressString(domainInput));

    // Test single ip address name
    final InetSocketAddress ipInput = new InetSocketAddress(ipAddress, port2);
    final String ipOutput = ipAddress + ":" + port2;
    assertEquals(ipOutput, SettingsConnectionProvider.toCloudSolrClientAddressString(ipInput));
  }

}
