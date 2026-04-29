package eu.europeana.metis.mediaprocessing.extraction.iiif;


import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonParser;
import tools.jackson.databind.DeserializationContext;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.deser.std.StdDeserializer;
import tools.jackson.databind.json.JsonMapper;

/**
 * The type IIIF info json profile deserializer.
 */
public class IIIFInfoJsonProfileDeserializer extends StdDeserializer<IIIFProfile> {

  /**
   * Default constructor. Initializes the deserializer with the {@link IIIFProfile} class as the target type. Note: Required, do
   * not remove.
   */
  public IIIFInfoJsonProfileDeserializer() {
    super(IIIFProfile.class);
  }

  @Override
  public IIIFProfile deserialize(JsonParser jsonParser, DeserializationContext ctxt) throws JacksonException {
    ObjectMapper objectMapper = JsonMapper.builder()
                                          .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES)
                                          .disable(DeserializationFeature.FAIL_ON_TRAILING_TOKENS)
                                          .build();
    JsonNode node = objectMapper.readTree(jsonParser);

    if (!node.isArray() || node.isEmpty()) {
      return null;
    }

    IIIFProfile iiifProfile = new IIIFProfile();
    iiifProfile.setUrl(node.get(0).asString());

    if (node.size() > 1 && node.get(1).isObject()) {
      iiifProfile.setDetail(objectMapper.treeToValue(node.get(1), IIIFProfileDetail.class));
    }

    return iiifProfile;
  }
}
