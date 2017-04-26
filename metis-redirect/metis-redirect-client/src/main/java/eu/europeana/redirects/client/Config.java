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
package eu.europeana.redirects.client;

import java.io.IOException;
import java.util.Properties;

/**
 * Configuration class for the Redirects REST client
 * Created by ymamakis on 1/15/16.
 */
public class Config {
    private static String redirectsPath;

    public Config(){
        Properties props = new Properties();
        try {
            props.load(Config.class.getClassLoader().getResourceAsStream("redirect.properties"));
            redirectsPath= props.getProperty("redirect.path");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public  String getRedirectsPath() {

        return redirectsPath;
    }
}
