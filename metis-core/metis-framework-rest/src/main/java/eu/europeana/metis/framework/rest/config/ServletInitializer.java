/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.metis.framework.rest.config;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import org.springframework.util.ClassUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.support.AbstractDispatcherServletInitializer;

/**
 * Servlet initializer
 * Created by gmamakis on 12-2-16.
 */
public class ServletInitializer extends AbstractDispatcherServletInitializer {
  //Set profile for choosing between cached or live CRM Zoho
  private static final String PROFILEPATH = "profile.properties";

  @Override
  protected WebApplicationContext createServletApplicationContext() {
    Properties prop = new Properties();
    InputStream input = null;
    try {
      input = ServletInitializer.class.getClassLoader().getResourceAsStream(PROFILEPATH);
      prop.load(input);
      AnnotationConfigWebApplicationContext context = new AnnotationConfigWebApplicationContext();
      context.scan(ClassUtils.getPackageName(getClass()));
      context.getEnvironment().setActiveProfiles(prop.getProperty("profile"));
      return context;
    } catch (IOException ex) {
      ex.printStackTrace();
    } finally {
      if (input != null) {
        try {
          input.close();
        } catch (IOException e) {
          e.printStackTrace();
        }
      }
    }
    return null;
  }

  @Override
  protected String[] getServletMappings() {
    return new String[]{"/"};
  }

  @Override
  protected WebApplicationContext createRootApplicationContext() {
    return null;
  }

}
