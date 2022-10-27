package eu.europeana.metis.authentication.rest;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.support.SpringBootServletInitializer;

/**
 * The Spring boot application entry point.
 */
@SpringBootApplication
public class Application extends SpringBootServletInitializer {

  /**
   * The main spring boot method.
   *
   * @param args application arguments
   */
  public static void main(String[] args) {
    SpringApplication.run(Application.class, args);
  }
}
