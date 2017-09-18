package eu.europeana.validation.service;

import eu.europeana.features.ObjectStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Created by gmamakis on 16-1-17.
 */
public class ObjectStorageResourceResolver implements AbstractLSResourceResolver {
    private String prefix;
    private static final Logger LOGGER = LoggerFactory.getLogger(ObjectStorageResourceResolver.class);
    private ObjectStorageClient client;

    public void setClient(ObjectStorageClient client){
        this.client = client;
    }
    @Override
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

    @Override
    public String getPrefix() {
        return prefix;
    }

    @Override
    public ObjectStorageClient getObjectStorageClient() {
        return client;
    }

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        try {

            LSInput input = new ClasspathLSInput();
            InputStream stream;
            if (!systemId.startsWith("http")) {

                stream = client.get(prefix+"/"+systemId).get().getPayload().openStream();
            } else {
                stream = this.getClass().getClassLoader().getResourceAsStream("xml.xsd");
            }
            input.setPublicId(publicId);
            input.setSystemId(systemId);
            input.setBaseURI(baseURI);
            input.setCharacterStream(new InputStreamReader(stream));

            return input;
        } catch (Exception e) {
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        }
        return null;
    }
}
