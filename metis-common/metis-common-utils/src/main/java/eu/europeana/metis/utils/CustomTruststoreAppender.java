package eu.europeana.metis.utils;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class with a static method to combine the default truststore with a custom truststore into one and use that during the
 * application lifecycle.
 */
public final class CustomTruststoreAppender {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomTruststoreAppender.class);

  private CustomTruststoreAppender() {
  }

  /**
   * Will append the provided truststore to the default truststore from the system.
   *
   * @param path the path to the truststore file
   * @param password the password to access the truststore file
   * @throws TrustStoreConfigurationException if any issue arises during the appending of the custom truststore to the default
   */
  public static void appendCustomTruststoreToDefault(String path, String password)
      throws TrustStoreConfigurationException {

    try {
      X509TrustManager defaultX509TrustManager = getDefaultX509TrustManager();
      X509TrustManager customX509TrustManager = getCustomX509TrustManager(path,
          password);

      X509TrustManager mergedX509TrustManager = new CustomX509TrustManager(defaultX509TrustManager,
          customX509TrustManager);

      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, new TrustManager[]{mergedX509TrustManager}, null);
      SSLContext.setDefault(sslContext);
    } catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException | KeyManagementException e) {
      throw new TrustStoreConfigurationException(e);
    }
  }

  /**
   * Will append the provided truststore to the default truststore from the system.
   *
   * @param path the path to the truststore file
   * @param password the password to access the truststore file
   * @throws TrustStoreConfigurationException if any issue arises during the appending of the custom truststore to the default
   * @
   * @deprecated Use {@link #appendCustomTruststoreToDefault(String, String)}
   */
  // TODO: 25/08/2023 Remove this when version >= 12-SNAPSHOT
  @Deprecated(since = "11", forRemoval = true)
  public static void appendCustomTrustoreToDefault(String path, String password)
      throws TrustStoreConfigurationException {
    CustomTruststoreAppender.appendCustomTruststoreToDefault(path, password);
  }

  private static X509TrustManager getDefaultX509TrustManager()
      throws NoSuchAlgorithmException, KeyStoreException {
    TrustManagerFactory trustManagerFactory = TrustManagerFactory
        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
    // Using null here initialises the TMF with the default trust store.
    trustManagerFactory.init((KeyStore) null);

    // Get hold of the default trust manager
    X509TrustManager defaultX509TrustManager = null;
    for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
      if (trustManager instanceof X509TrustManager x509TrustManager) {
        defaultX509TrustManager = x509TrustManager;
        break;
      }
    }
    return defaultX509TrustManager;
  }

  private static X509TrustManager getCustomX509TrustManager(String truststorePath,
      String truststorePassword)
      throws IOException, KeyStoreException, CertificateException, NoSuchAlgorithmException {
    KeyStore customTrustStore;
    try (InputStream truststoreInputStream = Files.newInputStream(Paths.get(truststorePath))) {
      customTrustStore = KeyStore.getInstance(KeyStore.getDefaultType());
      customTrustStore.load(truststoreInputStream, truststorePassword.toCharArray());
    }

    TrustManagerFactory trustManagerFactory = TrustManagerFactory
        .getInstance(TrustManagerFactory.getDefaultAlgorithm());
    trustManagerFactory.init(customTrustStore);

    X509TrustManager customX509TrustManager = null;
    for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
      if (trustManager instanceof X509TrustManager x509TrustManager) {
        customX509TrustManager = x509TrustManager;
        break;
      }
    }
    return customX509TrustManager;
  }

  private static class CustomX509TrustManager implements X509TrustManager {

    private final X509TrustManager x509TrustManager;
    private final X509TrustManager x509TrustManagerToBeMerged;

    CustomX509TrustManager(X509TrustManager x509TrustManager,
        X509TrustManager x509TrustManagerToBeMerged) {
      this.x509TrustManager = x509TrustManager;
      this.x509TrustManagerToBeMerged = x509TrustManagerToBeMerged;
    }

    @Override
    public X509Certificate[] getAcceptedIssuers() {
      // If you're planning to use client-cert auth,
      // merge results from "defaultTm" and "myTm".
      return x509TrustManager.getAcceptedIssuers();
    }

    @Override
    public void checkServerTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      try {
        x509TrustManagerToBeMerged.checkServerTrusted(chain, authType);
      } catch (CertificateException e) {
        LOGGER.debug("No custom trusted certificate found", e);
        LOGGER.warn(
            "Custom x509TrustManager did not have trusted certificates for the accessible resource, will try default x509TrustManager now");
        x509TrustManager.checkServerTrusted(chain, authType);
      }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain, String authType)
        throws CertificateException {
      // If you're planning to use client-cert auth,
      // do the same as checking the server.
      x509TrustManager.checkClientTrusted(chain, authType);
    }
  }

  /**
   * In case a problem occurs with the truststore configuration
   */
  public static class TrustStoreConfigurationException extends Exception {

    private static final long serialVersionUID = -6498227689619898437L;

    TrustStoreConfigurationException(final Exception e) {
      super(e);
    }
  }
}
