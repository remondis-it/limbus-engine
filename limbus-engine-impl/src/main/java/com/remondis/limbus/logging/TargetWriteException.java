package com.remondis.limbus.logging;

import java.io.IOException;

/**
 * Thrown on any error while writing to a target.
 *
 * @author schuettec
 *
 */
public class TargetWriteException extends IOException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public TargetWriteException() {
    super();
  }

  public TargetWriteException(String message, Throwable cause) {
    super(message, cause);
  }

  public TargetWriteException(String message) {
    super(message);
  }

  public TargetWriteException(Throwable cause) {
    super(cause);
  }

}
