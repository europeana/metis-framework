package eu.europeana.metis.mediaprocessing.extraction.iiif;

import java.util.ArrayList;
import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;

/**
 * The type Iiif info json tile deserializer.
 */
public class IIIFInfoJsonTileDeserializer extends StdDeserializer<List<Tile>> {

  private static final String WIDTH_FIELD = "width";
  private static final String HEIGHT_FIELD = "height";
  private static final String SCALE_FACTORS_FIELD = "scaleFactors";
  /**
   * Instantiates a new Iiif info json tile deserializer.
   */
  public IIIFInfoJsonTileDeserializer() {
    super(List.class);
  }

  @Override
  public List<Tile> deserialize(JsonParser jsonParser, DeserializationContext context) throws JacksonException {
    ObjectMapper objectMapper = JsonMapper.builder()
                                          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                          .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                                          .build();
    JsonNode node = objectMapper.readTree(jsonParser);
    if (node == null) {
      return List.of();
    }
    List<Tile> tileList = null;
    if (node.isArray()) {
      tileList = new ArrayList<>();
      for (int i = 0; i < node.size(); i++) {
        if (node.get(i).get(WIDTH_FIELD) != null && node.get(i).get(HEIGHT_FIELD) != null) {
          tileList.add(new Tile(node.get(i).get(WIDTH_FIELD).asInt(), node.get(i).get(HEIGHT_FIELD).asInt(),
              objectMapper.treeToValue(node.get(i).get(SCALE_FACTORS_FIELD),
                  List.class)));
        } else if (node.get(i).get(WIDTH_FIELD) != null && node.get(i).get(HEIGHT_FIELD) == null) {
          tileList.add(new Tile(node.get(i).get(WIDTH_FIELD).asInt(),
                  objectMapper.treeToValue(node.get(i).get(SCALE_FACTORS_FIELD), List.class)));
        }
      }
    }
    return tileList;
  }

}
