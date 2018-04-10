package org.integrationtest;

import org.max5.limbus.Initializable;
import org.max5.limbus.monitoring.Monitoring;
import org.max5.limbus.monitoring.MonitoringFactory;

public class TestPluginImpl extends Initializable<Exception> implements TestPlugin {

  private static final Monitoring monitor = MonitoringFactory.getMonitoring(TestPluginImpl.class);

  @SuppressWarnings("unused")
  private static ConsolePublisher publisherInstance = null;

  @Override
  protected void performInitialize() throws Exception {

  }

  @Override
  protected void performFinish() {

  }

  @Override
  public void callMonitoring() {
    monitor.publish(ConsolePublisher.class)
        .console("This is a test message from " + getClass().getName());
  }

  @Override
  public void holdPublisher() {
    publisherInstance = monitor.publish(ConsolePublisher.class);
  }

  @Override
  public void releasePublisher() {
    publisherInstance = null;
  }

}
