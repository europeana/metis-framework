package eu.europeana.validation.edm.validation;

import eu.europeana.validation.edm.model.Schema;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by ymamakis on 3/14/16.
 */
public abstract class AbstractSchemaDao{

    public AbstractSchemaDao(Datastore datastore, String rootPath){
        this.datastore = datastore;
        this.rootPath = rootPath;
    }
    private Datastore datastore;
    private String rootPath;
    public List<Schema> getAll(){
        return datastore.find(Schema.class).asList();
    }

    public Schema getSchemaByName(String name, String version){
        return datastore.find(Schema.class).filter("name",name).filter("version",version==null?"undefined":version).get();
    }

    public byte[] getZip(String name, String version){
        return datastore.find(Schema.class).filter("name",name).filter("version", version==null?"undefined":version).get().getZip();
    }


    public void updateSchema(String name, String path, String schematronPath, String version, InputStream file) throws IOException {
        UpdateOperations<Schema> ops = datastore.createUpdateOperations(Schema.class);
        Query<Schema> query = datastore.createQuery(Schema.class).filter("name",name).filter("version",version==null?"undefined":version);
        if(StringUtils.isEmpty(schematronPath)){
            ops.unset("schematronPath");
        } else {
            ops.set("schematronPath",rootPath+"/"+name+"/"+version+"/" +schematronPath);
        }
        ops.set("path",rootPath+"/"+name+"/"+version+"/"+path);
        byte[] b = IOUtils.toByteArray(file);
        ops.set("zip",b);
        datastore.update(query,ops);
        unzipFile(rootPath+"/"+name+"/"+version,b);
    }

    public abstract void unzipFile(String path, byte[] b) throws IOException ;

    public void createSchema(String name, String path, String schematronPath, String version, InputStream file) throws IOException{
        Schema schema = new Schema();
        schema.setName(name);
        schema.setPath(rootPath+"/"+name+"/"+version+"/"+path);
        if(StringUtils.isNotEmpty(schematronPath)){
            schema.setSchematronPath(rootPath+"/"+name+"/"+version+"/" +schematronPath);
        }
        schema.setVersion(version);
        byte[] b = IOUtils.toByteArray(file);
        schema.setZip(b);
        unzipFile(rootPath+"/"+name+"/"+version,b);
        datastore.save(schema);
    }

    public void deleteSchema(String name,String version){
        datastore.delete(datastore.createQuery(Schema.class).filter("name",name).filter("version",version==null?"undefined":version));
    }
}
