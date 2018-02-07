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
package eu.europeana.enrichment.rest.client;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentBase;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.enrichment.utils.EntityClass;
import eu.europeana.enrichment.utils.InputValue;

public class EnrichmentClientTestWithMain {
	
	public static void main(String[] args) throws IOException {
		List<InputValue> values = new ArrayList<InputValue>();
        values.add(new InputValue("proxy_dc_subject", "Music", "French", EntityClass.CONCEPT));
		values.add(new InputValue("proxy_dc_subject", "Ivory", "French", EntityClass.CONCEPT));
		values.add(new InputValue("proxy_dc_subject", "Steel", null, EntityClass.CONCEPT));
		values.add(new InputValue("proxy_dcterms_spatial", "Paris", null, EntityClass.PLACE));
		values.add(new InputValue("proxy_dc_date", "1918", null, EntityClass.TIMESPAN));
		values.add(new InputValue("proxy_dc_creator", "Rembrandt", null, EntityClass.AGENT));

		/*
		ObjectMapper obj = new ObjectMapper();
		
		//client.register(InputValueList.class);
		
		Form form = new Form();
		form.param("uri", "http://data.europeana.eu/concept/base/96");
		form.param("toXml", Boolean.toString(true));
		*/	
		EnrichmentClient enrichmentClient = new EnrichmentClient("http://metis-enrichment-rest-test.eanadev.org");
        
		//String inputValue = "{ \"inputValue\": [ { \"language\": \"string\", \"originalField\": \"string\", \"value\": \"string\", \"vocabularies\": [ \"CONCEPT\"]}]}";
		
		EnrichmentResultList result = null;
		
		try {
			result = enrichmentClient.enrich(values);
			
		} catch (UnknownException e) {
			e.printStackTrace();
		}
		
		System.out.println(result.getResult());
		
		int counter = 1;
		for (EnrichmentBase b : result.getResult()) {
			System.out.println(counter + "." + b.getAbout() + " " + b.getNotes());
			counter++;
		}	
	}
}
