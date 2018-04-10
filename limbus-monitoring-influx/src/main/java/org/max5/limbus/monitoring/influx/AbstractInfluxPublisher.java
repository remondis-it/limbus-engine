package org.max5.limbus.monitoring.influx;

import java.util.concurrent.TimeUnit;

import org.influxdb.dto.Point;
import org.max5.limbus.monitoring.PublisherUtils;
import org.max5.limbus.monitoring.publisher.AbstractRuntimePublisher;

/**
 * This class is an abstract publisher implementation for use with InfluxDB. It implements the supported publisher
 * interfaces and converts collected information to {@link Point} objects that can be written to an Influx database.
 *
 * <p>
 * This
 * </p>
 *
 * @author schuettec
 *
 */
public abstract class AbstractInfluxPublisher extends AbstractRuntimePublisher {

  @Override
  public void publishRuntime(long timeStamp, String className, String method, Integer lineNumberStart,
      Integer lineNumberEnd, long runtime) {
    //@formatter:off
    Point point = Point.measurement("runtime")
                       .tag("system", PublisherUtils.getLocalHostname())
                       .tag("classname", className)
                       .tag("method", method)
                       .field("lineStart", lineNumberStart)
                       .field("lineEnd", lineNumberEnd)
                       .field("runtime", runtime)
                       .time(timeStamp, TimeUnit.MILLISECONDS)
                       .build();
    //@formatter:on
    writePoint(point);
  }

  /**
   * Calles by the monitoring to publish a point to Influx database.
   *
   * @param point
   *        The point to write.
   */
  protected abstract void writePoint(Point point);

}
