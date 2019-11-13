package eu.europeana.validation.service;

import java.io.File;
import java.io.IOException;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Class enabling classpath XSD reading for split XSDs. This is because of an issue with JAXP XSD
 * loading. When initializing the {@link #setPrefix(String)} the value should be sanitized otherwise
 * the use of this class can became unsecure.
 */
public class ClasspathResourceResolver implements LSResourceResolver {

  private String prefix;
  private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathResourceResolver.class);

  @Override
  public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId,
      String baseURI) {
    try {
      //Read file from system
      String fullPath = new File(prefix, systemId).getAbsolutePath();
      final byte[] bytes = Files.readAllBytes(Paths.get(fullPath));
      final String fileContent = new String(bytes, StandardCharsets.UTF_8.name());
      final StringReader stringReader = new StringReader(fileContent);

      LSInput input = new ClasspathLSInput();
      input.setPublicId(publicId);
      input.setSystemId(systemId);
      input.setBaseURI(baseURI);
      input.setCharacterStream(stringReader);
      return input;
    } catch (IOException e) {
      LOGGER.error("An error occurred while resolving a resource", e);
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

