package eu.europeana.metis.mongo;

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
import eu.europeana.metis.utils.NetworkUtil;
import java.io.IOException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Created by ymamakis on 3/17/16.
 */
public class EmbeddedLocalhostMongo {

  public static final String DEFAULT_MONGO_HOST = "127.0.0.1";
  private MongodExecutable mongodExecutable;
  private final String mongoHost;
  private int mongoPort;
  private static final Logger LOGGER = LoggerFactory.getLogger(EmbeddedLocalhostMongo.class);

  /**
   * Constructor for default object for localhost mongo
   */
  public EmbeddedLocalhostMongo() {
    mongoHost = "127.0.0.1";
  }

  /**
   * Constructor with option to provide a custom monog host ip if required.
   *
   * @param mongoHost the ip of the mongo host that should be used
   */
  public EmbeddedLocalhostMongo(String mongoHost) {
    this.mongoHost = mongoHost;
  }

  /**
   * Starts a local host mongo.
   */
  public void start() {
    if (mongodExecutable == null) {
      try {
        mongoPort = NetworkUtil.getAvailableLocalPort();
        IRuntimeConfig runtimeConfig = new RuntimeConfigBuilder()
            .defaultsWithLogger(Command.MongoD, LOGGER)
            .processOutput(ProcessOutput.getDefaultInstanceSilent())
            .build();

        MongodStarter runtime = MongodStarter.getInstance(runtimeConfig);
        mongodExecutable = runtime.prepare(new MongodConfigBuilder().version(Version.V3_5_1)
            .net(new Net(mongoHost, mongoPort, Network.localhostIsIPv6())).build());
        mongodExecutable.start();
      } catch (IOException e) {
        LOGGER.error("Exception when starting embedded mongo", e);
      }
    }
  }

  public String getMongoHost() {
    return mongoHost;
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
