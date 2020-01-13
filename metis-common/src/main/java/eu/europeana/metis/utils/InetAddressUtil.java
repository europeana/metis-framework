package eu.europeana.metis.utils;

import com.mongodb.ServerAddress;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class provides utilities concerning internet addresses and ports.
 *
 * @param <E> The type of exception thrown when the properties are not valid.
 */
public class InetAddressUtil<E extends Exception> {

  // Exception creator
  private final Function<String, E> exceptionCreator;

  /**
   * Constructor.
   *
   * @param exceptionCreator Function for creating exception from a given message.
   */
  public InetAddressUtil(Function<String, E> exceptionCreator) {
    this.exceptionCreator = exceptionCreator;
  }

  /**
   * This method converts arrays of hosts and ports to a list of Internet addresses.
   *
   * @param hosts The hosts. This cannot be null or empty.
   * @param ports The ports. This cannot be null or empty. Must contain either the same number of
   * elements as the hosts array, or exactly 1 element (which will then apply to all hosts).
   * @return The list of converted internet addresses.
   * @throws E In case either of the arrays is null, or their lengths don't match.
   */
  public List<InetSocketAddress> getAddressesFromHostsAndPorts(String[] hosts,
          int[] ports) throws E {

    // Null check.
    if (hosts == null) {
      throw exceptionCreator.apply("The host array is null.");
    }
    if (ports == null) {
      throw exceptionCreator.apply("The port array is null.");
    }

    // Check the hosts and ports input for array length.
    if (hosts.length != ports.length && ports.length != 1) {
      throw exceptionCreator.apply("The port array length does not match the host array length.");
    }

    // Compile the server address list
    final List<InetSocketAddress> addresses = new ArrayList<>(hosts.length);
    for (int i = 0; i < hosts.length; i++) {
      final int port = hosts.length == ports.length ? ports[i] : ports[0];
      addresses.add(new InetSocketAddress(hosts[i], port));
    }

    // Done
    return addresses;
  }

  /**
   * This method converts arrays of hosts and ports to a list of Mongo-style Internet addresses.
   *
   * @param hosts The hosts. This cannot be null or empty.
   * @param ports The ports. This cannot be null or empty. Must contain either the same number of
   * elements as the hosts array, or exactly 1 element (which will then apply to all hosts).
   * @return The list of converted internet addresses.
   * @throws E In case either of the arrays is null, or their lengths don't match.
   */
  public List<ServerAddress> getMongoAddressesFromHostsAndPorts(String[] hosts,
          int[] ports) throws E {
    return getAddressesFromHostsAndPorts(hosts, ports).stream().map(ServerAddress::new).collect(
            Collectors.toList());
  }
}
