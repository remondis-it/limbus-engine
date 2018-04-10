package org.max5.limbus.monitoring.influx;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.max5.limbus.monitoring.Monitoring;
import org.max5.limbus.monitoring.MonitoringFactory;
import org.max5.limbus.monitoring.publisher.Runtime;
import org.max5.limbus.utils.Lang;

public class ExamplePublish {

  private static final Monitoring monitor = MonitoringFactory.getMonitoring(ExamplePublish.class);

  public static void main(String[] args) throws Exception {
    Lang.initializeJDKLogging();
    ThreadPoolExecutor service = new ThreadPoolExecutor(10, 15, 30, TimeUnit.SECONDS,
        new LinkedBlockingQueue<Runnable>());

    for (int i = 0; i < 9999; i++) {
      service.submit(new Runnable() {

        @Override
        public void run() {
          for (int i = 0; i < 100; i++) {
            monitor.publish(Runtime.class)
                .start();
            try {
              Thread.sleep(Math.round(Math.random() * 500.0));
            } catch (InterruptedException e) {
              e.printStackTrace();
            }
            monitor.publish(Runtime.class)
                .stop();
            monitor.publish(Runtime.class)
                .publish();
          }
        }
      });
    }

  }

}
