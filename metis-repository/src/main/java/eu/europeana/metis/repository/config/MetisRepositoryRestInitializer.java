package eu.europeana.metis.repository.config;

import java.io.File;
import javax.servlet.MultipartConfigElement;
import javax.servlet.ServletRegistration.Dynamic;
import org.springframework.web.servlet.support.AbstractAnnotationConfigDispatcherServletInitializer;

/**
 * This class is the bootstrap code for Spring. It tells Spring how to start this web application.
 */
public class MetisRepositoryRestInitializer extends
        AbstractAnnotationConfigDispatcherServletInitializer {

  private static final int MAX_UPLOAD_SIZE_IN_MB = 5 * 1024 * 1024; // 5 MB

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
