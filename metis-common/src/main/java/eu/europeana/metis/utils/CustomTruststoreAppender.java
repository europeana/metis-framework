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
import org.apache.logging.log4j.core.net.ssl.TrustStoreConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A class with a static method to combine the default trustore with a custom trustore into one and use that during the application lifecycle.
 *
 * @author Simon Tzanakis (Simon.Tzanakis@europeana.eu)
 * @since 2018-02-06
 */
public final class CustomTruststoreAppender {

  private static final Logger LOGGER = LoggerFactory.getLogger(CustomTruststoreAppender.class);

  private CustomTruststoreAppender() {
  }

  /**
   * Will append the provided truststore to the default trustore from the system.
   *
   * @param trustorePath the path to the truststore file
   * @param trustorePassword the password to access the trustore file
   * @throws TrustStoreConfigurationException if any issue arises during the appending of the custom trustore to the default
   */
  public static void appendCustomTrustoreToDefault(String trustorePath, String trustorePassword)
      throws TrustStoreConfigurationException {

    try {
      X509TrustManager defaultX509TrustManager = getDefaultX509TrustManager();
      X509TrustManager customX509TrustManager = getCustomX509TrustManager(trustorePath,
          trustorePassword);

      X509TrustManager mergedX509TrustManager = new CustomX509TrustManager(defaultX509TrustManager,
          customX509TrustManager);

      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, new TrustManager[]{mergedX509TrustManager}, null);
      SSLContext.setDefault(sslContext);
    } catch (NoSuchAlgorithmException | KeyStoreException | IOException | CertificateException | KeyManagementException e) {
      throw new TrustStoreConfigurationException(e);
    }
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
      if (trustManager instanceof X509TrustManager) {
        defaultX509TrustManager = (X509TrustManager) trustManager;
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
      if (trustManager instanceof X509TrustManager) {
        customX509TrustManager = (X509TrustManager) trustManager;
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
        LOGGER.warn(
            "Custom x509TrustManager did not have trusted certificates for the accessible resource, will try default x509TrustManager now");
        x509TrustManager.checkServerTrusted(chain, authType);
      }
    }

    @Override
    public void checkClientTrusted(X509Certificate[] chain,
        String authType) throws CertificateException {
      // If you're planning to use client-cert auth,
      // do the same as checking the server.
      x509TrustManager.checkClientTrusted(chain, authType);
    }
  }

}
