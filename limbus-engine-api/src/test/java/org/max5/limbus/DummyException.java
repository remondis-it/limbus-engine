package org.max5.limbus;

/**
 * This exception tries to produce output as minimally as possible when thrown. Use for test purposes.
 *
 * @author schuettec
 *
 */
class DummyException extends Exception {

  /**
   *
   */
  private static final long serialVersionUID = 1L;

  public DummyException() {
    this("Thrown for test purposes. Ignore this exception!", null, false, false);
  }

  protected DummyException(String message, Throwable cause, boolean enableSuppression, boolean writableStackTrace) {
    super(message, cause, enableSuppression, writableStackTrace);
  }

  public DummyException(String message, Throwable cause) {
    this();
  }

  public DummyException(String message) {
    this();
  }

  public DummyException(Throwable cause) {
    this();
  }

}
