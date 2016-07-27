package eu.europeana.enrichment.rest.client;

import co.freeside.betamax.Betamax;
import co.freeside.betamax.Recorder;
import co.freeside.betamax.TapeMode;
import eu.europeana.enrichment.api.external.EntityClass;
import eu.europeana.enrichment.api.external.EntityWrapper;
import eu.europeana.enrichment.api.external.InputValue;
import org.codehaus.jackson.JsonGenerationException;
import org.codehaus.jackson.map.JsonMappingException;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class EnrichmentDriverTest {

	@Rule
	public Recorder recorder = new Recorder();

	@Test
	@Betamax(tape = "testEnrich.yaml", mode = TapeMode.READ_WRITE)
	public void test() throws JsonGenerationException, JsonMappingException,
			IOException {
		Properties props = new Properties();
		props.load(new FileInputStream("src/test/resources/test.properties"));
		String basePath = props.getProperty("base.path");
		EnrichmentDriver driver = new EnrichmentDriver(basePath+"/");
		List<InputValue> values = prepareValues();
		
		List<EntityWrapper> lst = driver
				.enrich(
						values, false);
		Assert.assertEquals(18, lst.size());
	}

	private List<InputValue> prepareValues() {
		List<InputValue> values = new ArrayList<InputValue>();

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
		
		return values;
	}
}
