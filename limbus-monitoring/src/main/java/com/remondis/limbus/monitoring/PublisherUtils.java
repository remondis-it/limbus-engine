package com.remondis.limbus.monitoring;

import java.net.InetAddress;
import java.net.UnknownHostException;

import com.remondis.limbus.utils.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a util class for useful methods supporting the development of publisher implemnentations.
 *
 * @author schuettec
 *
 */
public class PublisherUtils {

  private static final Logger log = LoggerFactory.getLogger(PublisherUtils.class);

  private static final ThreadLocal<ClientContext> currentClientContext = new ThreadLocal<ClientContext>();

  private static String HOSTNAME;

  protected static void logPublisherCallFailed(Object publisherInstance, Throwable e) {
    log.warn(String.format("Processing a monitoring record with publisher '%s' failed.", publisherInstance.getClass()
        .getName()), e);
  }

  /**
   * Returns the {@link ClientContext} of the client that called a publisher method. This method can only be used in
   * publisher
   * implementations while the publisher method is executing.
   *
   * @return Returns the {@link ClientContext}.
   */
  public static ClientContext getClientContext() {
    return currentClientContext.get();
  }

  /**
   * Sets the {@link ClientContext} of the client that called a publisher method.
   *
   * @param callId
   *        The {@link ClientContext} to set.
   */
  protected static void setClientContext(ClientContext callId) {
    currentClientContext.set(callId);
  }

  /**
   * Clears the {@link ClientContext} after the publisher implementation call was performed.
   */
  protected static void removeClientContext() {
    currentClientContext.remove();
  }

  /**
   * Throws an {@link IllegalArgumentException} if the specified value is null for a provider configuration.
   *
   * @param fieldName
   *        The name of the field
   * @param reference
   *        The value of the reference.
   * @throws IllegalArgumentException
   *         Thrown if the
   */
  public static void denyRequired(String fieldName, Object reference) throws IllegalArgumentException {
    if (reference == null) {
      throw new IllegalArgumentException(
          String.format("Field '%s' in provider configuration is not set but required.", fieldName));
    }
  }

  /**
   * @return Returns the local host name that can be used while publishing monitoring events, to group the information
   *         by hostname.
   */
  public static String getLocalHostname() {
    String defaultValue = "UNKNOWN";
    try {
      if (HOSTNAME == null) {
        HOSTNAME = Lang.defaultIfNull(InetAddress.getLocalHost()
            .getHostName(), defaultValue);
      }
      return HOSTNAME;
    } catch (UnknownHostException e) {
      log.warn("Monitoring system failed to resolve the local hostname.", e);
      return defaultValue;
    } catch (SecurityException e) {
      log.warn("Monitoring system failed to resolve the local hostname due to missing runtime permissions.", e);
      return defaultValue;
    }
  }

}
