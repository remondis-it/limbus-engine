package com.remondis.limbus.files;

/**
 * Thrown if a file could not be found.
 *
 * 
 *
 */
public class FileNotFoundException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public FileNotFoundException() {
  }

  public FileNotFoundException(String message) {
    super(message);
  }

  public FileNotFoundException(Throwable cause) {
    super(cause);
  }

  public FileNotFoundException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileNotFoundException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
