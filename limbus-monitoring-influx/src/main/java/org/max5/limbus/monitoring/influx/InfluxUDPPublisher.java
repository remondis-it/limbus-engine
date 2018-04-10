package org.max5.limbus.monitoring.influx;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.influxdb.dto.Point;
import org.max5.limbus.monitoring.PublisherUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a publisher implementation to write monitoring events to an Influx database via UDP.
 *
 * <h2>Thread safety</h2>
 * <p>
 * This implementation is
 * thread safe. The only shared information is an object of {@link InetAddress} used to create UDP datagrams.
 * <p>
 *
 * @author schuettec
 *
 */
public class InfluxUDPPublisher extends AbstractInfluxPublisher {

  private static final Logger log = LoggerFactory.getLogger(InfluxUDPPublisher.class);

  protected String ip;
  protected Integer port;

  private transient InetAddress host;

  @Override
  protected void performInitialize() throws Exception {
    super.performInitialize();

    PublisherUtils.denyRequired("ip", ip);
    PublisherUtils.denyRequired("port", port);
    try {
      host = InetAddress.getByName(ip);
    } catch (UnknownHostException e) {
      throw new Exception(String.format("Could not resolve host for specified host '%s:%s'.", ip, port), e);
    }
  }

  @Override
  protected void writePoint(Point point) {
    try {
      InfluxUtils.sendData(point, host, port);
    } catch (Exception e) {
      logError(e);
    }
  }

  private void logError(Throwable e) {
    log.debug(String.format("Error while publishing a runtime measurement to InfluxDB @ %s:%s", ip, port), e);
  }

  @Override
  protected void performFinish() {
  }

  /**
   * @return the ip
   */
  public String getIp() {
    return ip;
  }

  /**
   * @param ip
   *        the ip to set
   */
  public void setIp(String ip) {
    this.ip = ip;
  }

  /**
   * @return the port
   */
  public Integer getPort() {
    return port;
  }

  /**
   * @param port
   *        the port to set
   */
  public void setPort(Integer port) {
    this.port = port;
  }

}
