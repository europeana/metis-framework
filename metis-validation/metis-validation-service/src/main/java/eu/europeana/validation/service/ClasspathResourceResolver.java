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

import eu.europeana.features.ObjectStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.w3c.dom.ls.LSInput;

import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

/**
 * Class enabling classpath XSD reading for split XSDs. This is because of an issue with JAXP XSD loading
 * Created by ymamakis on 12/21/15.
 */
public class ClasspathResourceResolver implements AbstractLSResourceResolver {
    private String prefix;
    private static final Logger LOGGER = LoggerFactory.getLogger(ClasspathResourceResolver.class);
    private static Map<String,InputStream> cache;
    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        try {
            if(cache==null){
                cache= new HashMap<>();
            }
            LSInput input = new ClasspathLSInput();
            InputStream stream;
            if(!systemId.startsWith("http")) {
                stream = new FileInputStream(prefix+"/"+systemId);
            }else {
                if(cache.get(systemId)==null) {

                    stream = new FileInputStream(this.getClass().getClassLoader().getResource("xml.xsd").getFile());
                    cache.put(systemId,stream);
                } else {
                    stream = cache.get(systemId);
                }
            }
            input.setPublicId(publicId);
            input.setSystemId(systemId);
            input.setBaseURI(baseURI);
            input.setCharacterStream(new InputStreamReader(stream));
            return input;
        } catch (Exception e){
            e.printStackTrace();
            LOGGER.error(e.getMessage());
        } return null;
    }
    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    @Override
    public ObjectStorageClient getObjectStorageClient() {
        return null;
    }


    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}

