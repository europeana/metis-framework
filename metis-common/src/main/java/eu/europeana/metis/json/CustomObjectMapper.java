package eu.europeana.metis.json;

import com.fasterxml.jackson.databind.Module;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.module.SimpleModule;
import org.bson.types.ObjectId;

/**
 * Created by ymamakis on 7/18/16.
 */
public class CustomObjectMapper extends ObjectMapper {

    /** Required for instances of {@link java.io.Serializable}. **/
    private static final long serialVersionUID = -1337976528047131517L;

    public CustomObjectMapper() {
        SimpleModule module = new SimpleModule("ObjectIdmodule");
        module.addSerializer(ObjectId.class, new ObjectIdSerializer());
        this.registerModule(module);
    }
    
    /** Provides final functionality for constructor. **/
    @Override
    public final ObjectMapper registerModule(Module module) {
        return super.registerModule(module);
    }
}
