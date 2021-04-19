package eu.europeana.metis.utils;

import java.util.function.Consumer;
import java.util.function.Function;

/**
 * This class provides functionality to avoid SonarQube <code>findbugs:RCN_REDUNDANT_NULLCHECK_WOULD_HAVE_BEEN_A_NPE</code>
 * issues. There is a bug that causes a lot of false positives (see https://github.com/spotbugs/spotbugs/issues/756),
 * presumably this will be fixed at some point.
 *
 * In the mean time, this class can be used to perform actions on the objects that would otherwise
 * cause this issue.
 *
 * TODO JV Check on this issue and make sure to deprecate this class when the issue is fixed.
 */
public class SonarqubeNullcheckAvoidanceUtils {

  private SonarqubeNullcheckAvoidanceUtils() {
  }

  /**
   * Perform an action on an object.S
   *
   * @param object The object.
   * @param action The action.
   * @param <I> The type of the object.
   */
  public static <I> void performAction(I object, Consumer<I> action) {
    action.accept(object);
  }

  /**
   * Perform an action on an object that can throw an exception.
   *
   * @param object The object.
   * @param action The action.
   * @param <I> The type of the object.
   * @param <E> The type of the exception.
   * @throws E In case the action throws an exception.
   */
  public static <I, E extends Exception> void performThrowingAction(I object,
          ThrowingConsumer<I, E> action) throws E {
    action.accept(object);
  }

  /**
   * Perform a function on an object.
   *
   * @param object The object.
   * @param function The function.
   * @param <I> The type of the object.
   * @param <O> The type of the result.
   * @return The result.
   */
  public static <I, O> O performFunction(I object, Function<I, O> function) {
    return function.apply(object);
  }

  /**
   * Perform a function on an object that can throw an exception.
   *
   * @param object The object.
   * @param function The function.
   * @param <I> The type of the object.
   * @param <O> The type of the result.
   * @param <E> The type of the exception.
   * @return The result.
   */
  public static <I, O, E extends Exception> O performThrowingFunction(I object,
          ThrowingFunction<I, O, E> function) throws E {
    return function.apply(object);
  }

  /**
   * Represents an action that can throw an exception.
   *
   * @param <I> The type of the input object.
   * @param <E> The type of the exception.
   */
  public interface ThrowingConsumer<I, E extends Exception> {

    /**
     * Perform the action.
     *
     * @param input The input.
     * @throws E The exception.
     */
    void accept(I input) throws E;
  }

  /**
   * Represents an action that can throw an exception.
   *
   * @param <I> The type of the input object.
   * @param <O> The type of the output result.
   * @param <E> The type of the exception.
   */
  public interface ThrowingFunction<I, O, E extends Exception> {

    /**
     * Perform the action.
     *
     * @param input The input.
     * @return The output.
     * @throws E The exception.
     */
    O apply(I input) throws E;
  }
}
