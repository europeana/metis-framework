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
package eu.europeana.redirects.params;

import eu.europeana.corelib.definitions.model.EdmLabel;

/**
 * Created by ymamakis on 1/14/16.
 */
public final class  QualifiedFieldName {

    public final static String ISSHOWNAT=EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_AT.toString();

    public final static String ISSHOWNBY = EdmLabel.PROVIDER_AGGREGATION_EDM_IS_SHOWN_BY.toString();

    public final static String OBJECT = EdmLabel.PROVIDER_AGGREGATION_EDM_OBJECT.toString();

    public final static String IDENTIFIER = EdmLabel.PROXY_DC_IDENTIFIER.toString();

}
