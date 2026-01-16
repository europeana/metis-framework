package eu.europeana.metis.repository.rest;

import eu.europeana.metis.repository.rest.view.InstantSerializer;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.ser.SerializationContextExt;

import java.io.StringWriter;
import java.io.Writer;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Unit test for {@link InstantSerializer} class
 */
class InstantSerializerTest {
  private final InstantSerializer instantSerializer = new InstantSerializer();

  @Test
  void serialize() {
    final Instant instant = Instant.parse("2020-05-20T17:58:55.00Z");
    final Writer jsonWriter = new StringWriter();
    ObjectMapper objectMapper = JsonMapper.builder()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
            .build();
    final JsonGenerator jsonGenerator = objectMapper.createGenerator(jsonWriter);
    final SerializationContextExt serializationContextExt = new ObjectMapper()._serializationContext();

    instantSerializer.serialize(instant, jsonGenerator, serializationContextExt);
    jsonGenerator.flush();

    assertEquals("\"2020-05-20T17:58:55Z\"", jsonWriter.toString());
  }
}
