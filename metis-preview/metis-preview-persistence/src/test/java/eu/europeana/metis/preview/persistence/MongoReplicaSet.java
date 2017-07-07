package eu.europeana.metis.preview.persistence;

import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.IMongosConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.MongosConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.config.Storage;
import de.flapdoodle.embed.mongo.config.Timeout;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.mongo.tests.MongosSystemForTestFactory;
import de.flapdoodle.embed.process.runtime.Network;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by ymamakis on 9/5/16.
 */
public class MongoReplicaSet {
    static MongosSystemForTestFactory mongosSystemForTestFactory;

    public static void start(int port) {

       
        try {
            Storage replication = new Storage(null, "repset1", 0);
            IMongodConfig conf1 = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                    .net(new Net("127.0.0.1",port, Network.localhostIsIPv6())).replication(replication).configServer(false).timeout(new Timeout(50 * 60 * 1000))
                    .build();
            IMongodConfig conf2 = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                    .net(new Net("127.0.0.1",port + 1, Network.localhostIsIPv6())).replication(replication).configServer(false).timeout(new Timeout(50 * 60 * 1000))
                    .build();
            IMongodConfig conf3 = new MongodConfigBuilder().version(Version.Main.PRODUCTION)
                    .net(new Net("127.0.0.1",port + 2, Network.localhostIsIPv6())).replication(replication).configServer(false).timeout(new Timeout(50 * 60 * 1000))
                    .build();

            Map<String, List<IMongodConfig>> repSet = new HashMap<>();
            List<IMongodConfig> repConfig = new ArrayList<>();
            repConfig.add(conf1);
            repConfig.add(conf2);
            repConfig.add(conf3);
            repSet.put("repset1", repConfig);
            IMongosConfig config = new MongosConfigBuilder().version(Version.Main.PRODUCTION).net(new Net(port + 3, Network.localhostIsIPv6())).
                    configDB("127.0.0.1:10004").timeout(new Timeout(50 * 60 * 1000)) .build();



            IMongodConfig mongodConfig = new MongodConfigBuilder()
                    .version(Version.Main.PRODUCTION)
                    .net(new Net("127.0.0.1",port + 4, Network.localhostIsIPv6())).timeout(new Timeout(50 * 60 * 1000))
                    .configServer(true).build();
            List<IMongodConfig> configServer = new ArrayList<>();
            configServer.add(mongodConfig);
            mongosSystemForTestFactory = new MongosSystemForTestFactory(config, repSet, configServer, "test_db", "record", "about");

            //TODO // FIXME: 9/6/16 Disabling the deletion because of the Mongo replication failing in flapdoodle currently
            //mongosSystemForTestFactory.start();


        } catch (IOException e) {
            e.printStackTrace();
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }

    public static void stop() {
        mongosSystemForTestFactory.stop();
    }
}
