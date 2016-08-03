package eu.europeana.metis.mapping.xsd;

import eu.europeana.metis.mapping.model.MappingSchema;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.Map;

/**
 * DTO for generating Mapping tamplates from XSDs
 * Created by ymamakis on 8/3/16.
 */
@XmlRootElement
public abstract class AbstractXSDUploadDTO {
    private String rootFile;
    private String mappingName;
    private MappingSchema schema;
    private String rootXPath;
    private Map<String,String> namespaces;


    @XmlElement
    public String getRootFile() {
        return rootFile;
    }

    public void setRootFile(String rootFile) {
        this.rootFile = rootFile;
    }

    @XmlElement
    public String getMappingName() {
        return mappingName;
    }

    public void setMappingName(String mappingName) {
        this.mappingName = mappingName;
    }

    @XmlElement
    public MappingSchema getSchema() {
        return schema;
    }

    public void setSchema(MappingSchema schema) {
        this.schema = schema;
    }

    @XmlElement
    public String getRootXPath() {
        return rootXPath;
    }

    public void setRootXPath(String rootXPath) {
        this.rootXPath = rootXPath;
    }

    @XmlElement
    public Map<String, String> getNamespaces() {
        return namespaces;
    }

    public void setNamespaces(Map<String, String> namespaces) {
        this.namespaces = namespaces;
    }
}
