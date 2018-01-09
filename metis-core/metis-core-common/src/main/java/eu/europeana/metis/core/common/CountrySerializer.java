package eu.europeana.metis.core.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;


/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-09
 */
public class CountrySerializer extends StdSerializer<Country> {

  public CountrySerializer() {
    super(Country.class);
  }

  public CountrySerializer(Class t) {
    super(t);
  }

  @Override
  public void serialize(Country country,
      JsonGenerator generator,
      SerializerProvider provider)
      throws IOException {
    generator.writeStartObject();
    generator.writeFieldName("enum");
    generator.writeString(country.name());
    generator.writeFieldName("name");
    generator.writeString(country.getName());
    generator.writeFieldName("isoCode");
    generator.writeString(country.getIsoCode());
    generator.writeEndObject();
  }
}
