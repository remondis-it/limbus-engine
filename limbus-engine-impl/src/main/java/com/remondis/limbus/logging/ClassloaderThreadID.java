package com.remondis.limbus.logging;

/**
 * This tupel stores the classloader id and an optional thread id.
 *
 * @author schuettec
 *
 */
public class ClassloaderThreadID {

  private int classloaderHashCode;
  private int threadHashCode;

  ClassloaderThreadID(int classloaderHashCode, int threadHashCode) {
    super();
    this.classloaderHashCode = classloaderHashCode;
    this.threadHashCode = threadHashCode;
  }

  /**
   * Returns the {@link ClassloaderThreadID} for the specified classloader and thread hashcode.
   *
   * @param classLoader
   *        The classloader
   * @param thread
   *        The thread
   * @return Returns the {@link ClassloaderThreadID}
   */
  public static ClassloaderThreadID getID(ClassLoader classLoader, Thread thread) {
    return new ClassloaderThreadID(ContextClassloaderSelector.getClassLoaderHashCode(classLoader),
        ContextClassloaderSelector.getThreadHashCode(thread));
  }

  /**
   * Returns the {@link ClassloaderThreadID} for the specified classloader and thread hashcode.
   *
   * @param classloaderHashCode
   *        The classloader's identity hashcode
   * @param threadHashCode
   *        The thread's identity hashcode
   * @return Returns the {@link ClassloaderThreadID}
   */
  public static ClassloaderThreadID getID(int classloaderHashCode, int threadHashCode) {
    return new ClassloaderThreadID(classloaderHashCode, threadHashCode);
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
    result = prime * result + classloaderHashCode;
    result = prime * result + threadHashCode;
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
    ClassloaderThreadID other = (ClassloaderThreadID) obj;
    if (classloaderHashCode != other.classloaderHashCode)
      return false;
    if (threadHashCode != other.threadHashCode)
      return false;
    return true;
  }

  /**
   * @return the classloaderHashCode
   */
  public int getClassloaderHashCode() {
    return classloaderHashCode;
  }

  /**
   * @return the threadHashCode
   */
  public int getThreadHashCode() {
    return threadHashCode;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "ClassloaderThreadID [classloader=" + classloaderHashCode + ", thread=" + threadHashCode + "]";
  }

}
