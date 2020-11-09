package eu.europeana.metis.mongo.utils;

import dev.morphia.aggregation.experimental.Aggregation;
import dev.morphia.aggregation.experimental.AggregationOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.internal.MorphiaCursor;
import eu.europeana.metis.network.ExternalRequestUtil;
import eu.europeana.metis.network.SonarqubeNullcheckAvoidanceUtils;
import java.util.List;
import java.util.Optional;
import java.util.function.BiFunction;
import java.util.function.Supplier;

/**
 * Helper for morphia operations
 */
public final class MorphiaUtils {

  private MorphiaUtils() {
    //Not to be instantiated
  }

  /**
   * Get a list out of a query.
   * <p>This method is retryable according to {@link ExternalRequestUtil#retryableExternalRequestForNetworkExceptions(Supplier)}</p>
   *
   * @param query the query to execute
   * @param <T> the type of class that the {@link Query} represents
   * @return the list with the {@link T} objects
   */
  public static <T> List<T> getListOfQueryRetryable(Query<T> query) {
    return getListOfQueryRetryable(query, null);
  }

  /**
   * Get a list out of a query.
   * <p>This method is retryable according to {@link ExternalRequestUtil#retryableExternalRequestForNetworkExceptions(Supplier)}</p>
   *
   * @param query the query to execute
   * @param findOptions the options for the query, can be null
   * @param <T> the type of class that the {@link Query} represents
   * @return the list with the {@link T} objects
   */
  public static <T> List<T> getListOfQueryRetryable(Query<T> query, FindOptions findOptions) {
    final BiFunction<Query<T>, FindOptions, MorphiaCursor<T>> queryFunction = getMorphiaCursorFromQuery();
    return ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(() -> {
      try (MorphiaCursor<T> cursor = queryFunction.apply(query, findOptions)) {
        return SonarqubeNullcheckAvoidanceUtils.performFunction(cursor, MorphiaCursor::toList);
      }
    });
  }

  /**
   * Get a list out of an {@link Aggregation}.
   * <p>This method is retryable according to {@link ExternalRequestUtil#retryableExternalRequestForNetworkExceptions(Supplier)}</p>
   *
   * @param aggregation the query to execute
   * @param resultObjectClass the class object representing the {@link R} type
   * @param <T> the type of class that the {@link Aggregation} represents
   * @param <R> the type of class that the result of the {@link Aggregation} represents
   * @return the list with the {@link R} objects
   */
  public static <T, R> List<R> getListOfAggregationRetryable(Aggregation<T> aggregation,
      Class<R> resultObjectClass) {
    return getListOfAggregationRetryable(aggregation, resultObjectClass, null);
  }

  /**
   * Get a list out of an {@link Aggregation}.
   * <p>This method is retryable according to {@link ExternalRequestUtil#retryableExternalRequestForNetworkExceptions(Supplier)}</p>
   *
   * @param aggregation the query to execute
   * @param resultObjectClass the class object representing the {@link R} type
   * @param aggregationOptions the options for the aggregation, can be null
   * @param <T> the type of class that the {@link Aggregation} represents
   * @param <R> the type of class that the result of the {@link Aggregation} represents
   * @return the list with the {@link R} objects
   */
  public static <T, R> List<R> getListOfAggregationRetryable(Aggregation<T> aggregation,
      Class<R> resultObjectClass, AggregationOptions aggregationOptions) {
    final BiFunction<Aggregation<T>, AggregationOptions, MorphiaCursor<R>> aggregationFunction = getMorphiaCursorFromAggregation(
        resultObjectClass);
    return ExternalRequestUtil.retryableExternalRequestForNetworkExceptions(() -> {
      try (MorphiaCursor<R> cursor = aggregationFunction.apply(aggregation, aggregationOptions)) {
        return SonarqubeNullcheckAvoidanceUtils.performFunction(cursor, MorphiaCursor::toList);
      }
    });
  }

  /**
   * Creates the {@link MorphiaCursor} based on the {@link FindOptions} supplied if present, can be
   * null.
   * <p>Make sure that the returned {@link MorphiaCursor} is properly <b>CLOSED</b> after its
   * use.</p>
   *
   * @param <T> the type of class that the {@link Query} represents
   * @return the morphia cursor
   */
  @SuppressWarnings("resource")
  private static <T> BiFunction<Query<T>, FindOptions, MorphiaCursor<T>> getMorphiaCursorFromQuery() {
    return (querySupplied, findOptionsSupplied) -> Optional.ofNullable(findOptionsSupplied)
        .map(querySupplied::iterator).orElseGet(querySupplied::iterator);
  }

  /**
   * Creates the {@link MorphiaCursor} based on the {@link AggregationOptions} supplied. Can be
   * null.
   * <p>Make sure that the returned {@link MorphiaCursor} is properly <b>CLOSED</b> after its
   * use.</p>
   *
   * @param resultObjectClass the class object representing the {@link R} type
   * @param <T> the type of class that the {@link Aggregation} represents
   * @param <R> the type of class that the result of the {@link Aggregation} represents
   * @return the morphia cursor
   */
  @SuppressWarnings("resource")
  private static <T, R> BiFunction<Aggregation<T>, AggregationOptions, MorphiaCursor<R>> getMorphiaCursorFromAggregation(
      Class<R> resultObjectClass) {
    return (aggregationSupplied, aggregationOptionsSupplied) -> Optional
        .ofNullable(aggregationOptionsSupplied)
        .map(aggregationOptions -> aggregationSupplied
            .execute(resultObjectClass, aggregationOptions))
        .orElseGet(() -> aggregationSupplied.execute(resultObjectClass));
  }
}
