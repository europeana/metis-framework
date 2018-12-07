package eu.europeana.metis.mediaprocessing;

import eu.europeana.corelib.definitions.jibx.RDF;
import eu.europeana.metis.mediaprocessing.exception.RdfConverterException;
import eu.europeana.metis.mediaprocessing.exception.RdfSerializationException;
import eu.europeana.metis.mediaprocessing.model.EnrichedRdf;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.jibx.runtime.IMarshallingContext;
import org.jibx.runtime.JiBXException;

/**
 * This object implements RDF serialization functionality.
 */
public class RdfSerializerImpl extends RdfConverter implements RdfSerializer {

  private final IMarshallingContext context;

  /**
   * Constructor.
   *
   * @throws RdfConverterException In case something went wrong constructing this object.
   */
  // TODO should become package-private.
  public RdfSerializerImpl() throws RdfConverterException {
    try {
      context = getBindingFactory().createMarshallingContext();
    } catch (JiBXException e) {
      throw new RdfConverterException("Problem creating serializer.", e);
    }
  }

  // TODO should become private.
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
