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

package eu.europeana.metis.framework.common;

import org.mongodb.morphia.annotations.Entity;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * HTTP specific metadata
 * Created by ymamakis on 2/17/16.
 */
@Entity
@XmlRootElement
public class HttpMetadata extends HarvestingMetadata{

    /**
     * The harvest username
     */
    private String harvestUser;

    /**
     * The harvest password
     */
    private String harvestPassword;
    @XmlElement
    public String getHarvestUser() {
        return harvestUser;
    }

    public void setHarvestUser(String harvestUser) {
        this.harvestUser = harvestUser;
    }
    @XmlElement
    public String getHarvestPassword() {
        return harvestPassword;
    }

    public void setHarvestPassword(String harvestPassword) {
        this.harvestPassword = harvestPassword;
    }
}
