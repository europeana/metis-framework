package eu.europeana.metis.core.common;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import eu.europeana.metis.utils.Country;
import java.io.IOException;

/**
 * Deserializer for {@link Country} enum.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-09
 */
public class CountryDeserializer extends StdDeserializer<Country> {

  private static final long serialVersionUID = 1L;

  /**
   * Constructor for null value
   */
  public CountryDeserializer() {
    this(null);
  }

  /**
   * Required as part of {@link StdDeserializer}
   *
   * @param vc required parameter
   */
  public CountryDeserializer(Class<?> vc) {
    super(vc);
  }

  @Override
  public Country deserialize(JsonParser jsonParser, DeserializationContext deserializationContext)
      throws IOException {
    JsonNode node = jsonParser.getCodec().readTree(jsonParser);
    return Country.getCountryFromEnumName(node.get("enum").asText());
  }
}
