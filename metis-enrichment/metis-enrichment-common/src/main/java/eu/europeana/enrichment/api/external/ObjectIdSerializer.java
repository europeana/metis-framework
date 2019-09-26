package eu.europeana.enrichment.api.external;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.bson.types.ObjectId;
import java.io.IOException;

/**
 * ObjectId mapper class for correct serialization of org.bson.types.ObjectId to
 * String
 *
 * @author Yorgos.Mamakis@ europeana.eu
 */
public class ObjectIdSerializer extends StdSerializer<ObjectId> {

  private static final long serialVersionUID = -3701196028474165763L;

  /**
   * Required default constructor.
   */
  public ObjectIdSerializer() {
    super(ObjectId.class);
  }

  @Override
  public void serialize(ObjectId value, JsonGenerator jgen,
      SerializerProvider provider) throws IOException {
    jgen.writeString(value.toString());

  }

}
