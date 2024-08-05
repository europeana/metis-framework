package eu.europeana.metis.solr.connection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SolrProperties}
 *
 * @author Jorge Ortiz
 * @since 03-02-2022
 */
class SolrPropertiesTest {

  private SolrProperties<Exception> solrProperties;

  @BeforeEach
  void setup() {
    solrProperties = new SolrProperties<>(Exception::new);
  }

  @AfterEach
  void teardown() {
    solrProperties = null;
  }

  @Test
  void setZookeeperHosts() throws Exception {
    final String[] zooKeeperHosts = new String[]{"localhost1", "localhost2", "localhost3"};
    final int[] zooKeeperPorts = new int[]{2181, 2182, 2183};

    solrProperties.setZookeeperHosts(zooKeeperHosts, zooKeeperPorts);

    final List<InetSocketAddress> actualZookeeperHosts = solrProperties.getZookeeperHosts();
    assertEquals(getExpectedZookeeperHosts(), actualZookeeperHosts);
  }

  private List<InetSocketAddress> getExpectedZookeeperHosts() {
    return List.of(new InetSocketAddress("localhost1", 2181),
        new InetSocketAddress("localhost2", 2182),
        new InetSocketAddress("localhost3", 2183));
  }

  @Test
  void addZookeeperHost() throws Exception {
    solrProperties.addZookeeperHost(new InetSocketAddress("192.168.172.2", 2181));

    final List<InetSocketAddress> actualZookeeperHosts = solrProperties.getZookeeperHosts();
    assertEquals(1, actualZookeeperHosts.size());
  }

  @Test
  void addZookeeperHostException() {
    final Exception actualException = assertThrows(Exception.class, () -> solrProperties.addZookeeperHost(null));

    assertEquals("Value 'host' cannot be null.", actualException.getMessage());
  }

  @Test
  void setZookeeperChroot() throws Exception {
    solrProperties.addZookeeperHost(new InetSocketAddress("192.168.172.2", 2181));

    solrProperties.setZookeeperChroot("/zookeeper");

    assertEquals("/zookeeper", solrProperties.getZookeeperChroot());
  }

  @Test
  void setZookeeperChrootNull() throws Exception {
    solrProperties.addZookeeperHost(new InetSocketAddress("192.168.172.2", 2181));

    solrProperties.setZookeeperChroot("");

    assertNull(solrProperties.getZookeeperChroot());
  }

  @Test
  void setZookeeperChrootException() throws Exception {
    solrProperties.addZookeeperHost(new InetSocketAddress("192.168.172.2", 2181));

    final Exception expectedException = assertThrows(Exception.class, () -> solrProperties.setZookeeperChroot("root"));

    assertEquals("A chroot, if provided, must start with '/'.", expectedException.getMessage());
  }

  @Test
  void setZookeeperDefaultCollection() throws Exception {
    solrProperties.addZookeeperHost(new InetSocketAddress("192.168.172.2", 2181));

    solrProperties.setZookeeperDefaultCollection("zookeeperCollection");

    assertEquals("zookeeperCollection", solrProperties.getZookeeperDefaultCollection());
  }

  @Test
  void setZookeeperDefaultCollectionNull() throws Exception {
    solrProperties.addZookeeperHost(new InetSocketAddress("192.168.172.2", 2181));

    final Exception expectedException = assertThrows(Exception.class, () -> solrProperties.setZookeeperDefaultCollection(null));

    assertEquals("Value 'zookeeperDefaultCollection' cannot be null.", expectedException.getMessage());
  }

  @Test
  void setZookeeperTimeoutInSecs() throws Exception {
    solrProperties.addZookeeperHost(new InetSocketAddress("192.168.172.2", 2181));

    solrProperties.setZookeeperTimeoutInSecs(10);

    assertEquals(10, solrProperties.getZookeeperTimeoutInSecs());
  }

  @Test
  void setZookeeperTimeoutNegative() throws Exception {
    solrProperties.addZookeeperHost(new InetSocketAddress("192.168.172.2", 2181));

    solrProperties.setZookeeperTimeoutInSecs(-1);

    assertNull(solrProperties.getZookeeperTimeoutInSecs());
  }

  @Test
  void addSolrHost() throws Exception {
    solrProperties.addSolrHost(new URI("http://localhost:8983/solr"));

    List<URI> actualSolrHosts = solrProperties.getSolrHosts();

    assertEquals(1, actualSolrHosts.size());
    assertEquals(new URI("http://localhost:8983/solr"), actualSolrHosts.getFirst());
  }

  @Test
  void hasZookeeperConnection() {
    assertFalse(solrProperties.hasZookeeperConnection());
  }
}