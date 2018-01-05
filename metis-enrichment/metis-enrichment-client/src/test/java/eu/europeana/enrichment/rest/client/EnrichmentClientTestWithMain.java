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
import java.util.Properties;
import eu.europeana.enrichment.api.exceptions.UnknownException;
import eu.europeana.enrichment.api.external.model.EnrichmentResultList;
import eu.europeana.metis.utils.EntityClass;
import eu.europeana.metis.utils.InputValue;

public class EnrichmentClientTestWithMain {
	
    // TODO JOCHEN should not have a main class.
	public static void main(String[] args) throws IOException {
		List<InputValue> values = new ArrayList<InputValue>();

		InputValue val1 = new InputValue();
		val1.setOriginalField("proxy_dc_subject");
		val1.setValue("Music");
		List<EntityClass> entityClasses1 = new ArrayList<EntityClass>();
		entityClasses1.add(EntityClass.CONCEPT);
		val1.setVocabularies(entityClasses1);
		val1.setLanguage("French");

		InputValue val2 = new InputValue();
		val2.setOriginalField("proxy_dc_subject");
		val2.setValue("Ivory");
		List<EntityClass> entityClasses2 = new ArrayList<EntityClass>();
		entityClasses2.add(EntityClass.CONCEPT);
		val2.setVocabularies(entityClasses2);
		val2.setLanguage("French");

		InputValue val3 = new InputValue();
		val3.setOriginalField("proxy_dc_subject");
		val3.setValue("Steel");
		List<EntityClass> entityClasses3 = new ArrayList<EntityClass>();
		entityClasses3.add(EntityClass.CONCEPT);
		val3.setVocabularies(entityClasses3);

		InputValue val4 = new InputValue();
		val4.setOriginalField("proxy_dcterms_spatial");
		val4.setValue("Paris");
		List<EntityClass> entityClasses4 = new ArrayList<EntityClass>();
		entityClasses4.add(EntityClass.PLACE);
		val4.setVocabularies(entityClasses4);

		InputValue val5 = new InputValue();
		val5.setOriginalField("proxy_dc_date");
		val5.setValue("1918");
		List<EntityClass> entityClasses5 = new ArrayList<EntityClass>();
		entityClasses5.add(EntityClass.TIMESPAN);
		val5.setVocabularies(entityClasses5);

		InputValue val6 = new InputValue();
		val6.setOriginalField("proxy_dc_creator");
		val6.setValue("Rembrandt");
		List<EntityClass> entityClasses6 = new ArrayList<EntityClass>();
		entityClasses6.add(EntityClass.AGENT);
		val6.setVocabularies(entityClasses6);

		values.add(val1);
		values.add(val2);
		values.add(val3);
		values.add(val4);
		values.add(val5);
		values.add(val6);
		
		/*
		ObjectMapper obj = new ObjectMapper();
		
		//client.register(InputValueList.class);
		
		Form form = new Form();
		form.param("uri", "http://data.europeana.eu/concept/base/96");
		form.param("toXml", Boolean.toString(true));
		*/
		//eu.europeana.enrichment.rest.client.EnrichmentClient enrichmentClient = new eu.europeana.enrichment.rest.client.EnrichmentClient("http://metis-enrichment-test.cfapps.io/");
		
        final Properties props = new Properties();
        try {
          props.load(EnrichmentClientTestWithMain.class.getClassLoader()
              .getResourceAsStream("client.properties"));
        } catch (IOException e1) {
          e1.printStackTrace();
          throw e1;
        }
        final String hostUrl = props.getProperty("host.url");
		EnrichmentClient enrichmentClient = new EnrichmentClient(hostUrl);
		
		//String inputValue = "{ \"inputValue\": [ { \"language\": \"string\", \"originalField\": \"string\", \"value\": \"string\", \"vocabularies\": [ \"CONCEPT\"]}]}";
		
		EnrichmentResultList result = null;
		
		try {
			result = enrichmentClient.enrich(values);
		} catch (UnknownException e) {
			e.printStackTrace();
		}
		
		System.out.println(result);
	}
}
