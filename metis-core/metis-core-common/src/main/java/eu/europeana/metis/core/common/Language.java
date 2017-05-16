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
 * The language of the dataset (enumerated)
 * Created by ymamakis on 2/17/16.
 */
public enum Language {

    AR,AZ,BE,BG,BS,CA,CS,CY,DA,DE,EL,EN,ES,ET,EU,FI,FR,GA,GD,GL,HE,HI,HR,HU,HY,IE,IS,IT,JA,KA,KO,LT,LV,MK,MT,
    MUL,NL,NO,PL,PT,RO,RU,SK,SL,SQ,SR,SV,TR,UK,YI,ZH;

    @JsonCreator
    public static Language getLanguageFromEnumName(String name){
        for (Language language:Language.values()) {
            if(language.name().equalsIgnoreCase(name)){
                return language;
            }
        }
        return null;
    }
}
