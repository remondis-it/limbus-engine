package org.max5.limbus.monitoring.publisher;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.max5.limbus.Initializable;
import org.max5.limbus.monitoring.ClientContext;
import org.max5.limbus.monitoring.PublisherUtils;
import org.max5.limbus.utils.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class defines an abstract runtime publisher that is able to measure the runtime between the invocations of
 * {@link #start()} and {@link #stop()}. Since those methods must be called immediately it is neccessary to buffer the
 * measurements on {@link #stop()}. The next {@link #publish()} call will then publish the buffered runtime
 * measurements. The number of maximal published records can be configured. The default is {@value #MAX_PUBLISH}
 * records.
 *
 * @author schuettec
 *
 */
public abstract class AbstractRuntimePublisher extends Initializable<Exception> implements Runtime {

  private static final int MAX_PUBLISH = 10;

  private static final Logger log = LoggerFactory.getLogger(AbstractRuntimePublisher.class);

  private transient ConcurrentHashMap<Integer, ClientContext> started;

  private transient LinkedBlockingQueue<RuntimeMeasurement> measurements;

  /**
   * Holds the number of maximumm records to be published on publish().
   */
  private Integer maxPublish;

  @Override
  protected Object readResolve() {
    super.readResolve();
    started = new ConcurrentHashMap<>();
    measurements = new LinkedBlockingQueue<RuntimeMeasurement>();
    return this;
  }

  @Override
  protected void performInitialize() throws Exception {
    maxPublish = Lang.defaultIfNull(maxPublish, MAX_PUBLISH);
  }

  @Override
  public void start() {
    ClientContext clientContext = PublisherUtils.getClientContext();
    if (started.containsValue(clientContext)) {
      log.warn("A runtime measurement was started but not stopped:\n{}", Lang.stackTraceAsString(Thread.currentThread()
          .getStackTrace()));
    } else {
      started.put(clientContext.hashCode(), clientContext);
    }
  }

  @Override
  public void stop() {
    ClientContext clientContext = PublisherUtils.getClientContext();
    if (started.containsKey(clientContext.hashCode())) {
      ClientContext clientContextWhenStarted = started.get(clientContext.hashCode());
      // schuettec - 24.04.2017 : Remove the client context immediately.
      started.remove(clientContext.hashCode());

      long runtime = clientContext.getTimestamp() - clientContextWhenStarted.getTimestamp();
      this.measurements.add(new RuntimeMeasurement(clientContextWhenStarted, clientContext, runtime));

    } else {
      log.warn("A runtime measurement was stopped but not started:\n{}", Lang.stackTraceAsString(Thread.currentThread()
          .getStackTrace()));
    }
  }

  @Override
  public void publish() {
    for (int i = 0; i < maxPublish; i++) {
      RuntimeMeasurement toPublish = measurements.poll();
      if (toPublish != null) {
        ClientContext start = toPublish.getClientContextOnStart();
        ClientContext end = toPublish.getClientContextOnEnd();
        publishRuntime(end.getTimestamp(), end.getClassName(), end.getMethodName(), start.getLineNumber(),
            end.getLineNumber(), toPublish.getDurationInMilliseconds());
      }
    }
  }

}
