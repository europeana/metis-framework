package eu.europeana.metis.mediaprocessing.extraction.iiif;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.IOException;

/**
 * The type Iiif info json profile deserializer.
 */
public class IIIFInfoJsonProfileDeserializer extends JsonDeserializer<IIIFProfile> {

  @Override
  public IIIFProfile deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
    JsonNode node = mapper.readTree(jsonParser);

    if (!node.isArray() || node.isEmpty()) {
      return null;
    }

    IIIFProfile iiifProfile = new IIIFProfile();
    iiifProfile.setUrl(node.get(0).asText());

    if (node.size() > 1 && node.get(1).isObject()) {
      iiifProfile.setDetail(mapper.treeToValue(node.get(1), IIIFProfileDetail.class));
    }

    return iiifProfile;
  }
}
