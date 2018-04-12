package com.remondis.limbus.monitoring;

import com.remondis.limbus.utils.SerializeException;

public class MonitoringConfigurationExample {
  public static void main(String[] args) throws SerializeException {
    ProcessingConfig processing = new ProcessingConfig();

    PublisherConfig pub1 = new PublisherConfig("Runtime", "com.some.package.RuntimePublisher");
    PublisherConfig pub2 = new PublisherConfig("ThreadCount", "com.some.package.ThreadCounterPublisher");
    PublisherConfig pub3 = new PublisherConfig("Runtime", "com.some.package.RuntimePublisherDuplicate");

    Pattern p1 = new Pattern("com.client.app.package");
    p1.publishers.add("ThreadCount");
    p1.publishers.add("Runtime");

    Pattern p2 = new Pattern("com.another.app.package");
    p2.publishers.add("Runtime");

    MonitoringConfiguration c = new MonitoringConfiguration();
    c.addPublisher(pub1);
    c.addPublisher(pub2);
    c.addPublisher(pub3);
    c.addPattern(p1);
    c.addPattern(p2);
    c.setProcessing(processing);

    MonitoringFactory.getDefaultXStream()
        .writeObject(c, System.out);
  }
}
