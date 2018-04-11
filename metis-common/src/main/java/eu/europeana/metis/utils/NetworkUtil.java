package eu.europeana.metis.utils;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-02-24
 */
public final class NetworkUtil {
  private static final int BACKLOG = 100;

  private NetworkUtil() {
  }

  public static int getAvailableLocalPort() throws IOException {
    ServerSocket s = new ServerSocket(0, BACKLOG, InetAddress.getByName("localhost"));
    int localPort = s.getLocalPort();
    s.close();
    return localPort;
  }
}
