package eu.europeana.metis.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.types.ObjectId;

/**
 * Created by ymamakis on 7/18/16.
 */
public class CustomObjectMapper extends ObjectMapper {

    public CustomObjectMapper() {
        SimpleModule module = new SimpleModule("ObjectIdmodule");
        module.addSerializer(ObjectId.class, new ObjectIdSerializer());
        this.registerModule(module);
    }

}