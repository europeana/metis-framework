package eu.europeana.metis.repository.rest;

import static org.junit.jupiter.api.Assertions.assertEquals;

import eu.europeana.metis.repository.rest.view.InstantSerializer;
import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonGenerator;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.ser.SerializationContextExt;

/**
 * Unit test for {@link InstantSerializer} class
 */
class InstantSerializerTest {
  private final InstantSerializer instantSerializer = new InstantSerializer();

  @Test
  void serialize() {
    final Instant instant = Instant.parse("2020-05-20T17:58:55.00Z");
    final Writer jsonWriter = new StringWriter();
    final JsonGenerator jsonGenerator = new JsonFactory().createGenerator(jsonWriter);
    final SerializationContextExt serializationContextExt = new ObjectMapper()._serializationContext();

    instantSerializer.serialize(instant, jsonGenerator, serializationContextExt);
    jsonGenerator.flush();

    assertEquals("\"2020-05-20T17:58:55Z\"", jsonWriter.toString());
  }
}
