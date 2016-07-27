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
package eu.europeana.enrichment.migration;

import java.net.UnknownHostException;

/**
 * @author hgeorgiadis
 *
 */
public class MigrateMain {

	public static void main(String[] args) {
		/*String sourceHost;
		int sourcePort = 0;
		String sourceDBName;
		String targetHost;
		int targetPort = 0;
		String targetDBName;

		if (args == null || args.length != 6) {
			System.out.println("Please provide with the following 6 arguments:  source mongo host (string), "
					+ "source mongo port (int), source mongo DB name (string), target mongo host (string), target mongo port (int), target mongo DB name (string)");
			return;
		}

		sourceHost = args[0];
		try {
			sourcePort = Integer.parseInt(args[1]);
		} catch (NumberFormatException e) {
			System.out.println(
					"Argument #2 is a source mongo port and must be an integer. '" + args[1] + "' is not an integer");
		}
		sourceDBName = args[2];

		targetHost = args[3];
		try {
			targetPort = Integer.parseInt(args[4]);
		} catch (NumberFormatException e) {
			System.out.println(
					"Argument #5 is a source mongo port and must be an integer. '" + args[4] + "' is not an integer");
		}
		targetDBName = args[5];
*/

		String sourceHost="172.17.0.2";
		int sourcePort = 27017;
		String sourceDBName="annocultor_db";
		String targetHost="172.17.0.2";
		int targetPort = 27017;
		String targetDBName="annocultor_db_new";
		MongoDatabaseMigration mongoDatabaseMigration = null;
		try {
			mongoDatabaseMigration = new MongoDatabaseMigration(sourceHost, sourcePort, sourceDBName, targetHost,
					targetPort, targetDBName);
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		try {
			mongoDatabaseMigration.migrateAllPhase();
		} catch (UnknownHostException e) {
			e.printStackTrace();
			return;
		}
		System.out.println("Migration was finished successfully!");
	}

}
