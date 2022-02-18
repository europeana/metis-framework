package eu.europeana.metis.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import javax.net.ServerSocketFactory;
import javax.net.ssl.SSLServerSocketFactory;

/**
 * Network utility class.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-02-24
 */
public class NetworkUtil {

  private static final int BACKLOG = 100;

  /**
   * This method can be used in JUnit tests to get a random available port on localhost to run a service. It should not be used
   * for normal operation, otherwise ssl checks should be followed to avoid man-in-the-middle attacks.
   *
   * @return the available port number
   * @throws IOException if the specified localhost is not available
   */
  public int getAvailableLocalPort() throws IOException {
    ServerSocket s = getServerSocketFactory().createServerSocket(0, BACKLOG, InetAddress.getByName("localhost"));
    int localPort = s.getLocalPort();
    s.close();
    return localPort;
  }

  /**
   * Get a server socket factory.
   *
   * @return the server socket factory
   */
  ServerSocketFactory getServerSocketFactory() {
    return SSLServerSocketFactory.getDefault();
  }
}
