package eu.europeana.validation.service;

import eu.europeana.features.ObjectStorageClient;
import org.apache.commons.io.FileUtils;
import org.jclouds.io.payloads.InputStreamPayload;
import org.mongodb.morphia.Datastore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Enumeration;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by gmamakis on 16-1-17.
 */
public class ObjectStorageSchemaDao extends AbstractSchemaDao {
    private ObjectStorageClient client;
    public void setClient(ObjectStorageClient client){
        this.client = client;
    }
    public ObjectStorageSchemaDao(Datastore datastore, String rootPath){
        super(datastore,rootPath);

    }
    @Override
    public void unzipFile(String path, byte[] b) throws IOException {
        File tmp = new File("/tmp/"+new Date().getTime()+".zip");
        FileUtils.writeByteArrayToFile(tmp,b);
        ZipFile zip = new ZipFile(tmp);
        Enumeration<? extends ZipEntry> entries = zip.entries();
        while(entries.hasMoreElements()){
            ZipEntry entry = entries.nextElement();
            if(entry.isDirectory()){
                //DO NOTHING
            } else {
                InputStream zipStream = zip.getInputStream(entry);
                client.put(path+"/"+entry.getName(),new InputStreamPayload(zipStream));

            }
        }
        FileUtils.deleteQuietly(tmp);
    }
}
