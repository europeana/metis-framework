package eu.europeana.enrichment.utils;

import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import java.io.StringReader;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;

/**
 * This class contains conversion tools for the {@link EnrichmentBase} class.
 */
public final class EnrichmentBaseConverter {

  private EnrichmentBaseConverter() {
    // This class should not be instantiated.
  }

  /**
   * Parse the XML string to convert it to an instance of (one of the subclasses of) {@link
   * EnrichmentBase}.
   *
   * @param entityXml The XML string.
   * @return The entity.
   * @throws JAXBException In case there was an issue parsing the XML.
   */
  public static EnrichmentBase convertToEnrichmentBase(String entityXml) throws JAXBException {
    final StringReader reader = new StringReader(entityXml);
    final JAXBContext context = JAXBContext.newInstance(EnrichmentBase.class);
    return (EnrichmentBase) context.createUnmarshaller().unmarshal(reader);
  }
}
