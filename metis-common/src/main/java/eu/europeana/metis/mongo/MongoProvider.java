package eu.europeana.metis.mongo;/*
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

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.RuntimeConfigBuilder;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.IRuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * Created by ymamakis on 3/17/16.
 */
public class MongoProvider {

  private MongodExecutable mongodExecutable;
  private final Logger LOGGER = LoggerFactory.getLogger(MongoProvider.class);

  public void start(int port) {
    if (mongodExecutable == null) {
      try {
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
            .defaultsWithLogger(Command.MongoD, LOGGER)
            .processOutput(ProcessOutput.getDefaultInstanceSilent())
            .build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
        mongodExecutable = runtime.prepare(new MongodConfigBuilder().version(Version.V3_5_1)
            .net(new Net("localhost", port, Network.localhostIsIPv6())).build());
        mongodExecutable.start();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void stop() {
    mongodExecutable.stop();
  }
}
