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
package eu.europeana.metis.mapping.model;

import eu.europeana.metis.mapping.validation.Flag;

/**
 * Condition and Simple Mapping Interface definition
 * @see ConditionMapping
 * @see SimpleMapping
 * Created by ymamakis on 4/13/16.
 */
public interface IMapping {
    /**
     * Type of Mapping
     * @return the type of Mapping
     */
    MappingType getType();

    /**
     * The function for the mapping
     * @return the Function for the mapping
     */
    Function getFunction();

    /**
     * The Value mappings for this field. The Value mappings contain all
     * the possible values allowed (or not allowed) for a given field as
     * specified by the user
     *
     * @return The value mappings for this field
     */
    ValueMappings getValueMappings();

    /**
     * Get a parameter value for the field
     * @return a parameter
     */
    String getParameter();

    /**
     * Get the constant value for the field
     * @return
     */
    String getConstant();

    /**
     * Get a flag that this mapping's resulting value is suspicious or wrong
     * @return a Flag
     */
    Flag getFlag();
}
