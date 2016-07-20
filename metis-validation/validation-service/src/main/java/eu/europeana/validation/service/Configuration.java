package eu.europeana.validation.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import eu.europeana.validation.model.Schema;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;

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
                JsonObject element = object.getAsJsonArray("mlab").get(0).getAsJsonObject();

                JsonObject credentials = element.getAsJsonObject("credentials");
                JsonPrimitive uri = credentials.getAsJsonPrimitive("uri");
                String mongoUsername = StringUtils.substringBetween(uri.getAsString(),"mongodb://",":");

                String mongoPassword = StringUtils.substringBetween(uri.getAsString(),mongoUsername+":","@");

                String mongoHost = StringUtils.substringBetween(uri.getAsString(),mongoPassword+"@",":");

                int mongoPort = Integer.parseInt(StringUtils.substringBetween(uri.getAsString(),mongoHost+":","/"));

                String mongoDb = StringUtils.substringAfterLast(uri.getAsString(),"/");
                Logger.getGlobal().severe(mongoDb);
              //  ServerAddress address = new ServerAddress(mongoHost, mongoPort);
                MongoClient client = new MongoClient(new MongoClientURI(uri.getAsString()));
                /*MongoCredential credential =MongoCredential.createMongoCRCredential(mongoUsername,"admin",mongoPassword.toCharArray());
                List<MongoCredential> credentialList = new ArrayList<>();
                credentialList.add(credential);
                MongoClient client = new MongoClient(address,credentialList);
*/
                Morphia morphia = new Morphia();
                morphia.map(Schema.class);
                Datastore datastore = morphia.createDatastore(client, mongoDb);

                datastore.ensureIndexes();
                dao = new OpenstackSchemaDao(datastore, System.getenv("rootPath"));

                String authUri = System.getenv().get("swift_authentication_uri");
                String availabilityZone = System.getenv().get("swift_availability_zone");
                String tenantname = System.getenv("swift_tenantname");
                String username = System.getenv("swift_username");
                String password = System.getenv("swift_password");
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
