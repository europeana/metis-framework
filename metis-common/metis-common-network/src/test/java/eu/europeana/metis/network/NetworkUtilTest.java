package eu.europeana.metis.network;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mockStatic;

import java.io.IOException;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

/**
 * Unit test for {@link NetworkUtil}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class NetworkUtilTest {

  @Test
  void getAvailableLocalPort() throws IOException {
    int availableLocalPort = NetworkUtil.getAvailableLocalPort();

    assertTrue(availableLocalPort > 0);
  }

  @Test
  void getAvailableLocalPortWithException() {
    try (MockedStatic<NetworkUtil> networkUtilMockedStatic = mockStatic(NetworkUtil.class)) {
      networkUtilMockedStatic.when(NetworkUtil::getAvailableLocalPort).thenThrow(IOException.class);
      assertThrows(IOException.class, () -> {
        NetworkUtil.getAvailableLocalPort();
      });
    }
  }
}