package eu.europeana.metis.mediaprocessing;

import eu.europeana.metis.schema.jibx.RDF;
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
class RdfSerializerImpl implements RdfSerializer {

  private final MarshallingContextWrapper marshallingContext = new MarshallingContextWrapper();

  private static class MarshallingContextWrapper extends
          AbstractThreadSafeWrapper<IMarshallingContext, RdfSerializationException> {

    MarshallingContextWrapper() {
      super(() -> {
        try {
          return RdfBindingFactoryProvider.getBindingFactory().createMarshallingContext();
        } catch (JiBXException e) {
          throw new RdfSerializationException("Problem creating serializer.", e);
        }
      });
    }

    void serializeFromRdf(RDF rdf, OutputStream outputStream) throws RdfSerializationException {
      process(context -> {
        try {
          context.marshalDocument(rdf, "UTF-8", null, outputStream);
          return null;
        } catch (JiBXException e) {
          throw new RdfSerializationException("Problem with serializing RDF.", e);
        }
      });
    }
  }

  @Override
  public byte[] serialize(EnrichedRdf rdf) throws RdfSerializationException {
    try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
      serialize(rdf, outputStream);
      return outputStream.toByteArray();
    } catch (IOException e) {
      throw new RdfSerializationException("Problem with serializing RDF.", e);
    }
  }

  @Override
  public void serialize(EnrichedRdf rdf, OutputStream outputStream)
      throws RdfSerializationException {
    marshallingContext.serializeFromRdf(rdf.finalizeRdf(), outputStream);
  }
}
