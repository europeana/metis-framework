package eu.europeana.indexing;

import static org.junit.Assert.assertEquals;
import java.net.InetSocketAddress;
import java.util.Arrays;
import java.util.List;
import org.junit.Test;

public class IndexingConnectionProviderTest {

  @Test
  public void testInetSocketAddressToString() {

    // Define input
    final String ipAddress = "8.8.8.8";
    final String domainName = "europeana.eu";
    final int port1 = 1234;
    final int port2 = 1234;
    final String chroot = "/root";

    // Test single domain name
    final InetSocketAddress domainInput = new InetSocketAddress(domainName, port1);
    final String domainOutput = domainName + ":" + port1;
    assertEquals(domainOutput, IndexingConnectionProvider.toZookeeperAddressString(domainInput));

    // Test single ip address name
    final InetSocketAddress ipInput = new InetSocketAddress(ipAddress, port2);
    final String ipOutput = ipAddress + ":" + port2;
    assertEquals(ipOutput, IndexingConnectionProvider.toZookeeperAddressString(ipInput));

    // Test combination without chroot
    final List<InetSocketAddress> combinationInput1 = Arrays.asList(ipInput, domainInput);
    final String combinationOutput1 = ipOutput + "," + domainOutput;
    assertEquals(combinationOutput1,
        IndexingConnectionProvider.toZookeeperAddressString(combinationInput1, null));

    // Test combination with chroot
    final List<InetSocketAddress> combinationInput2 = Arrays.asList(domainInput, ipInput);
    final String combinationOutput2 = domainOutput + "," + ipOutput + chroot;
    assertEquals(combinationOutput2,
        IndexingConnectionProvider.toZookeeperAddressString(combinationInput2, chroot));
  }

}
