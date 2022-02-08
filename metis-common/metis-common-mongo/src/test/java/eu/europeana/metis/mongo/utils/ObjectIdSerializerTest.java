package eu.europeana.metis.mongo.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializerProvider;
import eu.europeana.metis.mongo.utils.ObjectIdSerializer;
import java.io.IOException;
import java.io.StringWriter;
import java.io.Writer;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Unit test for class {@link ObjectIdSerializer}
 *
 * @author Jorge Ortiz
 * @since 31-01-2022
 */
class ObjectIdSerializerTest {

  private ObjectIdSerializer objectIdSerializer;

  @BeforeEach
  void setup() {
    this.objectIdSerializer = new ObjectIdSerializer();
  }

  @Test
  void serializeValue_expectSuccess() throws IOException {
    final String expectedObjectId = "\"507f1f77bcf86cd799439011\"";
    final ObjectId objectId = new ObjectId("507f1f77bcf86cd799439011");
    final Writer jsonWriter = new StringWriter();
    final JsonGenerator jsonGenerator = getJsonGenerator(jsonWriter);

    objectIdSerializer.serialize(objectId, jsonGenerator, getSerializerProvider());
    jsonGenerator.flush();

    assertEquals(expectedObjectId, jsonWriter.toString());
  }

  @Test
  void serializeNull_expectSuccess() throws IOException {
    final String expectedObjectId = "null";
    final Writer jsonWriter = new StringWriter();
    final JsonGenerator jsonGenerator = getJsonGenerator(jsonWriter);

    objectIdSerializer.serialize(null, jsonGenerator, getSerializerProvider());
    jsonGenerator.flush();

    assertEquals(expectedObjectId, jsonWriter.toString());
  }

  private static JsonGenerator getJsonGenerator(Writer writer) throws IOException {
    final JsonGenerator jsonGenerator = JsonFactory.builder().build().createGenerator(writer);
    return jsonGenerator;
  }

  private static SerializerProvider getSerializerProvider() {
    return new ObjectMapper().getSerializerProvider();
  }
}