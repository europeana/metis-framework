package eu.europeana.metis.mediaprocessing.extraction.iiif;

import java.util.List;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.type.CollectionType;

/**
 * The type IIIF info json link deserializer.
 */
public class IIIFInfoJsonLinkDeserializer extends StdDeserializer<List<IIIFLink>> {

  /**
   * Default constructor.
   * Initializes the deserializer with the {@link List} class as the target type.
   * Note: Required, do not remove.
   */
  public IIIFInfoJsonLinkDeserializer() {
    super(List.class);
  }

  @Override
  public List<IIIFLink> deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws JacksonException {
    ObjectMapper objectMapper = JsonMapper.builder()
                                          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                          .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                                          .build();
    JsonNode jsonNode = objectMapper.readTree(jsonParser);
    CollectionType listType = objectMapper.getTypeFactory().constructCollectionType(List.class, IIIFLink.class);

    return objectMapper.readValue(jsonNode.toString(), listType);
  }
}
