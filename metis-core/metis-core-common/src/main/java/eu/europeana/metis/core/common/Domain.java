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
 * A provider domain
 * Created by ymamakis on 4/4/16.
 */
public enum Domain {
    GALLERY("Gallery"), LIBRARY("Library"), ARCHIVE("Archive"), MUSEUM("Museum"), AV_ARCHIVE("AV/Sound archive"),
    PUBLISHER("Publisher"), RESEARCH("Research (eg university)"), EDUCATION("Education (eg school)"),
    CREATIVE_INDUSTRY("Creative Industry"), CROSS_DOMAIN("Cross domain"), PERFORMING_ARTS("Performing Arts"),
    CONSULTANT("Consultant"), OTHER("Other");

    private String name;
    Domain(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public static Domain getDomainFromName(String name){
        for (Domain domain:Domain.values()) {
            if(domain.getName().equals(name)){
                return domain;
            }
        }
        return null;
    }
}
