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

package eu.europeana.metis.core.common;

/**
 * A role of a supplying institution to Europeana
 * Created by ymamakis on 4/4/16.
 */
public enum Role {

    CONTENT_PROVIDER("Content provider"),DIRECT_PROVIDER("Direct Provider"),DATA_AGGREGATOR("Data aggregator"),
    FINANCIAL_PARTNER("Financial partner"),POLICY_MAKER("Policy maker"),
    CONSULTANT("Consultant"),OTHER("Other"),EUROPEANA("Europeana");

    private String name;

    Role(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public static Role getRoleFromName(String name){
        for (Role role:Role.values()) {
            if(role.getName().equals(name)){
                return role;
            }
        }
        return null;
    }
}
