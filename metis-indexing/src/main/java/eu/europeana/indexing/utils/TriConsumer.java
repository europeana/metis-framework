package eu.europeana.indexing.utils;

/**
 * An operation that accepts three input arguments and returns no result.
 *
 * @param <K> type of the first argument
 * @param <V> type of the second argument
 * @param <S> type of the third argument
 */
public interface TriConsumer<K, V, S> {

  /**
   * Performs the operation given the specified arguments.
   * 
   * @param k the first input argument
   * @param v the second input argument
   * @param s the third input argument
   */
  void accept(K k, V v, S s);
}
