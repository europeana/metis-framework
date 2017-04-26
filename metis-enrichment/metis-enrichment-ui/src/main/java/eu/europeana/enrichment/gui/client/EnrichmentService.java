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
package eu.europeana.enrichment.gui.client;

import com.google.gwt.user.client.rpc.RemoteService;
import com.google.gwt.user.client.rpc.RemoteServiceRelativePath;
import eu.europeana.enrichment.gui.shared.EntityWrapperDTO;
import eu.europeana.enrichment.gui.shared.InputValueDTO;

import java.util.List;
/**
 * Main enrichment service interface exposing the enrich functionality
 * @author Yorgos.Mamakis@ europeana.eu
 *
 */
@RemoteServiceRelativePath("enrich")
public interface EnrichmentService extends RemoteService {

	/**
	 * Enrichment method
	 * @param values The values to enrich
	 * @param toEdm export as EDM/XML
	 * @return The generated enrichments
	 */
	List<EntityWrapperDTO> enrich(List<InputValueDTO> values, boolean toEdm);
}
