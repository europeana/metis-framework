package eu.europeana.metis.mediaprocessing;

/**
 * This class wraps an element and makes it available in a thread-safe way (by synchronizing
 * access). It also performs lazy creation: the object is assumed to be 'expensive' to create and
 * this will only be done if it is needed.
 *
 * @param <T> The type of the object.
 * @param <E> The type of the exception that may be thrown during creation and/or processing.
 */
abstract class AbstractThreadSafeWrapper<T, E extends Exception> {

  private final ThrowingSupplier<T, E> objectCreator;
  private T wrappedObject;

  /**
   * Constructor.
   *
   * @param objectCreator The supplier of the object.
   */
  protected AbstractThreadSafeWrapper(ThrowingSupplier<T, E> objectCreator) {
    this.objectCreator = objectCreator;
  }

  /**
   * Provides access to the object.
   *
   * @param processor The operation that needs to be executed on the object.
   * @param <O> The output/result type of the operation.
   * @return The output/result of the operation.
   * @throws E In case there was a problem.
   */
  protected <O> O process(ThrowingFunction<T, O, E> processor) throws E {
    synchronized (this) {
      if (wrappedObject == null) {
        wrappedObject = objectCreator.get();
      }
      return processor.apply(wrappedObject);
    }
  }

  @FunctionalInterface
  interface ThrowingSupplier<O, E extends Exception> {

    /**
     * Supply the value.
     * @return The value.
     * @throws E In case something went wrong supplying the value.
     */
    O get() throws E;
  }

  @FunctionalInterface
  interface ThrowingFunction<I, O, E extends Exception> {

    /**
     * Apply the function on the input.
     * @param input The input.
     * @return The result.
     * @throws E In case something went wrong applying the function to the input.
     */
    O apply(I input) throws E;
  }
}
