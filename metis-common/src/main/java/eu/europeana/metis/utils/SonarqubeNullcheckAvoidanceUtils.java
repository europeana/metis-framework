package eu.europeana.metis.utils;

import java.util.function.Consumer;
import java.util.function.Function;

public class SonarqubeNullcheckAvoidanceUtils {

  private SonarqubeNullcheckAvoidanceUtils() {
  }

  public static <I> void performAction(I object, Consumer<I> action) {
    action.accept(object);
  }

  public static <I, E extends Exception> void performThrowingAction(I object,
          ThrowingConsumer<I, E> action) throws E {
    action.accept(object);
  }

  public static <I, O> O performFunction(I object, Function<I, O> action) {
    return action.apply(object);
  }

  public interface ThrowingConsumer<I, E extends Exception> {

    void accept(I input) throws E;
  }
}
