package eu.europeana.metis.mediaprocessing.temp;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.UrlType;
import eu.europeana.metis.mediaprocessing.exception.MediaException;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntryImpl;
import eu.europeana.metis.mediaservice.EdmObject;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

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
  public List<RdfResourceEntry> getResourceEntriesForMetadataExtraction(RDF rdf) {
    return getResourceEntries(rdf, UrlType.URL_TYPES_FOR_METADATA_EXTRACTION);
  }

  // This method is thread-safe.
  public List<RdfResourceEntry> getResourceEntriesForLinkChecking(RDF rdf) {
    return getResourceEntries(rdf, UrlType.URL_TYPES_FOR_LINK_CHECKING);
  }

  private List<RdfResourceEntry> getResourceEntries(RDF rdf, Collection<UrlType> allowedUrlTypes) {
    return new EdmObject(rdf).getResourceUrls(allowedUrlTypes).entrySet().stream()
        .map(entry -> new RdfResourceEntryImpl(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }
}
