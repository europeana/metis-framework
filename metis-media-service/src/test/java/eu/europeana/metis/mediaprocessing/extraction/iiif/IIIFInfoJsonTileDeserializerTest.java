package eu.europeana.metis.mediaprocessing.extraction.iiif;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertIterableEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import org.junit.jupiter.api.Test;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

class IIIFInfoJsonTileDeserializerTest {

  private final IIIFInfoJsonTileDeserializer deserializer = new IIIFInfoJsonTileDeserializer();
  private final ObjectMapper objectMapper = JsonMapper.builder().build();

  @Test
  void shouldReturnEmptyListWhenInputIsNull() throws Exception {
    try (JsonParser jsonParser = objectMapper.createParser("")) {
      final List<Tile> result = deserializer.deserialize(jsonParser, objectMapper._deserializationContext());

      assertEquals(List.of(), result);
    }
  }

  @Test
  void shouldReturnNullWhenInputIsNotAnArray() throws Exception {
    try (JsonParser jsonParser = objectMapper.createParser("{\"width\":256}")) {
      jsonParser.nextToken();

      assertNull(deserializer.deserialize(jsonParser, objectMapper._deserializationContext()));
    }
  }

  @Test
  void shouldReturnEmptyListForEmptyArray() throws Exception {
    try (JsonParser jsonParser = objectMapper.createParser("[]")) {
      jsonParser.nextToken();

      assertTrue(deserializer.deserialize(jsonParser, objectMapper._deserializationContext()).isEmpty());
    }
  }

  @Test
  void shouldDeserializeTilesWithAndWithoutHeight() throws Exception {
    final String json = """
        [
          {"width":256,"height":128,"scaleFactors":[1,2,4],"ignored":"value"},
          {"width":512,"scaleFactors":[1,4]},
          {"height":64,"scaleFactors":[8]},
          {"scaleFactors":[16]}
        ]
        """;

    try (JsonParser jsonParser = objectMapper.createParser(json)) {
      jsonParser.nextToken();

      final List<Tile> result = deserializer.deserialize(jsonParser, objectMapper._deserializationContext());

      assertEquals(2, result.size());
      assertEquals(new Tile(256, 128, List.of(1, 2, 4)), result.get(0));
      assertEquals(new Tile(512, List.of(1, 4)), result.get(1));
      assertIterableEquals(List.of(1, 2, 4), result.get(0).getScaleFactors());
      assertIterableEquals(List.of(1, 4), result.get(1).getScaleFactors());
    }
  }
}
