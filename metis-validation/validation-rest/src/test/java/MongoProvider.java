
import de.flapdoodle.embed.mongo.MongodExecutable;
import de.flapdoodle.embed.mongo.MongodStarter;
import de.flapdoodle.embed.mongo.config.IMongodConfig;
import de.flapdoodle.embed.mongo.config.MongodConfigBuilder;
import de.flapdoodle.embed.mongo.config.Net;
import de.flapdoodle.embed.mongo.distribution.Version;
import de.flapdoodle.embed.process.runtime.Network;

import java.io.IOException;

/**
 * Created by ymamakis on 3/17/16.
 */
public class MongoProvider {
    static MongodExecutable mongodExecutable;
    public static void start(){

        try {
            int port = 10000;
            IMongodConfig conf = new MongodConfigBuilder().version(Version.Main.V3_0)
                    .net(new Net(port, Network.localhostIsIPv6()))
                    .build();
//					(Version.V2_0_7, port, false);

            MongodStarter runtime = MongodStarter.getDefaultInstance();

            mongodExecutable = runtime.prepare(conf);
            mongodExecutable.start();
        } catch (IOException e){
            e.printStackTrace();
        }
    }

    public static void stop(){
        mongodExecutable.stop();
    }
}
