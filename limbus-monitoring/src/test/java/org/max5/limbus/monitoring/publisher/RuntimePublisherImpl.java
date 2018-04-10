package org.max5.limbus.monitoring.publisher;

public class RuntimePublisherImpl extends AbstractRuntimePublisher {

  private Long duration;
  private String className;
  private String methodName;

  @Override
  public void publishRuntime(long timeStamp, String className, String method, Integer lineNumberStart,
      Integer lineNumberEnd, long duration) {
    this.className = className;
    this.methodName = method;
    this.duration = duration;
  }

  /**
   * @return the duration
   */
  public Long getDuration() {
    return duration;
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

  @Override
  protected void performInitialize() throws Exception {
    super.performInitialize();
  }

  @Override
  protected void performFinish() {
  }

}
