package eu.europeana.metis.mediaprocessing.extraction.iiif;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.CollectionType;
import java.io.IOException;
import java.util.List;

/**
 * The type Iiif info json link deserializer.
 */
public class IIIFInfoJsonLinkDeserializer extends JsonDeserializer<List<IIIFLink>> {

  @Override
  public List<IIIFLink> deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    ObjectMapper mapper = (ObjectMapper) jsonParser.getCodec();
    mapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
    JsonNode jsonNode = mapper.readTree(jsonParser);
    CollectionType listType = mapper.getTypeFactory().constructCollectionType(List.class, IIIFLink.class);

    return mapper.readValue(jsonNode.toString(), listType);
  }
}
