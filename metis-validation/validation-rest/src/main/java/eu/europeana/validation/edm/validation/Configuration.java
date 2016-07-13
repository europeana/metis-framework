package eu.europeana.validation.edm.validation;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mongodb.MongoClient;
import com.mongodb.MongoCredential;
import com.mongodb.ServerAddress;
import eu.europeana.validation.edm.model.Schema;
import org.apache.commons.lang3.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Created by ymamakis on 3/14/16.
 */
public class Configuration {

    private static Configuration INSTANCE;
    private static AbstractSchemaDao dao;
    private static AbstractLSResourceResolver resolver;
    private static SwiftProvider provider;

    private Configuration() {
        Properties properties = new Properties();
        try {
            if (StringUtils.isEmpty(System.getenv("VCAP_SERVICES"))) {
                properties.load(Configuration.class.getClassLoader().getResourceAsStream("validation.properties"));
                ServerAddress address = new ServerAddress(properties.getProperty("mongo.host"),
                        Integer.parseInt(properties.getProperty("mongo.port")));
                MongoClient client = new MongoClient(address);
                Morphia morphia = new Morphia();
                morphia.map(Schema.class);
                Datastore datastore = morphia.createDatastore(client, properties.getProperty("mongo.db"));
                datastore.ensureIndexes();
                dao = new SchemaDao(datastore, properties.getProperty("root.path"));
                resolver = new ClasspathResourceResolver();
            } else {

                JsonParser parser = new JsonParser();
                JsonObject object = parser.parse(System.getenv().get("VCAP_SERVICES")).getAsJsonObject();
                JsonObject element = object.getAsJsonArray("mongolab").get(0).getAsJsonObject();

                JsonObject credentials = element.getAsJsonObject("credentials");

                String mongoDb = credentials.get("db").getAsString();
                String mongoHost = credentials.get("host").getAsString();
                int mongoPort = credentials.get("port").getAsInt();
                String mongoUsername = credentials.get("username").getAsString();
                String mongoPassword = credentials.get("password").getAsString();
                ServerAddress address = new ServerAddress(mongoHost, mongoPort);
                MongoCredential credential =MongoCredential.createMongoCRCredential(mongoUsername,mongoDb,mongoPassword.toCharArray());
                List<MongoCredential> credentialList = new ArrayList<>();
                credentialList.add(credential);
                MongoClient client = new MongoClient(address,credentialList);
                Morphia morphia = new Morphia();
                morphia.map(Schema.class);
                Datastore datastore = morphia.createDatastore(client, mongoDb);
                datastore.ensureIndexes();
                dao = new OpenstackSchemaDao(datastore, System.getenv("rootPath"));


                JsonObject elementSwift = object.getAsJsonArray("swift-1.0").get(0).getAsJsonObject();
                JsonObject credentialsSwift = elementSwift.getAsJsonObject("credentials");
                String authUri = credentialsSwift.get("authentication_uri").getAsString();
                String availabilityZone = credentialsSwift.get("availability_zone").getAsString();
                String tenantname = credentialsSwift.get("tenant_name").getAsString();
                String username = credentialsSwift.get("user_name").getAsString();
                String password = credentialsSwift.get("password").getAsString();
                provider = new SwiftProvider(authUri, username, password, "schemas", availabilityZone, tenantname);
                resolver = new OpenstackResourceResolver();
                ((OpenstackResourceResolver) resolver).setProvider(provider);


                ((OpenstackSchemaDao) dao).setProvider(provider);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static Configuration getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new Configuration();
        }
        return INSTANCE;
    }

    public AbstractSchemaDao getDao() {
        return dao;
    }

    public AbstractLSResourceResolver getResolver() {
        return resolver;
    }


}
