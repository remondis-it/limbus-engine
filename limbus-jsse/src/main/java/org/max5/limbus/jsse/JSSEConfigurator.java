package org.max5.limbus.jsse;

import java.security.cert.X509Certificate;
import java.util.Iterator;
import java.util.Properties;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import org.max5.limbus.Initializable;
import org.max5.limbus.LimbusProperties;
import org.max5.limbus.files.LimbusFileService;
import org.max5.limbus.system.LimbusComponent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This component configures the JSSE via {@link LimbusProperties}. Use the default Limbus configuration mechanism to
 * override the JSSE default configuration. <b>This
 * component
 * assumes, that no SSL context was requested before.</b> If an SSL context was initialized before this component, the
 * SSL connections are not affected by the configuration loaded by this component.
 *
 * <h2>Configuring JSSE</h2>
 * <p>
 * The configuration keys used in the Limbus configuration file are the same as documented by JSSE. One exception is the
 * key {@value #LIMBUS_SSL_TRUST_ALL}. If this key is set to <code>true</code> a new SSL context is created with the
 * following configuration:
 * <ul>
 * <li>A trust manager that will trust all certificates</li>
 * <li>An empty keystore</li>
 * <li>A hostname verifier that will accept all hostnames</li>
 * </ul>
 * <b>Do not use this configuration
 * in production! Trusting all certificates is only
 * intended to be used in development environments.</b>
 * </p>
 *
 * @see <a href="http://docs.oracle.com/javase/8/docs/technotes/guides/security/jsse/JSSERefGuide.html#Customization">
 *      JSSE Configuration keys.</a>
 * @author schuettec
 *
 */
public class JSSEConfigurator extends Initializable<Exception> {

  /**
   * Configuration key to trust all certificates.
   */
  public static final String LIMBUS_SSL_TRUST_ALL = "limbus.ssl.trustAll";

  private static final Logger log = LoggerFactory.getLogger(JSSEConfigurator.class);

  @LimbusComponent
  private LimbusFileService filesystem;

  private LimbusProperties config;

  /**
   * Holds the socket factory that was active before the trust all mode was enabled.
   */
  private SSLSocketFactory sslSocketFactoryOld;
  /**
   * Holds the hostname verifier that was active before the trust all mode was enabled.
   */
  private HostnameVerifier hostnameVerifierOld;

  public JSSEConfigurator() {
  }

  @Override
  protected void performInitialize() throws Exception {
    // Do not expect a default configuration because JSSE has itself a default configuration
    this.config = new LimbusProperties(filesystem, JSSEConfigurator.class, false, false);

    // If a custom configuration was specified, setup JSSE
    if (hasCustomConfig()) {
      log.info("Configuration for Java Secure Socket Extension was found.");
      configureJSSE();
    } else {
      log.info("Using default configuration for Java Secure Socket Extension.");
    }

  }

  private void configureJSSE() throws Exception {
    if (isTrustAll()) {
      saveConfigurationBefore();
      trustAllCertificates();
      log.warn("SSL is currently configured to trust all certificates and bypass hostname verification.");
      log.warn("Do not use this configuration in production!");
    } else {
      applyProperties();
    }
  }

  private void applyProperties() {
    // schuettec - 11.01.2017 : Until a better way is found, we only apply the system properties specified in the
    // configuration.
    Properties properties = this.config.getProperties();
    Iterator<Object> it = properties.keySet()
        .iterator();
    while (it.hasNext()) {
      Object key = it.next();
      Object value = properties.get(key);
      System.setProperty((String) key, (String) value);
    }
  }

  private boolean isTrustAll() {
    return config.containsKey(LIMBUS_SSL_TRUST_ALL) && config.getBoolean(LIMBUS_SSL_TRUST_ALL);
  }

  private void trustAllCertificates() throws Exception {
    TrustManager[] trustAllCerts = new TrustManager[] {
        new X509TrustManager() {
          @Override
          public java.security.cert.X509Certificate[] getAcceptedIssuers() {
            return null;
          }

          @Override
          public void checkClientTrusted(X509Certificate[] certs, String authType) {
          }

          @Override
          public void checkServerTrusted(X509Certificate[] certs, String authType) {
          }
        }
    };

    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, trustAllCerts, new java.security.SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

    // Create all-trusting host name verifier
    HostnameVerifier allHostsValid = new HostnameVerifier() {

      @Override
      public boolean verify(String hostname, SSLSession session) {
        return true;
      }
    };
    // Install the all-trusting host verifier
    HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
  }

  private void saveConfigurationBefore() {
    this.sslSocketFactoryOld = HttpsURLConnection.getDefaultSSLSocketFactory();
    this.hostnameVerifierOld = HttpsURLConnection.getDefaultHostnameVerifier();
  }

  private void restoreConfigurationBefore() {
    HttpsURLConnection.setDefaultSSLSocketFactory(sslSocketFactoryOld);
    HttpsURLConnection.setDefaultHostnameVerifier(hostnameVerifierOld);
  }

  private boolean hasCustomConfig() {
    return !config.isEmpty();
  }

  @Override
  protected void performFinish() {
    // If trust-all mode was active, rollback its changes
    if (isTrustAll()) {
      restoreConfigurationBefore();
    }
  }

}
