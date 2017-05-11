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

package eu.europeana.metis.core.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * No Organization was found either in METIS or in CRM
 * Created by ymamakis on 2/25/16.
 */
@ResponseStatus(value= HttpStatus.CONFLICT, reason="Organization already exists")
public class OrganizationAlreadyExistsException extends Exception {
    private static final long serialVersionUID = -3332292346834265371L;

    public OrganizationAlreadyExistsException(String organizationId){
        super("Organization with organizationId " + organizationId + " already exists");
    }
}
