package eu.europeana.metis.mediaprocessing.temp;

import java.io.InputStream;
import java.util.Set;
import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.exception.MediaException;
import eu.europeana.metis.mediaservice.EdmObject;
import eu.europeana.metis.mediaservice.UrlType;

@Deprecated
public class TemporaryMediaHandler {

  private final EdmObject.Parser parser = new EdmObject.Parser();
  private final EdmObject.Writer writer = new EdmObject.Writer();

  // This method is not thread-safe.
  // This method does not close the input stream.
  public RDF deserialize(InputStream is) throws MediaException {
    return parser.parseXml(is).getRdf();
  }

  // This method is not thread-safe.
  public byte[] serialize(RDF rdf) {
    return writer.toXmlBytes(new EdmObject(rdf));
  }

  // This method is thread-safe.
  public Set<String> getResourceUrlsForMetadataExtraction(RDF rdf) {
    return new EdmObject(rdf).getResourceUrls(UrlType.URL_TYPES_FOR_METADATA_EXTRACTION).keySet();
  }

  // This method is thread-safe.
  public Set<String> getResourceUrlsForLinkChecking(RDF rdf) {
    return new EdmObject(rdf).getResourceUrls(UrlType.URL_TYPES_FOR_LINK_CHECKING).keySet();
  }
}
