package eu.europeana.metis.framework.common;

import org.bson.types.ObjectId;
import org.mongodb.morphia.annotations.Entity;
import org.mongodb.morphia.annotations.Id;

/**
 * Created by ymamakis on 4/4/16.
 */
@Entity
public class Label<T> {

    @Id
    private ObjectId id;
    private String lang;
    private T label;

    public String getLang() {
        return lang;
    }

    public T getLabel() {
        return label;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setLabel(T label) {
        this.label = label;
    }

    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
}
