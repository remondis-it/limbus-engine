package com.remondis.limbus.monitoring;

import java.net.MalformedURLException;
import java.util.Iterator;
import java.util.List;

/**
 * Thrown if the monitoring configuration is invalid.
 *
 * @author schuettec
 *
 */
public class InvalidConfigurationException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  private InvalidConfigurationException() {
  }

  private InvalidConfigurationException(String message) {
    super(message);
  }

  private InvalidConfigurationException(Throwable cause) {
    super(cause);
  }

  private InvalidConfigurationException(String message, Throwable cause) {
    super(message, cause);
  }

  private InvalidConfigurationException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  protected static InvalidConfigurationException duplicatePublisherId(String id) {
    return new InvalidConfigurationException(String.format("Duplicate publisher id in configuration '%s'!", id));
  }

  protected static InvalidConfigurationException duplicatePublisher(Class<?> clazz) {
    return new InvalidConfigurationException(
        String.format("Duplicate publisher in configuration '%s'!", clazz.getName()));
  }

  protected static InvalidConfigurationException duplicatePatterns(List<Pattern> delta) {
    StringBuilder b = new StringBuilder();
    Iterator<Pattern> iterator = delta.iterator();
    while (iterator.hasNext()) {
      Pattern duplicate = iterator.next();
      b.append("'")
          .append(duplicate.getPattern())
          .append("'");
      if (iterator.hasNext()) {
        b.append(", ");
      }
    }
    return new InvalidConfigurationException(String.format("Duplicate patterns in configuration: %s!", b.toString()));
  }

  protected static InvalidConfigurationException unknownPublisherId(String publisherId) {
    return new InvalidConfigurationException(
        String.format("The specified publisher reference on id '%s' was not declared!", publisherId));
  }

  protected static InvalidConfigurationException readError(String property, Exception e) {
    return new InvalidConfigurationException(
        String.format("Cannot read specified configuration from system property url: '%s'", property), e);
  }

  protected static InvalidConfigurationException malformedURL(String property, MalformedURLException e) {
    return new InvalidConfigurationException(String.format("The specified url cannot be interpreted: '%s'", property),
        e);
  }

}
