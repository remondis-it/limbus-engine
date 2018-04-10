package org.max5.limbus;

/**
 * This exception tries to produce output as minimally as possible when thrown. Use for test purposes.
 *
 * @author schuettec
 *
 */
class DummyRuntimeException extends RuntimeException {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public DummyRuntimeException() {
    this("Thrown for test purposes. Ignore this exception!", null, false, false);
  }

  protected DummyRuntimeException(String message, Throwable cause, boolean enableSuppression,
      boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public DummyRuntimeException(String message, Throwable cause) {
    this();
  }

  public DummyRuntimeException(String message) {
    this();
  }

  public DummyRuntimeException(Throwable cause) {
    this();
  }

}
