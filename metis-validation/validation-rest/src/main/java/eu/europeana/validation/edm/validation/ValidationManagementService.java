package eu.europeana.validation.edm.validation;

import eu.europeana.validation.edm.model.Schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by ymamakis on 3/14/16.
 */
public class ValidationManagementService {
    private AbstractSchemaDao dao=Configuration.getInstance().getDao();

    public void setDao(AbstractSchemaDao dao){
        this.dao = dao;
    }
    public List<Schema> getAll(){
        return dao.getAll();
    }

    public Schema getSchemaByName(String name,String version){
        return dao.getSchemaByName(name,version);
    }

    public void createSchema(String name, String path, String schematronPath, String version,InputStream file) throws IOException {
        dao.createSchema(name, path, schematronPath,version,file);
    }

    public void updateSchema(String name, String path, String schematronPath, String version, InputStream file) throws IOException {
        dao.updateSchema(name, path, schematronPath, version, file);
    }

    public void deleteSchema(String name,String version){
        dao.deleteSchema(name, version);
    }

    public byte[] getZip(String name, String version){
        return dao.getZip(name,version);
    }
}
