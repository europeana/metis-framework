package eu.europeana.enrichment.service;

//import de.flapdoodle.embed.mongo.MongodExecutable;
//import de.flapdoodle.embed.mongo.MongodStarter;
//import de.flapdoodle.embed.mongo.config.IMongodConfig;
//import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
//import de.flapdoodle.embed.mongo.config.Net;
//import de.flapdoodle.embed.mongo.config.processlistener.ProcessListenerBuilder;
//import de.flapdoodle.embed.mongo.distribution.Version;
//import eu.europeana.enrichment.api.external.EntityClass;
//import eu.europeana.enrichment.api.external.EntityWrapper;
//import eu.europeana.enrichment.api.external.InputValue;
//import org.junit.After;
//import org.junit.Before;
//import org.junit.Test;
//
//import java.io.File;
//import java.io.IOException;
//import java.util.ArrayList;
//import java.util.List;
//
//import static org.junit.Assert.assertEquals;

/**
 * Unit test for the enrichment
 * 
 * @author Yorgos.Mamakis@ europeana.eu
 * 
 */
public class EnricherTest {
	//MongodExecutable mongodExecutable;
	//Enricher enricher = new Enricher("src/test/resources/vocabularies");

	/**
	 * Create the database if it does not exist and start Mongo Files are
	 * persisted for speed of execution of consequent tests after the database
	 * is closed
	 * 
	 * @throws Exception
	 */
//	@Before
//	public void prepare() throws Exception {
//		IMongodConfig conf;
//		if (!new File("src/test/resources/dest").exists()) {
//			conf = new MongodConfigBuilder()
//					.version(Version.V2_0_9)
//					.net(new Net(10000, false))
//					.processListener(
//							new ProcessListenerBuilder()
//									.copyDbFilesBeforeStopInto(
//											new File("src/test/resources/dest"))
//									.build()).build();
//		}
//
//		else {
//			conf = new MongodConfigBuilder()
//					.version(Version.V2_0_9)
//					.net(new Net(10000, false))
//					.processListener(
//							new ProcessListenerBuilder()
//									.copyFilesIntoDbDirBeforeStarFrom(
//											new File("src/test/resources/dest"))
//									.build()).build();
//		}
//		MongodStarter runtime = MongodStarter.getDefaultInstance();
//
//		mongodExecutable = runtime.prepare(conf);
//		try {
//			mongodExecutable.start();
//			enricher.init("Europeana", "localhost", "10000");
//		} catch (IOException e) {
//			e.printStackTrace();
//		}
//
//	}

	/**
	 * Test enrich method
	 */
//	@Test
//	public void test() {
//		List<InputValue> values = new ArrayList<InputValue>();
//
//		InputValue val1 = new InputValue();
//		val1.setOriginalField("proxy_dc_subject");
//		val1.setValue("Music");
//		List<EntityClass> entityClasses1 = new ArrayList<EntityClass>();
//		entityClasses1.add(EntityClass.CONCEPT);
//		val1.setVocabularies(entityClasses1);
//
//		InputValue val2 = new InputValue();
//		val2.setOriginalField("proxy_dc_subject");
//		val2.setValue("Ivory");
//		List<EntityClass> entityClasses2 = new ArrayList<EntityClass>();
//		entityClasses2.add(EntityClass.CONCEPT);
//		val2.setVocabularies(entityClasses2);
//
//		InputValue val3 = new InputValue();
//		val3.setOriginalField("proxy_dc_subject");
//		val3.setValue("Steel");
//		List<EntityClass> entityClasses3 = new ArrayList<EntityClass>();
//		entityClasses3.add(EntityClass.CONCEPT);
//		val3.setVocabularies(entityClasses3);
//
//		InputValue val4 = new InputValue();
//		val4.setOriginalField("proxy_dcterms_spatial");
//		val4.setValue("Paris");
//		List<EntityClass> entityClasses4 = new ArrayList<EntityClass>();
//		entityClasses4.add(EntityClass.PLACE);
//		val4.setVocabularies(entityClasses4);
//
//		InputValue val5 = new InputValue();
//		val5.setOriginalField("proxy_dc_date");
//		val5.setValue("1918");
//		List<EntityClass> entityClasses5 = new ArrayList<EntityClass>();
//		entityClasses5.add(EntityClass.TIMESPAN);
//		val5.setVocabularies(entityClasses5);
//
//		InputValue val6 = new InputValue();
//		val6.setOriginalField("proxy_dc_creator");
//		val6.setValue("Rembrandt");
//		List<EntityClass> entityClasses6 = new ArrayList<EntityClass>();
//		entityClasses6.add(EntityClass.AGENT);
//		val6.setVocabularies(entityClasses6);
//
//		values.add(val1);
//		values.add(val2);
//		values.add(val3);
//		values.add(val4);
//		values.add(val5);
//		values.add(val6);
//
//		try {
//			List<EntityWrapper> enrichments = enricher.tagExternal(values);
//			assertEquals("Enrichment size was not the same", 17,
//					enrichments.size());
//		} catch (Exception e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
//	}

	/**
	 * Close database
	 */
//	@After
//	public void destroy() {
//		mongodExecutable.stop();
//	}
}
