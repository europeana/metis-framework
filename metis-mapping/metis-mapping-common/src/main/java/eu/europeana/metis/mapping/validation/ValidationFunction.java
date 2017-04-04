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
package eu.europeana.metis.mapping.validation;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * An expression of a validation Rule against which automatic flags are generated
 * @see IsEnumerationFunction,IsUriFunction,IsUrlFunction
 * Created by ymamakis on 6/15/16.
 */
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.PROPERTY,
        property = "type")
@JsonSubTypes({
        @JsonSubTypes.Type(value = IsUriFunction.class, name = "isUriFunction"),
        @JsonSubTypes.Type(value = IsUrlFunction.class, name = "isUrlFunction"),
        @JsonSubTypes.Type(value = IsEnumerationFunction.class, name = "isEnumerationFunction"),
        @JsonSubTypes.Type(value = IsLanguageFunction.class,name = "isLanguageFunction"),
        @JsonSubTypes.Type(value = IsDateTypeFunction.class,name = "isDateTypeFunction"),
        @JsonSubTypes.Type(value = IsFloatFunction.class,name = "isFloatFunction"),
        @JsonSubTypes.Type(value = IsBooleanFunction.class,name = "isBooleanFunction")
        })
public interface ValidationFunction {

    String getType();
    /**
     * Execute the functionality specified by a rule on a value
     * @param value The value to check against the rule
     * @return Whether validation fails or passes
     */
    boolean execute(String value);
}
