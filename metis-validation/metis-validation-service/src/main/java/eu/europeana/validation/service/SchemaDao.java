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

import org.apache.commons.io.FileUtils;
import org.mongodb.morphia.Datastore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Filesystem schema dao
 * Created by ymamakis on 3/21/16.
 */
public class SchemaDao extends AbstractSchemaDao{

    public SchemaDao(Datastore datastore, String rootPath){
        super(datastore,rootPath);
    }

    @Override
    public void unzipFile(String fullPath, byte[] in) throws IOException {
        File tmp = new File("/tmp/"+new Date().getTime()+".zip");
        FileUtils.writeByteArrayToFile(tmp,in);
        ZipFile zip = new ZipFile(tmp);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            if(entry.isDirectory()){
                new File(fullPath+"/"+entry.getName()).mkdir();
            } else {
                InputStream zipStream = zip.getInputStream(entry);
                FileUtils.copyInputStreamToFile(zipStream,new File(fullPath+"/"+entry.getName()));
            }
        }
        FileUtils.deleteQuietly(tmp);
    }


}
