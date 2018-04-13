package eu.europeana.metis.dereference.service.utils;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Function;

/**
 * This class provides graph utils.
 * 
 * @author jochen
 *
 */
public final class GraphUtils {

  private GraphUtils() {}

  /**
   * This utility method performs breadth-first search through a (directed) graph to search all
   * nodes that are within a certain distance of the source node. Each node consists of a key (that
   * can be used as a key in the {@link Map} interface) and a value. And each node contains
   * references to its neighbors (outgoing edges).
   * 
   * @param sourceNodeKey The key of the source node.
   * @param sourceNodeValue The value of the source node.
   * @param maxDistance The maximum distance that we search. If this is less or equal to 0, only the
   *        source node will be returned.
   * @param valueResolver A function that obtains a value for a given key. The function could return
   *        null.
   * @param neighborExtractor A function that accepts two parameters: a value and a set of keys. The
   *        function extracts all neighbors (keys) from the provided value and adds them to the set
   *        of keys.
   * @param <K> The type of the node keys.
   * @param <V> The type of the node values.
   * @return The values that are within the specified distance from the source node.
   */
  public static <K, V> Collection<V> breadthFirstSearch(K sourceNodeKey, V sourceNodeValue,
      int maxDistance, Function<K, V> valueResolver, BiConsumer<V, Set<K>> neighborExtractor) {

    // The map keeps track of all nodes that we have already seen, to avoid loops.
    final Map<K, V> allFoundNodes = new HashMap<>();
    allFoundNodes.put(sourceNodeKey, sourceNodeValue);

    // The set contains the nodes that were added in the last iteration (i.e. the nodes we are going
    // to visit). Visiting means getting and resolving its outgoing edges.
    final Set<K> nodesFoundDuringLastIteration = new HashSet<>();
    nodesFoundDuringLastIteration.add(sourceNodeKey);

    // Discover new neighbors (perform steps) exactly maxDistance times.
    for (int distance = 0; distance < maxDistance; distance++) {

      // Visit the nodes discovered during the last iteration to discover its neighbors.
      final Set<K> nodesFoundDuringThisIteration = new HashSet<>();
      nodesFoundDuringLastIteration.stream().map(allFoundNodes::get)
          .forEach(value -> neighborExtractor.accept(value, nodesFoundDuringThisIteration));

      // Remove the nodes discovered during last iteration from our list.
      nodesFoundDuringLastIteration.clear();

      // Retrieve the resources and fill the list for next iteration.
      for (K nodeKey : nodesFoundDuringThisIteration) {

        // If we have already seen this node, we don't visit it again.
        if (allFoundNodes.containsKey(nodeKey)) {
          continue;
        }

        // Try to retrieve the value. Save it to the map and the list if we found something.
        final V value = valueResolver.apply(nodeKey);
        if (value != null) {
          nodesFoundDuringLastIteration.add(nodeKey);
          allFoundNodes.put(nodeKey, value);
        }
      }
    }

    // Done: the result is all discovered nodes (their values).
    return allFoundNodes.values();
  }
}
