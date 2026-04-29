package eu.europeana.metis.repository.rest.view;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import tools.jackson.core.JacksonException;
import tools.jackson.core.JsonGenerator;
import tools.jackson.databind.SerializationContext;
import tools.jackson.databind.ser.std.StdSerializer;

public class InstantSerializer extends StdSerializer<Instant> {

  final DateTimeFormatter formatter = DateTimeFormatter.ISO_INSTANT;

  /**
   * Default constructor.
   * <p>
   * Initializes the deserializer with the {@link Instant} class as the target type.
   * <p>
   * Note: Required, do not remove.
   */
  public InstantSerializer() {
    super(Instant.class);
  }

  @Override
  public void serialize(Instant instant, JsonGenerator jsonGenerator, SerializationContext provider)
      throws JacksonException {
    jsonGenerator.writeString(formatter.format(instant));
  }
}
