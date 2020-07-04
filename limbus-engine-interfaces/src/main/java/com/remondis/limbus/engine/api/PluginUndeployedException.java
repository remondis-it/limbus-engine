package com.remondis.limbus.engine.api;

/**
 * Thrown by the framework when an object of type {@link LimbusPlugin} is accessed but the classpath delivering the
 * plugin was undeployed.
 *
 * 
 *
 */
public class PluginUndeployedException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public PluginUndeployedException() {
  }

  public PluginUndeployedException(String message) {
    super(message);
  }

  public PluginUndeployedException(Throwable cause) {
    super(cause);
  }

  public PluginUndeployedException(String message, Throwable cause) {
    super(message, cause);
  }

  public PluginUndeployedException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
