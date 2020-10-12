package eu.europeana.metis.core.common;

import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import java.time.format.DateTimeFormatter;

/**
 * This class provides serialization and deserialization for the java 8 date and time api.
 */
public final class JavaTimeSerialization {

  private JavaTimeSerialization() {
  }

  /**
   * Serializer for {@link java.time.Instant} objects according to {@link
   * DateTimeFormatter#ISO_INSTANT}.
   */
  public static class IsoInstantSerializer extends InstantSerializer {

    private static final long serialVersionUID = -4172609679650500288L;

    public IsoInstantSerializer() {
      super(InstantSerializer.INSTANCE, Boolean.FALSE, DateTimeFormatter.ISO_INSTANT);
    }
  }
}
