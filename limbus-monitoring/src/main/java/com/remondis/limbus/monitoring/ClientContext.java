package com.remondis.limbus.monitoring;

/**
 * This class defines the data identifying a method call.
 *
 * @author schuettec
 *
 */
public class ClientContext {

  /**
   * The following fields identify an object of this type.
   */
  protected long threadHashCode;
  protected long threadContextClHashCode;
  protected String className;
  protected String methodName;

  /**
   * Holds the timestamp in milliseconds from when this object was created.
   */
  protected long timestamp;
  /**
   * Holds the line number from where this client context was created.
   */
  protected Integer lineNumber;

  ClientContext(Thread thread, int stackOffset) {
    super();
    this.timestamp = System.currentTimeMillis();
    StackTraceElement[] stackTraceElements = Thread.currentThread()
        .getStackTrace();
    this.threadHashCode = System.identityHashCode(thread);
    this.threadContextClHashCode = System.identityHashCode(thread.getContextClassLoader());
    this.className = stackTraceElements[2 + stackOffset].getClassName();
    this.methodName = stackTraceElements[2 + stackOffset].getMethodName();
    this.lineNumber = stackTraceElements[2 + stackOffset].getLineNumber();
  }

  /**
   * @return the lineNumber
   */
  public Integer getLineNumber() {
    return lineNumber;
  }

  /**
   * @return the timestamp
   */
  public long getTimestamp() {
    return timestamp;
  }

  /**
   * @return the className
   */
  public String getClassName() {
    return className;
  }

  /**
   * @return the methodName
   */
  public String getMethodName() {
    return methodName;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((className == null) ? 0 : className.hashCode());
    result = prime * result + ((methodName == null) ? 0 : methodName.hashCode());
    result = prime * result + (int) (threadContextClHashCode ^ (threadContextClHashCode >>> 32));
    result = prime * result + (int) (threadHashCode ^ (threadHashCode >>> 32));
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    ClientContext other = (ClientContext) obj;
    if (className == null) {
      if (other.className != null)
        return false;
    } else if (!className.equals(other.className))
      return false;
    if (methodName == null) {
      if (other.methodName != null)
        return false;
    } else if (!methodName.equals(other.methodName))
      return false;
    if (threadContextClHashCode != other.threadContextClHashCode)
      return false;
    if (threadHashCode != other.threadHashCode)
      return false;
    return true;
  }

}
