package eu.europeana.metis.solr;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetSocketAddress;
import org.junit.jupiter.api.Test;

class SolrClientProviderTest {

  @Test
  void testInetSocketAddressToString() {

    // Define input
    final String ipAddress = "8.8.8.8";
    final String domainName = "europeana.eu";
    final int port1 = 1234;
    final int port2 = 1234;

    // Test single domain name
    final InetSocketAddress domainInput = new InetSocketAddress(domainName, port1);
    final String domainOutput = domainName + ":" + port1;
    assertEquals(domainOutput,
            SolrClientProvider.toCloudSolrClientAddressString(domainInput));

    // Test single ip address name
    final InetSocketAddress ipInput = new InetSocketAddress(ipAddress, port2);
    final String ipOutput = ipAddress + ":" + port2;
    assertEquals(ipOutput, SolrClientProvider.toCloudSolrClientAddressString(ipInput));
  }
}
