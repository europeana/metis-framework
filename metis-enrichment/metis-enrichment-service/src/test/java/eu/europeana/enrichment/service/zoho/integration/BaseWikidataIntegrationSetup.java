package eu.europeana.enrichment.service.zoho.integration;

import java.io.File;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import eu.europeana.enrichment.service.zoho.BaseZohoAccessSetup;

public abstract class BaseWikidataIntegrationSetup extends BaseZohoAccessSetup {

  /**
   * This method returns the classpath file for the give path name
   * 
   * @param fileName the name of the file to be searched in the classpath
   * @return the File object
   * @throws URISyntaxException
   */
  protected File getClasspathFile(String fileName) throws URISyntaxException {
    URL resource = getClass().getResource(fileName);
    if (resource == null)
      return null;
    URI fileLocation = resource.toURI();
    return (new File(fileLocation));
  }
}
