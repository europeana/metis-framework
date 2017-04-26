package eu.europeana.metis.framework.api;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;
import org.mongodb.morphia.annotations.Indexed;

/**
 * Created by gmamakis on 7-2-17.
 */
@Entity
public class MetisKey {

    @Id
    private ObjectId objId;
    @Indexed
    private String apiKey;
    private Profile profile;
    private Options options;

    public ObjectId getObjId() {
        return objId;
    }

    public void setObjId(ObjectId objId) {
        this.objId = objId;
    }

    public String getApiKey() {
        return apiKey;
    }

    public void setApiKey(String apiKey) {
        this.apiKey = apiKey;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setProfile(Profile profile) {
        this.profile = profile;
    }

    public Options getOptions() {
        return options;
    }

    public void setOptions(Options options) {
        this.options = options;
    }
}
