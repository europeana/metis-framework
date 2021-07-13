package eu.europeana.metis.repository.config;

import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * This class is the bootstrap code for Spring. It tells Spring how to start this web application.
 */
public class MetisRepositoryRestInitializer extends
        AbstractAnnotationConfigDispatcherServletInitializer {

  @Override
  protected Class<?>[] getRootConfigClasses() {
    return new Class<?>[0];
  }

  @Override
  protected Class<?>[] getServletConfigClasses() {
    return new Class[]{MetisRepositoryRestApplication.class};
  }

  @Override
  protected String[] getServletMappings() {
    return new String[]{"/"};
  }
}
