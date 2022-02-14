package eu.europeana.metis.network;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link InetAddressUtil}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class InetAddressUtilTest {

  @Test
  void getAddressesFromHostsAndPorts() {
    final InetAddressUtil<IllegalArgumentException> inetAddressUtil = new InetAddressUtil<>(IllegalArgumentException::new);
    final List<InetSocketAddress> expectedList = new ArrayList<>();
    expectedList.add(new InetSocketAddress("localhost", 8888));
    expectedList.add(new InetSocketAddress("192.168.1.1", 8080));

    final List<InetSocketAddress> actualList = inetAddressUtil.getAddressesFromHostsAndPorts(
        new String[]{"localhost", "192.168.1.1"},
        new int[]{8888, 8080});

    assertEquals(expectedList, actualList);
  }
}