package eu.europeana.metis.mediaprocessing;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.exception.RdfConverterException;
import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdfImpl;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfWrapper;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * This object implements RDF deserialization functionality.
 */
class RdfDeserializerImpl implements RdfDeserializer {

  private final IUnmarshallingContext context;

  /**
   * Constructor.
   *
   * @throws RdfConverterException In case something went wrong constructing this object.
   */
  RdfDeserializerImpl() throws RdfConverterException {
    try {
      context = RdfBindingFactoryProvider.getBindingFactory().createUnmarshallingContext();
    } catch (JiBXException e) {
      throw new RdfConverterException("Problem creating deserializer.", e);
    }
  }

  @Override
  public List<RdfResourceEntry> getResourceEntriesForMediaExtraction(byte[] input)
      throws RdfDeserializationException {
    return performDeserialization(input, this::getResourceEntriesForMediaExtraction);
  }

  @Override
  public List<RdfResourceEntry> getResourceEntriesForMediaExtraction(InputStream inputStream)
      throws RdfDeserializationException {
    return convertToResourceEntries(
        getResourceEntries(inputStream, UrlType.URL_TYPES_FOR_MEDIA_EXTRACTION));
  }

  @Override
  public List<String> getResourceEntriesForLinkChecking(byte[] input)
      throws RdfDeserializationException {
    return performDeserialization(input, this::getResourceEntriesForLinkChecking);
  }

  @Override
  public List<String> getResourceEntriesForLinkChecking(InputStream inputStream)
      throws RdfDeserializationException {
    return new ArrayList<>(
        getResourceEntries(inputStream, UrlType.URL_TYPES_FOR_LINK_CHECKING).keySet());
  }

  @Override
  public EnrichedRdf getRdfForResourceEnriching(byte[] input) throws RdfDeserializationException {
    return performDeserialization(input, this::getRdfForResourceEnriching);
  }

  @Override
  public EnrichedRdf getRdfForResourceEnriching(InputStream inputStream)
      throws RdfDeserializationException {
    return new EnrichedRdfImpl(deserialize(inputStream));
  }

  Map<String, List<UrlType>> getResourceEntries(InputStream inputStream,
      Set<UrlType> allowedUrlTypes) throws RdfDeserializationException {
    return new RdfWrapper(deserialize(inputStream)).getResourceUrls(allowedUrlTypes);
  }

  private static List<RdfResourceEntry> convertToResourceEntries(
      Map<String, List<UrlType>> urlWithTypes) {
    return urlWithTypes.entrySet().stream()
        .map(entry -> new RdfResourceEntry(entry.getKey(), entry.getValue()))
        .collect(Collectors.toList());
  }

  private static <R> R performDeserialization(byte[] input, DeserializationOperation<R> operation)
      throws RdfDeserializationException {
    try (InputStream inputStream = new ByteArrayInputStream(input)) {
      return operation.deserialize(inputStream);
    } catch (IOException e) {
      throw new RdfDeserializationException("Problem with deserializing RDF.", e);
    }
  }

  @FunctionalInterface
  private interface DeserializationOperation<R> {

    R deserialize(InputStream inputStream) throws RdfDeserializationException;
  }

  private RDF deserialize(InputStream inputStream) throws RdfDeserializationException {
    try {
      return (RDF) context.unmarshalDocument(inputStream, "UTF-8");
    } catch (JiBXException e) {
      throw new RdfDeserializationException("Problem with deserializing RDF.", e);
    }
  }
}
