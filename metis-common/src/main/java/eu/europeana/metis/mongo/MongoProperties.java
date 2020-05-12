package eu.europeana.metis.mongo;

import com.mongodb.MongoCredential;
import com.mongodb.ReadPreference;
import com.mongodb.ServerAddress;
import eu.europeana.metis.utils.InetAddressUtil;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.function.Function;
import java.util.function.Supplier;
import org.apache.commons.lang3.StringUtils;

/**
 * This class holds all the properties that are required to set up a Mongo DB connection.
 *
 * @param <E> The type of exception thrown when the properties are not valid.
 */
public class MongoProperties<E extends Exception> {

  // Exception creator
  private final Function<String, E> exceptionCreator;

  // Mongo connection properties
  private final List<ServerAddress> mongoHosts = new ArrayList<>();
  private MongoCredential mongoCredentials;
  private boolean mongoEnableSsl;
  private ReadPreferenceValue readPreferenceValue;

  /**
   * Constructor.
   *
   * @param exceptionCreator Function for creating exception from a given message.
   */
  public MongoProperties(Function<String, E> exceptionCreator) {
    this.exceptionCreator = exceptionCreator;
  }

  /**
   * Setter for all properties.
   *
   * @param hosts The hosts. This cannot be null or empty.
   * @param ports The ports. This cannot be null or empty. Must contain either the same number of
   * elements as the hosts array, or exactly 1 element (which will then apply to all hosts).
   * @param authenticationDatabase The name of the authentication database. Can be null, in which
   * case no authentication takes place.
   * @param username The username. Can be null, in which case no authentication takes place.
   * @param password The password. Can be null, in which case no authentication takes place.
   * @param enableSsl Whether to enable SSL connections.
   * @param readPreferenceValue The read preference. Can be null, where then the default applies
   * @throws E In case either of the arrays is null, or their lengths don't match.
   */
  public void setAllProperties(String[] hosts, int[] ports, String authenticationDatabase,
      String username, String password, boolean enableSsl, ReadPreferenceValue readPreferenceValue)
      throws E {

    // Set the hosts.
    final List<ServerAddress> addresses = new InetAddressUtil<>(this.exceptionCreator)
        .getMongoAddressesFromHostsAndPorts(nonNull(hosts, "hosts"), nonNull(ports, "ports"));
    mongoHosts.clear();
    for (ServerAddress address : nonNull(addresses, "addresses")) {
      mongoHosts.add(nonNull(address, "address"));
    }

    // Compile the credentials
    if (StringUtils.isBlank(authenticationDatabase) || StringUtils.isBlank(username) ||
        StringUtils.isBlank(password)) {
      this.mongoCredentials = null;
    } else {
      this.mongoCredentials = MongoCredential
          .createCredential(username, authenticationDatabase, password.toCharArray());
    }

    // Set the other properties
    this.mongoEnableSsl = enableSsl;
    setReadPreferenceValue(readPreferenceValue);
  }

  /**
   * Add a Mongo host. This method must be called at least once.
   *
   * @param host Mongo host.
   * @throws E In case the provided value is null.
   */
  public void addMongoHost(InetSocketAddress host) throws E {
    mongoHosts.add(new ServerAddress(nonNull(host, "host")));
  }

  /**
   * Set Mongo credentials. This method is optional: by default, there are no credentials set.
   *
   * @param username Username.
   * @param password Password.
   * @param authenticationDatabase The authentication database where the user is known.
   * @throws E In case any of the provided values are null.
   */
  public void setMongoCredentials(String username, String password, String authenticationDatabase)
      throws E {
    this.mongoCredentials = MongoCredential.createCredential(nonNull(username, "username"),
        nonNull(authenticationDatabase, "authenticationDatabase"),
        nonNull(password, "password").toCharArray());
  }

  /**
   * Enable SSL for the Mongo connection. This method is optional: by default this is disabled.
   */
  public void setMongoEnableSsl() {
    this.mongoEnableSsl = true;
  }

  /**
   * Set the read preference value. Can be null, where then the default applies
   *
   * @param readPreferenceValue the read preference value
   */
  public void setReadPreferenceValue(ReadPreferenceValue readPreferenceValue) {
    //Secondary preferred as default
    this.readPreferenceValue = Optional.ofNullable(readPreferenceValue)
        .orElse(ReadPreferenceValue.SECONDARY_PREFERRED);
  }

  private <T> T nonNull(T value, String fieldName) throws E {
    if (value == null) {
      throw exceptionCreator.apply(String.format("Value '%s' cannot be null.", fieldName));
    }
    return value;
  }

  /**
   * This method returns the list of Mongo hosts.
   *
   * @return The Mongo hosts.
   * @throws E In case no such hosts were set.
   */
  public List<ServerAddress> getMongoHosts() throws E {
    if (mongoHosts.isEmpty()) {
      throw exceptionCreator.apply("Please provide at least one Mongo host.");
    }
    return Collections.unmodifiableList(mongoHosts);
  }

  /**
   * This method returns the Mongo credentials.
   *
   * @return The credentials, or null if no such credentials were set.
   */
  public MongoCredential getMongoCredentials() {
    return mongoCredentials;
  }

  /**
   * This method returns whether SSL is to be enabled for the Mongo connection.
   *
   * @return Whether SSL is to be enabled for the Mongo connection.
   */
  public boolean mongoEnableSsl() {
    return mongoEnableSsl;
  }

  /**
   * This method returns the value of the read preference
   *
   * @return the read preference set
   */
  public ReadPreferenceValue getReadPreferenceValue() {
    return readPreferenceValue;
  }

  /**
   * Enum for read preference values
   */
  public enum ReadPreferenceValue {
    PRIMARY(ReadPreference::primary),
    PRIMARY_PREFERRED(ReadPreference::primaryPreferred),
    SECONDARY(ReadPreference::secondary),
    SECONDARY_PREFERRED(ReadPreference::secondaryPreferred),
    NEAREST(ReadPreference::nearest);

    private Supplier<ReadPreference> readPreferenceSupplier;

    ReadPreferenceValue(Supplier<ReadPreference> readPreferenceSupplier) {
      this.readPreferenceSupplier = readPreferenceSupplier;
    }

    public Supplier<ReadPreference> getReadPreferenceSupplier() {
      return readPreferenceSupplier;
    }
  }
}
