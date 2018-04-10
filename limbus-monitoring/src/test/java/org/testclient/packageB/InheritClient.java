package org.testclient.packageB;

import java.util.UUID;

import org.max5.limbus.monitoring.Monitoring;
import org.max5.limbus.monitoring.MonitoringFactory;
import org.max5.limbus.monitoring.publisher.MessagePublisher;

public class InheritClient extends Client {

  private static final Monitoring monitor = MonitoringFactory.getMonitoring(InheritClient.class);

  @Override
  public String sendMessage() {
    return super.sendMessage();
  }

  @Override
  public String sendFormattedMessage() {
    return super.sendFormattedMessage();
  }

  public String sendMessageAsInheritClient() {
    String message = UUID.randomUUID()
        .toString();
    monitor.publish(MessagePublisher.class)
        .message(message);
    return message;
  }

  public String sendFormattedMessageAsInheritClient() {
    String message = UUID.randomUUID()
        .toString();
    monitor.publish(MessagePublisher.class)
        .message("formatted:%s", message);
    return "formatted:" + message;
  }
}
