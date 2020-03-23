package eu.europeana.metis.utils;

import com.mongodb.ServerAddress;
import java.net.InetSocketAddress;
import java.util.List;
import java.util.function.Function;
import java.util.function.IntUnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

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
    final IntUnaryOperator portGetter;
    if (ports.length == 1) {
      portGetter = index -> ports[0];
    } else if (hosts.length == ports.length) {
      portGetter = index -> ports[index];
    } else {
      throw exceptionCreator.apply("The port array length does not match the host array length.");
    }

    // Compile the server address list
    return IntStream.range(0, hosts.length)
            .mapToObj(index -> new InetSocketAddress(hosts[index], portGetter.applyAsInt(index)))
            .collect(Collectors.toList());
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
