package com.remondis.limbus.files;

import java.io.File;

/**
 * Thrown if a file could not be created.
 *
 * @author schuettec
 *
 */
public class FileAccessException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public FileAccessException() {
  }

  public FileAccessException(String message) {
    super(message);
  }

  public FileAccessException(Throwable cause) {
    super(cause);
  }

  public FileAccessException(File unchecked, Exception cause) {
    this(String.format("Cannot access the file %s", unchecked.getAbsolutePath()), cause);
  }

  public FileAccessException(String message, Throwable cause) {
    super(message, cause);
  }

  public FileAccessException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

}
