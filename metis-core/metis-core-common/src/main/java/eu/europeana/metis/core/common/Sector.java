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

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * The sector the institution belongs to
 * Created by ymamakis on 4/4/16.
 */
public enum Sector {

    GOVT_MINISTRY("Government Department / Ministry"),PRIVATE("Private"),PUBLIC("Public");

    private String name;
    Sector(String name){
        this.name = name;
    }

    public String getName(){
        return this.name;
    }

    public static Sector getSectorFromName(String name){
        for (Sector sector:Sector.values()) {
            if(sector.getName().equals(name)){
                return sector;
            }
        }
        return null;
    }

    @JsonCreator
    public static Sector getSectorFromEnumName(String name){
        for (Sector sector:Sector.values()) {
            if(sector.name().equalsIgnoreCase(name)){
                return sector;
            }
        }
        return null;
    }
}
