package eu.europeana.validation.edm.validation;

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
 * Created by ymamakis on 3/21/16.
 */
public class OpenstackSchemaDao extends AbstractSchemaDao {

    public void setProvider(SwiftProvider provider) {
        this.provider = provider;
    }

    private SwiftProvider provider;
    public OpenstackSchemaDao(Datastore datastore, String rootPath){
        super(datastore,rootPath);

    }

    @Override
    public void unzipFile(String fullPath, byte[] b) throws IOException {
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
                provider.getObjectApi().put(fullPath+"/"+entry.getName(),new InputStreamPayload(zipStream));

            }
        }
        FileUtils.deleteQuietly(tmp);
    }


}
