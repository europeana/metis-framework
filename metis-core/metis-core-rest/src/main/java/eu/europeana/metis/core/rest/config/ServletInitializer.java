package eu.europeana.metis.core.rest.config;

import java.io.File;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration.Dynamic;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

/**
 * Servlet initializer Created by gmamakis on 12-2-16.
 */
public class ServletInitializer extends AbstractDispatcherServletInitializer {

  private static final int MAX_UPLOAD_SIZE_IN_MB = 5 * 1024 * 1024; // 5 MB

  @Override
  protected WebApplicationContext createServletApplicationContext() {
    AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
    context.scan(ClassUtils.getPackageName(getClass()));
    return context;
  }

  @Override
  protected String[] getServletMappings() {
    return new String[]{"/"};
  }

  @Override
  protected WebApplicationContext createRootApplicationContext() {
    return null;
  }

  @Override
  protected void customizeRegistration(Dynamic registration) {

    // Call super method
    super.customizeRegistration(registration);

    // register a MultipartConfigElement.
    final File uploadDirectory = new File(System.getProperty("java.io.tmpdir"));
    final MultipartConfigElement multipartConfigElement = new MultipartConfigElement(
            uploadDirectory.getAbsolutePath(), MAX_UPLOAD_SIZE_IN_MB, MAX_UPLOAD_SIZE_IN_MB * 2L,
            MAX_UPLOAD_SIZE_IN_MB / 2);
    registration.setMultipartConfig(multipartConfigElement);
  }
}
