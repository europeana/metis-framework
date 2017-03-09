/*
 * Copyright 2007-2013 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 *
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.validation.service;

import eu.europeana.validation.model.Schema;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Schema and validation management service
 * Created by ymamakis on 3/14/16.
 */
public class ValidationManagementService {
    private AbstractSchemaDao dao;

    public ValidationManagementService(Configuration configuration)
    {
        dao = configuration.getDao();
    }

    public ValidationManagementService()
    {
        dao = Configuration.getInstance().getDao();
    }
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
