package eu.europeana.enrichment.rest.client.main;

import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.rest.client.EnrichmentDriver;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.map.ObjectMapper;
import org.glassfish.jersey.client.JerseyClient;
import org.glassfish.jersey.client.JerseyClientBuilder;

import javax.ws.rs.client.Entity;
import javax.ws.rs.core.Form;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.io.IOException;

public class EnrichmentClient {

	public static void main(String[] args) throws JsonGenerationException, JsonMappingException, IOException {

		JerseyClient client = JerseyClientBuilder.createClient();
		/*List<InputValue> values = new ArrayList<InputValue>();

		InputValue val1 = new InputValue();
		val1.setOriginalField("proxy_dc_subject");
		val1.setValue("Music");
		List<EntityClass> entityClasses1 = new ArrayList<EntityClass>();
		entityClasses1.add(EntityClass.CONCEPT);
		val1.setVocabularies(entityClasses1);

		InputValue val2 = new InputValue();
		val2.setOriginalField("proxy_dc_subject");
		val2.setValue("Ivory");
		List<EntityClass> entityClasses2 = new ArrayList<EntityClass>();
		entityClasses2.add(EntityClass.CONCEPT);
		val2.setVocabularies(entityClasses2);

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
		InputValueList inList = new InputValueList();
		inList.setInputValueList(values);
		*/
		ObjectMapper obj = new ObjectMapper();
		
		//client.register(InputValueList.class);
		
		Form form = new Form();
		form.param("uri", "http://data.europeana.eu/concept/base/96");
		form.param("toXml", Boolean.toString(true));
		Response res = client.target(
				"http://136.243.103.29:8080/enrichment-framework-rest-0.1-SNAPSHOT/getByUri/")
				.request()
				.post(Entity.entity(form,MediaType.APPLICATION_FORM_URLENCODED), 
						Response.class);
		EntityWrapper entity = new ObjectMapper().readValue(res.readEntity(String.class),EntityWrapper.class);
		EnrichmentDriver driver = new EnrichmentDriver("http://136.243.103.29:8080/enrichment-framework-rest-0.1-SNAPSHOT/");
		EntityWrapper entityRet = driver.getByUri("http://vocab.getty.edu/aat/300177435");



		System.out.println(entityRet.getContextualEntity());
	}

}
