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
import org.w3c.dom.ls.LSResourceResolver;

/**
 * Abstract Resolver for Schemas enabling search on Jar files and Openstack Swift
 * Created by ymamakis on 3/21/16.
 */
public interface AbstractLSResourceResolver extends LSResourceResolver {

    void setPrefix(String prefix);

    String getPrefix();

    ObjectStorageClient getObjectStorageClient();
}
