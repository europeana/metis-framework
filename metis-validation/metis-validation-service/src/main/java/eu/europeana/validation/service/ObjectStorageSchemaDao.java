package eu.europeana.validation.service;

import com.amazonaws.util.BinaryUtils;
import com.amazonaws.util.IOUtils;
import com.amazonaws.util.Md5Utils;
import eu.europeana.domain.ObjectMetadata;
import eu.europeana.domain.StorageObject;
import eu.europeana.features.ObjectStorageClient;
import org.apache.commons.io.FileUtils;
import org.jclouds.io.Payload;
import org.jclouds.io.payloads.InputStreamPayload;
import org.mongodb.morphia.Datastore;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Date;
import java.util.Enumeration;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * Created by gmamakis on 16-1-17.
 */
public class ObjectStorageSchemaDao extends AbstractSchemaDao {
    private ObjectStorageClient client;
    private String rootPath;

    public void setClient(ObjectStorageClient client) {
        this.client = client;
    }

    public ObjectStorageSchemaDao(Datastore datastore, String rootPath) {
        super(datastore, rootPath);
        this.rootPath = rootPath;
    }

    @Override
    public void unzipFile(String path, byte[] b) throws IOException {
        File tmp = new File("/tmp/" + new Date().getTime() + ".zip");
        FileUtils.writeByteArrayToFile(tmp, b);
        try(ZipFile zip = new ZipFile(tmp)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.isDirectory()) {
                    //DO NOTHING
                } else {
                    InputStream zipStream = zip.getInputStream(entry);
                    StorageObject obj = null;
                    try {

                        ObjectMetadata metadata = new ObjectMetadata();
                        metadata.setContentLength(zipStream.available());

                        byte[] content = IOUtils.toByteArray(zipStream);
                        byte[] md5 = Md5Utils.computeMD5Hash(content);
                        String md5Base64 = BinaryUtils.toBase64(md5);

                        metadata.setContentMD5(md5Base64);
                        //TODO: workaround because of an issue with the reading in S3StorageClient
                        Payload payload = new InputStreamPayload(zip.getInputStream(entry));
                        Logger.getAnonymousLogger()
                            .info(rootPath + "/" + path + "/" + entry.getName());
                        obj = new StorageObject(path + "/" + entry.getName(),
                            new URI(path + "/" + entry.getName()), new Date(), metadata, payload);
                    } catch (URISyntaxException e) {
                        e.printStackTrace();
                    }
                    client.put(obj);

                }
            }
        }
        FileUtils.deleteQuietly(tmp);
    }
}
