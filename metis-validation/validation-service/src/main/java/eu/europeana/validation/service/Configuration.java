/*
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
package eu.europeana.validation.service;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.mongodb.MongoClient;
import com.mongodb.MongoClientURI;
import com.mongodb.ServerAddress;
import eu.europeana.features.ObjectStorageClient;
import eu.europeana.features.S3ObjectStorageClient;
import eu.europeana.features.SwiftObjectStorageClient;
import eu.europeana.validation.model.Schema;
import java.io.IOException;
import java.util.Properties;
import java.util.logging.Logger;
import org.apache.commons.lang.StringUtils;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.Morphia;

/**
 * Configuration class
 * Created by ymamakis on 3/14/16.
 */
public class Configuration {

  private static Configuration INSTANCE;
  private static AbstractSchemaDao dao;
  private static AbstractLSResourceResolver resolver;
  private static ObjectStorageClient objectStorageClient;

  private Configuration() {
    Properties properties = new Properties();
    try {
      if (StringUtils.isEmpty(System.getenv("VCAP_SERVICES"))) {
        properties.load(
            Configuration.class.getClassLoader().getResourceAsStream("validation.properties"));
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
        String mongoUsername = StringUtils.substringBetween(uri.getAsString(), "mongodb://", ":");

        String mongoPassword = StringUtils
            .substringBetween(uri.getAsString(), mongoUsername + ":", "@");

        String mongoHost = StringUtils
            .substringBetween(uri.getAsString(), mongoPassword + "@", ":");

        int mongoPort = Integer
            .parseInt(StringUtils.substringBetween(uri.getAsString(), mongoHost + ":", "/"));

        String mongoDb = StringUtils.substringAfterLast(uri.getAsString(), "/");
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
        dao = new ObjectStorageSchemaDao(datastore, System.getenv("rootPath"));
        if (System.getenv("swift_authentication_uri") != null) {
          String authUri = System.getenv().get("swift_authentication_uri");
          String availabilityZone = System.getenv().get("swift_availability_zone");
          String tenantname = System.getenv("swift_tenantname");
          String username = System.getenv("swift_username");
          String password = System.getenv("swift_password");

          objectStorageClient = new SwiftObjectStorageClient(authUri, username, password,
              "rootPath", availabilityZone, tenantname);
        } else {
          String clientKey = System.getenv("s3_client_key");
          String secretKey = System.getenv("s3_secret_key");
          String region = System.getenv("s3_region");
          String bucket = System.getenv("s3_bucket");
          objectStorageClient = new S3ObjectStorageClient(clientKey, secretKey, region, bucket);
        }
        resolver = new ObjectStorageResourceResolver();
        ((ObjectStorageResourceResolver) resolver).setClient(objectStorageClient);

        ((ObjectStorageSchemaDao) dao).setClient(objectStorageClient);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private Configuration(String mongoHost, int mongoPort, String mongoDb, String rootPath) {
    ServerAddress address = new ServerAddress(mongoHost, mongoPort);
    MongoClient client = new MongoClient(address);
    Morphia morphia = new Morphia();
    morphia.map(Schema.class);
    Datastore datastore = morphia.createDatastore(client, mongoDb);
    datastore.ensureIndexes();
    dao = new SchemaDao(datastore, rootPath);
    resolver = new ClasspathResourceResolver();
  }

  public static Configuration getInstance() {
    if (INSTANCE == null) {
      INSTANCE = new Configuration();
    }
    return INSTANCE;
  }

  public static Configuration getInstance(String mongoHost, int mongoPort, String mongoDb, String rootPath) {
    if (INSTANCE == null) {
      INSTANCE = new Configuration(mongoHost, mongoPort, mongoDb, rootPath);
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
