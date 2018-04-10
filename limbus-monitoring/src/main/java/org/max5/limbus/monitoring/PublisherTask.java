package org.max5.limbus.monitoring;

import java.util.concurrent.LinkedBlockingQueue;

import org.max5.limbus.utils.Lang;

/**
 * This {@link Runnable} is executed by {@link MonitoringProcessor} to publish monitoring records.
 *
 * @author schuettec
 *
 */
public class PublisherTask implements Runnable {

  public static final LinkedBlockingQueue<PublisherTask> TASKS = new LinkedBlockingQueue<>();

  protected MethodCall call;
  private Object publisherInstance;

  public PublisherTask(MethodCall call, Object publisherInstance) {
    Lang.denyNull("call", call);
    Lang.denyNull("publisherInstance", publisherInstance);
    this.call = call;
    this.publisherInstance = publisherInstance;

    TASKS.add(this);
  }

  @Override
  public void run() {
    try {
      call.replay(publisherInstance);
    } catch (Throwable e) {
      PublisherUtils.logPublisherCallFailed(publisherInstance, e);
    }
    TASKS.remove(this);
  }

}
