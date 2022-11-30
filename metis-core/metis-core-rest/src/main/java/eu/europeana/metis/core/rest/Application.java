package eu.europeana.metis.core.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * The Spring boot application entry point.
 */
@SpringBootApplication
public class Application {

  /**
   * The main spring boot method.
   *
   * @param args application arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
