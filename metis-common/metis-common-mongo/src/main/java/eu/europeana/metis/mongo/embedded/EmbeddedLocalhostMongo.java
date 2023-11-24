package eu.europeana.metis.mongo.embedded;

import de.flapdoodle.embed.mongo.Command;
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.Defaults;
import de.flapdoodle.embed.mongo.config.MongodConfig;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.config.RuntimeConfig;
import de.flapdoodle.embed.process.config.io.ProcessOutput;
import de.flapdoodle.embed.process.runtime.Network;
import eu.europeana.metis.network.NetworkUtil;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Starts an in memory Mongo database. This class is to be used for unit testing on localhost.
 */
public class EmbeddedLocalhostMongo {

  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedLocalhostMongo.class);

  private static final String DEFAULT_MONGO_HOST = "127.0.0.1";
  private MongodExecutable mongodExecutable;
  private int mongoPort;

  /**
   * Constructor for default object for localhost mongo
   */
  public EmbeddedLocalhostMongo() {
    //Nothing to do
  }

  /**
   * Starts a local host mongo.
   */
  public void start() {
    if (mongodExecutable == null) {
      try {
        mongoPort = new NetworkUtil().getAvailableLocalPort();
        RuntimeConfig runtimeConfig = Defaults.runtimeConfigFor(Command.MongoD, LOGGER)
            .processOutput(ProcessOutput.getDefaultInstanceSilent())
            .build();

        MongodConfig mongodConfig = MongodConfig.builder()
            .version(Version.V4_0_12)
            .net(new Net(DEFAULT_MONGO_HOST, mongoPort, Network.localhostIsIPv6()))
            .build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
        mongodExecutable = runtime.prepare(mongodConfig);
        mongodExecutable.start();
      } catch (IOException e) {
        LOGGER.error("Exception when starting embedded mongo", e);
      }
    }
  }

  public String getMongoHost() {
    return DEFAULT_MONGO_HOST;
  }

  public int getMongoPort() {
    return mongoPort;
  }

  /**
   * Stop a previously started local host mongo.
   */
  public void stop() {
    mongodExecutable.stop();
  }
}
