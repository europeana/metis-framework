package eu.europeana.metis.mongo.embedded;


import de.flapdoodle.embed.mongo.commands.ImmutableMongodArguments;
import de.flapdoodle.embed.mongo.commands.MongodArguments;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.transitions.ImmutableMongod;
import de.flapdoodle.embed.mongo.transitions.Mongod;
import de.flapdoodle.embed.mongo.transitions.RunningMongodProcess;
import de.flapdoodle.embed.process.io.ImmutableProcessOutput;
import de.flapdoodle.embed.process.io.ProcessOutput;
import de.flapdoodle.embed.process.io.Processors;
import de.flapdoodle.embed.process.io.Slf4jLevel;
import de.flapdoodle.reverse.TransitionWalker;
import de.flapdoodle.reverse.transitions.Start;
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
  private static final ImmutableProcessOutput processOutput = ProcessOutput.builder()
                                                                           .commands(Processors.logTo(LOGGER, Slf4jLevel.DEBUG))
                                                                           .output(Processors.logTo(LOGGER, Slf4jLevel.INFO))
                                                                           .error(Processors.logTo(LOGGER, Slf4jLevel.ERROR))
                                                                           .build();

  private static final ImmutableMongodArguments mongodArguments = MongodArguments.defaults()
                                                                                 .withSyncDelay(0)
                                                                                 .withStorageEngine("ephemeralForTest")
                                                                                 .withUseNoJournal(true);

  private TransitionWalker.ReachedState<RunningMongodProcess> runningMongodProcessReachedState;
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
    if (runningMongodProcessReachedState == null) {
      try {
        mongoPort = new NetworkUtil().getAvailableLocalPort();
        ImmutableMongod mongod = Mongod.instance()
                                       .withNet(Start.to(Net.class).initializedWith(
                                           Net.builder().bindIp(DEFAULT_MONGO_HOST).port(mongoPort).isIpv6(true).build()))
                                       .withProcessOutput(Start.to(ProcessOutput.class).initializedWith(processOutput))
                                       .withMongodArguments(Start.to(MongodArguments.class).initializedWith(mongodArguments));

        runningMongodProcessReachedState = mongod.start(Version.Main.V4_4);

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
    runningMongodProcessReachedState.close();
  }
}
