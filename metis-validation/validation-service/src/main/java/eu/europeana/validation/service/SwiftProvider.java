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

import org.jclouds.ContextBuilder;
import org.jclouds.openstack.swift.v1.SwiftApi;
import org.jclouds.openstack.swift.v1.features.ContainerApi;
import org.jclouds.openstack.swift.v1.features.ObjectApi;

/**
 * Connector to Openstack Swift
 * Created by ymamakis on 11/17/15.
 */
public class SwiftProvider {

    private ObjectApi objectApi;
    public SwiftProvider(String authUrl, String userName, String password, String containerName, String regionName, String tenantName){

        final SwiftApi swiftApi = ContextBuilder.newBuilder("openstack-swift")
                .credentials(tenantName+":"+userName, password)
                .endpoint(authUrl)
                .buildApi(SwiftApi.class);

        final ContainerApi containerApi = swiftApi.getContainerApi(regionName);

        if (containerApi.get(containerName) == null) {
            if (!containerApi.create(containerName)) {
                throw new RuntimeException("swift cannot create container: " + containerName);
            }
        }

        objectApi = swiftApi.getObjectApi(regionName, containerName);
    }

    public ObjectApi getObjectApi(){
        return this.objectApi;
    }
}
