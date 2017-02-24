package eu.europeana.metis.utils;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2017-02-24
 */
public class NetworkUtil {
  public static int getAvailableLocalPort() throws IOException {
    ServerSocket s = new ServerSocket(0);
    int localPort = s.getLocalPort();
    s.close();
    return localPort;
  }
}
