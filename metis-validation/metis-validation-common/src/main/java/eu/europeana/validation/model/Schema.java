package eu.europeana.validation.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.IndexOptions;
import dev.morphia.annotations.Indexed;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import org.bson.types.ObjectId;

/**
 * Created by ymamakis on 3/14/16.
 */

@XmlRootElement
@Entity
public class Schema {

    @Id
    private ObjectId id;


    @Indexed(options = @IndexOptions(unique = true))
    private String name;

    private String path;

    private String schematronPath;


    @Indexed()
    private String version;


    @XmlTransient
    @JsonIgnore
    private byte[] zip;
    @XmlElement
    public ObjectId getId() {
        return id;
    }

    public void setId(ObjectId id) {
        this.id = id;
    }
    @XmlElement
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
    @XmlElement
    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
    @XmlElement
    public String getSchematronPath() {
        return schematronPath;
    }

    public void setSchematronPath(String schematronPath) {
        this.schematronPath = schematronPath;
    }


    public byte[] getZip() {
        return zip.clone();
    }

    public void setZip(byte[] zip) {
        this.zip = zip.clone();
    }
    @XmlElement
    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }
}
