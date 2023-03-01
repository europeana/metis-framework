package eu.europeana.metis.repository.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import eu.europeana.metis.repository.rest.view.InstantSerializer;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import org.junit.jupiter.api.Test;

/**
 * Unit test for {@link InstantSerializer} class
 */
class InstantSerializerTest {
  private InstantSerializer instantSerializer = new InstantSerializer();

  @Test
  void serialize() throws IOException {
    final Instant instant = Instant.parse("2020-05-20T17:58:55.00Z");
    final Writer jsonWriter = new StringWriter();
    final JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
    final SerializerProvider serializerProvider = new ObjectMapper().getSerializerProvider();

    instantSerializer.serialize(instant, jsonGenerator, serializerProvider);
    jsonGenerator.flush();

    assertEquals("\"2020-05-20T17:58:55Z\"", jsonWriter.toString());
  }
}
