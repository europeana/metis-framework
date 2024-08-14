package eu.europeana.metis.utils;

import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performAction;
import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performThrowingAction;
import static eu.europeana.metis.utils.SonarqubeNullcheckAvoidanceUtils.performThrowingFunction;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link SonarqubeNullcheckAvoidanceUtils}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class SonarqubeNullcheckAvoidanceUtilsTest {

  private List<Integer> integerList = new ArrayList<>();

  @BeforeEach
  void setup() {
    integerList.clear();
  }

  @Test
  void testPerformAction() {
    Integer number = 12;

    performAction(number, x -> integerList.add(x));

    assertEquals(1, integerList.size());
  }

  @Test
  void testPerformThrowingAction() {
    Integer number = 15;

    assertThrows(ArithmeticException.class, () -> {
      performThrowingAction(number, x -> {
        integerList.add(x + 1);
        integerList.add(x / 0);
      });
    });
    assertEquals(1, integerList.size());
  }

  @Test
  void testPerformThrowingFunction() {
    Integer number = 12;

    assertThrows(ArithmeticException.class, () -> {
      Integer value = performThrowingFunction(number, x -> {
            integerList.add(x * 2);
            integerList.add(x / 0);
            return integerList.size();
          }
      );
    });
  }

  @Test
  void testPerformThrowingFunctionReturn() {
    Integer number = 12;

    Integer actualValue = performThrowingFunction(number, x -> {
          integerList.add(x * 2);
          return integerList.getFirst();
        }
    );
    assertEquals(24, actualValue);
  }
}