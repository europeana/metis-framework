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

    public static void stop(){
        mongodExecutable.stop();
    }
}
