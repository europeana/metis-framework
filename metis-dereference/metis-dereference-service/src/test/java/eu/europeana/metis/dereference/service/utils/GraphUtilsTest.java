package eu.europeana.metis.dereference.service.utils;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;
import org.junit.Test;

public class GraphUtilsTest {

  /**
   * Value resolver for {@link GraphUtilsTest#testBreadthFirstSearch()}. Only return values for a
   * non-negative exponent. This simulates that a value is not always available for a given key.
   */
  private static class ValueResolver implements Function<Integer, Long> {

    @Override
    public Long apply(Integer exponent) {
      if (exponent < 0) {
        return null;
      } else {
        return Math.round(Math.pow(2, exponent));
      }
    }
  }

  /**
   * Neighbor extractor for {@link GraphUtilsTest#testBreadthFirstSearch()}. We don't check for
   * negative exponents here, so we can trigger a null result in the value resolver.
   */
  private static class NeighborExtractor implements BiConsumer<Long, Set<Integer>> {

    @Override
    public void accept(Long value, Set<Integer> neighbors) {
      final int exponent = (int) Math.round(Math.log(value) / Math.log(2));
      neighbors.add(exponent - 1);
      neighbors.add(exponent + 1);
    }
  }


  /**
   * This method tests breath first search by searching for all integer powers of 2 that are at most
   * 5 multiplications by 2 away from 2^3 (either multiplying or dividing by 2). We expect 2^0, 2^1,
   * ..., 2^8 as result. We will regard the exponent as key and the power as value.
   */
  @Test
  public void testBreadthFirstSearch() {

    // Spies of the functionality hooks. We spy because we want to make sure that the breadth first
    // search does not revisit nodes.
    final Function<Integer, Long> valueResolver = spy(new ValueResolver());
    final BiConsumer<Long, Set<Integer>> neighborExtractor = spy(new NeighborExtractor());

    // Perform the functionality and check result.
    final Collection<Long> resultCollection =
        GraphUtils.breadthFirstSearch(3, 8L, 5, valueResolver, neighborExtractor);
    assertNotNull(resultCollection);
    assertEquals(9, resultCollection.size());

    // Check the result values
    final Set<Long> result = new HashSet<>(resultCollection);
    for (int exponent = 0; exponent < 9; exponent++) {
      assertTrue(result.contains(Math.round(Math.pow(2, exponent))));
    }

    // Check the resolve calls: 9 resolves: the source is not resolved, but -1 is.
    verify(valueResolver, times(9)).apply(anyInt());
    for (int exponent = -1; exponent < 9; exponent++) {
      if (exponent != 3) {
        verify(valueResolver, times(1)).apply(exponent);
      }
    }

    // Check the resolve calls: 8 resolves (all except 2^9 as we reached the distance limit there)
    verify(neighborExtractor, times(8)).accept(anyLong(), any());
    for (int exponent = 0; exponent < 8; exponent++) {
      final Long value = Math.round(Math.pow(2, exponent));
      verify(neighborExtractor, times(1)).accept(eq(value), any());
    }
  }
}
