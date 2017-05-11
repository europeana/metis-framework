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
 * Enumeration denoting the geographical level of a  provider
 * Created by ymamakis on 4/4/16.
 */
public enum GeographicLevel {
    REGIONAL("Regional"),MUNICIPAL("Municipal"),NATIONAL("National"),EUROPEAN("European"),WORLDWIDE("Worldwide");

    private String name;
    GeographicLevel(String name){
        this.name =name;
    }

    public String getName(){
        return this.name;
    }

    public static GeographicLevel getGeographicLevelFromName(String name){
        for (GeographicLevel gl:GeographicLevel.values()) {
            if(gl.getName().equals(name)){
                return gl;
            }
        }
        return null;
    }
}
