package eu.europeana.metis.core.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import java.io.IOException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-09
 */
public class LanguageDeserializer extends StdDeserializer<Language> {

  private static final long serialVersionUID = 1L;

  public LanguageDeserializer() {
    this(null);
  }

  public LanguageDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Language deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    return Language.getLanguageFromEnumName(node.get("enum").asText());
  }

}
