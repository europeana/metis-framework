package eu.europeana.validation.edm.validation;

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
