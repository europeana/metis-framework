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

import org.apache.log4j.Logger;
import org.w3c.dom.ls.LSInput;

import java.io.InputStream;
import java.io.InputStreamReader;

/**
 * Class enabling classpath XSD reading for split XSDs. This is because of an issue with JAXP XSD loading
 * Created by ymamakis on 12/21/15.
 */
public class OpenstackResourceResolver implements AbstractLSResourceResolver {
    private String prefix;
    private static final Logger logger = Logger.getRootLogger();

    public void setProvider(SwiftProvider provider) {
        this.provider = provider;
    }

    private SwiftProvider provider;

    @Override
    public LSInput resolveResource(String type, String namespaceURI, String publicId, String systemId, String baseURI) {
        try {

            LSInput input = new ClasspathLSInput();
            InputStream stream;
            if (!systemId.startsWith("http")) {

                stream = provider.getObjectApi().get(prefix + "/" + systemId).getPayload().openStream();

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
            logger.error(e.getMessage());
        }
        return null;
    }

    /**
     * @return the prefix
     */
    public String getPrefix() {
        return prefix;
    }

    @Override
    public SwiftProvider getSwiftProvider() {
        return provider;
    }

    /**
     * @param prefix the prefix to set
     */
    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }
}

