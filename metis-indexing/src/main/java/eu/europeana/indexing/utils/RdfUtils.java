package eu.europeana.indexing.utils;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.corelib.definitions.jibx.WebResourceType;
import java.util.stream.Stream;
import org.apache.commons.lang3.StringUtils;

/**
 * This class contains utililty methods regarding RDF records.
 */
public class RdfUtils {

  /**
   * Obtains the list of web resources from an RDF record. This will filter the resources: it only
   * returns those that need to be indexed.
   *
   * @param record The record from which to obtain the web resources.
   * @return The web resources that need to be filtered.
   */
  public static Stream<WebResourceType> getWebResources(RDF record) {
    if (record.getWebResourceList() == null) {
      return Stream.empty();
    }
    return record.getWebResourceList()
        .stream().filter(resource -> StringUtils.isNotBlank(resource.getAbout()));
  }
}
