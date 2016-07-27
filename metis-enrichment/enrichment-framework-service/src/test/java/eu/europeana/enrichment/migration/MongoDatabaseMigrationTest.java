package eu.europeana.enrichment.migration;

import java.net.UnknownHostException;

import org.junit.Ignore;
import org.junit.Test;

import eu.europeana.enrichment.api.internal.ConceptTermList;

public class MongoDatabaseMigrationTest {

	@Test
	@Ignore
	public void testMigrateAll() throws UnknownHostException {
		MongoDatabaseMigration mongoDatabaseMigration = new MongoDatabaseMigration("136.243.103.29", 27017, "annocultor_db", "136.243.103.29", 27017, "annocultor_migration_db");
		mongoDatabaseMigration.migrateAllPhase();
	}
	
	
	@Test
	@Ignore
	public void testFindOneHavingTerms() throws UnknownHostException {
		MongoDatabaseMigration mongoDatabaseMigration = new MongoDatabaseMigration("136.243.103.29", 27017, "annocultor_db", "136.243.103.29", 27017, "annocultor_migration_db");
		ConceptTermList conceptTermList = mongoDatabaseMigration.findOneHavingTerms();
		if (conceptTermList!=null) {
			System.out.println(conceptTermList.getCodeUri());
		}
		else {
			System.out.println("No ConceptTermList with terms refs was found");
		}
	}

	@Test
	@Ignore
	public void testCreateSequence() throws UnknownHostException {
		MongoDatabaseMigration mongoDatabaseMigration = new MongoDatabaseMigration("136.243.103.29", 27017, "annocultor_db", "136.243.103.29", 27017, "annocultor_migration_db");
		mongoDatabaseMigration.createSequence(5433, 165087, 140094, 2530);
	}
	
	

	
}
