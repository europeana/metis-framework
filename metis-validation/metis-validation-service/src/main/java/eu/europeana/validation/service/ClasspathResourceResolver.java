package eu.europeana.validation.service;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Class enabling classpath XSD reading for split XSDs. This is because of an issue with JAXP XSD
 * loading Created by ymamakis on 12/21/15.
 */
public class ClasspathResourceResolver implements LSResourceResolver {

  private String prefix;
  private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathResourceResolver.class);
  private static Map<String, InputStream> cache = new HashMap<>();

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
      String baseURI) {
    try {
      LSInput input = new ClasspathLSInput();
      InputStream stream;
      if (!systemId.startsWith("http")) {
        String fullPath = prefix + "/" + systemId;
        if (cache.get(fullPath) == null) {
          stream = Files.newInputStream(Paths.get(fullPath));
          cache.put(systemId, stream);
        } else {
          stream = cache.get(fullPath);
        }
      } else {
        if (cache.get(systemId) == null) {
          stream = Thread.currentThread().getContextClassLoader().getResourceAsStream("xml.xsd");
          cache.put(systemId, stream);
        } else {
          stream = cache.get(systemId);
        }
      }
      input.setPublicId(publicId);
      input.setSystemId(systemId);
      input.setBaseURI(baseURI);
      input.setCharacterStream(new InputStreamReader(stream, "UTF-8"));
      return input;
    } catch (java.io.IOException e) {
      LOGGER.error(e.getMessage(), e);
    }
    return null;
  }

  /**
   * @return the prefix
   */
  public String getPrefix() {
    return prefix;
  }

  /**
   * @param prefix the prefix to set
   */
  public void setPrefix(String prefix) {
    this.prefix = prefix;
  }
}

