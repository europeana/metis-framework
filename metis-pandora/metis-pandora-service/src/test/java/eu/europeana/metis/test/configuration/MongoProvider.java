package eu.europeana.metis.test.configuration;

import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;

/**
 * Created by ymamakis on 6/27/16.
 */
public class MongoProvider {
    private static MongodExecutable mongodExecutable;

    public static void start(int port){
        try {
            IMongodConfig conf = new MongodConfigBuilder().version(Version.Main.V3_0)
                    .net(new Net(port, Network.localhostIsIPv6()))
                    .build();

            MongodStarter runtime = MongodStarter.getDefaultInstance();

            mongodExecutable = runtime.prepare(conf);
            mongodExecutable.start();

        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void clear() {
        mongodExecutable.cleanup();
    }
    public static void stop(){
        mongodExecutable.stop();
    }
}
