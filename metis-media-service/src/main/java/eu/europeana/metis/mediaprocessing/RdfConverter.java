package eu.europeana.metis.mediaprocessing;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.exception.RdfConverterException;
import eu.europeana.metis.mediaprocessing.exception.RdfDeserializationException;
import eu.europeana.metis.mediaprocessing.exception.RdfSerializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdfImpl;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntry;
import eu.europeana.metis.mediaprocessing.model.RdfResourceEntryImpl;
import eu.europeana.metis.mediaprocessing.model.RdfWrapper;
import eu.europeana.metis.mediaprocessing.model.UrlType;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

public class RdfConverter {

  private static IBindingFactory rdfBindingFactory;

  private static synchronized IBindingFactory getBindingFactory() throws RdfConverterException {
    if (rdfBindingFactory == null) {
      try {
        rdfBindingFactory = BindingDirectory.getFactory(RDF.class);
      } catch (JiBXException e) {
        throw new RdfConverterException("Unable to create binding factory", e);
      }
    }
    return rdfBindingFactory;
  }

  /**
   * Creates {@code EdmObject}s from xml.
   * <p>
   * It's recommended to keep an instance for reuse.
   * <p>
   * It's not thread safe.
   */
  public static class Parser implements RdfDeserializer {

    private final IUnmarshallingContext context;

    public Parser() throws RdfConverterException {
      try {
        context = getBindingFactory().createUnmarshallingContext();
      } catch (JiBXException e) {
        throw new RdfConverterException("Problem creating deserializer.", e);
      }
    }

    @Override
    public List<RdfResourceEntry> getResourceEntriesForMediaExtraction(byte[] input)
        throws RdfDeserializationException {
      try (InputStream inputStream = new ByteArrayInputStream(input)) {
        return getResourceEntriesForMediaExtraction(inputStream);
      } catch (IOException e) {
        throw new RdfDeserializationException("Problem with deserializing RDF.", e);
      }
    }

    @Override
    public List<RdfResourceEntry> getResourceEntriesForMediaExtraction(InputStream inputStream)
        throws RdfDeserializationException {
      return getResourceEntries(inputStream, UrlType.URL_TYPES_FOR_MEDIA_EXTRACTION);
    }

    @Override
    public List<RdfResourceEntry> getResourceEntriesForLinkChecking(byte[] input)
        throws RdfDeserializationException {
      try (InputStream inputStream = new ByteArrayInputStream(input)) {
        return getResourceEntriesForLinkChecking(inputStream);
      } catch (IOException e) {
        throw new RdfDeserializationException("Problem with deserializing RDF.", e);
      }
    }

    @Override
    public List<RdfResourceEntry> getResourceEntriesForLinkChecking(InputStream inputStream)
        throws RdfDeserializationException {
      return getResourceEntries(inputStream, UrlType.URL_TYPES_FOR_LINK_CHECKING);
    }

    @Override
    public EnrichedRdf getRdfForResourceEnriching(byte[] input) throws RdfDeserializationException {
      try (InputStream inputStream = new ByteArrayInputStream(input)) {
        return getRdfForResourceEnriching(inputStream);
      } catch (IOException e) {
        throw new RdfDeserializationException("Problem with deserializing RDF.", e);
      }
    }

    @Override
    public EnrichedRdf getRdfForResourceEnriching(InputStream inputStream)
        throws RdfDeserializationException {
      return new EnrichedRdfImpl(deserialize(inputStream));
    }

    private List<RdfResourceEntry> getResourceEntries(InputStream inputStream,
        Set<UrlType> allowedUrlTypes) throws RdfDeserializationException {
      return getResourceEntries(deserialize(inputStream), allowedUrlTypes);
    }

    public List<RdfResourceEntry> getResourceEntries(RDF rdf, Set<UrlType> allowedUrlTypes) {
      return new RdfWrapper(rdf).getResourceUrls(allowedUrlTypes).entrySet().stream()
          .map(entry -> new RdfResourceEntryImpl(entry.getKey(), entry.getValue()))
          .collect(Collectors.toList());
    }

    public RDF deserialize(InputStream inputStream) throws RdfDeserializationException {
      try {
        return (RDF) context.unmarshalDocument(inputStream, "UTF-8");
      } catch (JiBXException e) {
        throw new RdfDeserializationException("Problem with deserializing RDF.", e);
      }
    }
  }

  /**
   * Creates xml from {@code EdmObject}s.
   * <p>
   * It's recommended to keep an instance for reuse.
   * <p>
   * It's not thread safe.
   */
  public static class Writer implements RdfSerializer {

    private final IMarshallingContext context;

    public Writer() throws RdfConverterException {
      try {
        context = getBindingFactory().createMarshallingContext();
      } catch (JiBXException e) {
        throw new RdfConverterException("Problem creating serializer.", e);
      }
    }

    public byte[] serialize(RDF rdf) throws RdfSerializationException {
      try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        serialize(rdf, outputStream);
        return outputStream.toByteArray();
      } catch (IOException e) {
        throw new RdfSerializationException("Problem with serializing RDF.", e);
      }
    }

    private void serialize(RDF rdf, OutputStream outputStream) throws RdfSerializationException {
      try {
        context.marshalDocument(rdf, "UTF-8", null, outputStream);
      } catch (JiBXException e) {
        throw new RdfSerializationException("Problem with serializing RDF.", e);
      }
    }

    @Override
    public byte[] serialize(EnrichedRdf rdf) throws RdfSerializationException {
      return serialize(rdf.finalizeRdf());
    }

    @Override
    public void serialize(EnrichedRdf rdf, OutputStream outputStream)
        throws RdfSerializationException {
      serialize(rdf.finalizeRdf(), outputStream);
    }
  }
}
