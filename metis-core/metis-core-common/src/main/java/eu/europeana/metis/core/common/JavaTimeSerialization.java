package eu.europeana.metis.core.common;

import static java.lang.Boolean.FALSE;

import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import java.io.Serial;
import java.time.format.DateTimeFormatter;

/**
 * This class provides serialization and deserialization for the java 8 date and time api.
 */
public final class JavaTimeSerialization {

  private JavaTimeSerialization() {
  }

  /**
   * Serializer for {@link java.time.Instant} objects according to {@link DateTimeFormatter#ISO_INSTANT}.
   */
  public static class IsoInstantSerializer extends InstantSerializer {

    @Serial private static final long serialVersionUID = -4172609679650500288L;

    /**
     * Constructor for Iso InstantSerializer. Does not utilize nanoseconds.
     */
    public IsoInstantSerializer() {
      super(InstantSerializer.INSTANCE, FALSE, FALSE, DateTimeFormatter.ISO_INSTANT);
    }
  }
}
