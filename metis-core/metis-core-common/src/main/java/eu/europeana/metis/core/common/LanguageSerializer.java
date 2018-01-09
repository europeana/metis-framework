package eu.europeana.metis.core.common;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import java.io.IOException;

/**
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-01-09
 */
public class LanguageSerializer extends StdSerializer<Language> {


  public LanguageSerializer() {
    super(Language.class);
  }

  public LanguageSerializer(Class t) {
    super(t);
  }

  @Override
  public void serialize(Language language,
      JsonGenerator generator,
      SerializerProvider provider)
      throws IOException {
    generator.writeStartObject();
    generator.writeFieldName("enum");
    generator.writeString(language.name());
    generator.writeFieldName("name");
    generator.writeString(language.getName());
    generator.writeEndObject();
  }

}
