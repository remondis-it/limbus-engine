package org.testclient.packageB;

import java.util.UUID;

import com.remondis.limbus.monitoring.Monitoring;
import com.remondis.limbus.monitoring.MonitoringFactory;
import com.remondis.limbus.monitoring.publisher.MessagePublisher;
import com.remondis.limbus.monitoring.publisher.SomeOtherPublisher;

public class Client {

  private static final Monitoring monitor = MonitoringFactory.getMonitoring(Client.class);

  public String sendMessage() {
    String message = UUID.randomUUID()
        .toString();
    monitor.publish(MessagePublisher.class)
        .message(message);
    return message;
  }

  public String sendFormattedMessage() {
    String message = UUID.randomUUID()
        .toString();
    monitor.publish(MessagePublisher.class)
        .message("formatted:%s", message);
    return "formatted:" + message;
  }

  public void someOtherMethod() {
    monitor.publish(SomeOtherPublisher.class)
        .someOtherMethod();
  }

  public void callImmediately() {
    monitor.publish(SomeOtherPublisher.class)
        .callImmediatelyMethod();
  }

}
