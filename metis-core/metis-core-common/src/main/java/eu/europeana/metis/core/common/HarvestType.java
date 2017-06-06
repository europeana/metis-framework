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
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * The list of harvesting types supported by METIS
 * Created by ymamakis on 2/17/16.
 */
public enum HarvestType {
  FTP_HARVEST, HTTP_HARVEST, OAIPMH_HARVEST, FOLDER_HARVEST, NULL;

  @JsonCreator
  public static HarvestType getHarvestTypeFromEnumName(
      @JsonProperty("harvestName") String harvestName) {
    for (HarvestType harvestType : HarvestType.values()) {
      if (harvestType.name().equalsIgnoreCase(harvestName)) {
        return harvestType;
      }
    }
    return NULL;
  }
}
