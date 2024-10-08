package eu.europeana.metis.mediaprocessing.utils;


import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdfImpl;
import eu.europeana.metis.schema.convert.RdfDeserializer;
import eu.europeana.metis.schema.convert.RdfSerializer;
import eu.europeana.metis.schema.convert.model.RdfDeserializationException;
import eu.europeana.metis.schema.convert.model.RdfResourceEntry;
import eu.europeana.metis.schema.convert.model.RdfSerializationException;
import eu.europeana.metis.schema.convert.model.ResourceInfo;
import eu.europeana.metis.schema.convert.model.UrlType;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import org.w3c.dom.Document;

public class ConverterUtils {

  private final RdfDeserializer rdfDeserializer = new RdfDeserializer();
  private final RdfSerializer rdfSerializer = new RdfSerializer();

  public EnrichedRdf getRdfForResourceEnriching(byte[] input) throws RdfDeserializationException {
    return rdfDeserializer.performDeserialization(input, this::getRdfForResourceEnriching);
  }

  public EnrichedRdf getRdfForResourceEnriching(InputStream inputStream)
      throws RdfDeserializationException {
    return new EnrichedRdfImpl(rdfDeserializer.deserialize(inputStream));
  }

  public List<RdfResourceEntry> getRemainingResourcesForMediaExtraction(byte[] input)
      throws RdfDeserializationException {
    return rdfDeserializer.performDeserialization(input, this::getRemainingResourcesForMediaExtraction);
  }

  public List<RdfResourceEntry> getRemainingResourcesForMediaExtraction(InputStream inputStream)
      throws RdfDeserializationException {

    // Get all the resource entries.
    final Document deserializedDocument = rdfDeserializer.deserializeToDocument(inputStream);
    final Map<String, ResourceInfo> allResources = rdfDeserializer.getResourceEntries(deserializedDocument,
        UrlType.URL_TYPES_FOR_MEDIA_EXTRACTION);

    // Find the main thumbnail resource if it exists and remove it from the result.
    rdfDeserializer.getMainThumbnailResource(deserializedDocument).map(RdfResourceEntry::getResourceUrl)
                   .ifPresent(allResources::remove);

    // Done.
    return rdfDeserializer.convertToResourceEntries(allResources);
  }

  public List<RdfResourceEntry> getResourceEntriesForLinkChecking(byte[] input)
      throws RdfDeserializationException {
    return rdfDeserializer.performDeserialization(input, this::getResourceEntriesForLinkChecking);
  }

  public List<RdfResourceEntry> getResourceEntriesForLinkChecking(InputStream inputStream) throws RdfDeserializationException {
    return rdfDeserializer.convertToResourceEntries(
        rdfDeserializer.getResourceEntries(rdfDeserializer.deserializeToDocument(inputStream),
            UrlType.URL_TYPES_FOR_LINK_CHECKING));
  }

  public byte[] enrichedRdfToBytes(EnrichedRdf enrichedRdf) throws RdfSerializationException {
    return rdfSerializer.convertRdfToBytes(enrichedRdf.finalizeRdf());
  }

}
