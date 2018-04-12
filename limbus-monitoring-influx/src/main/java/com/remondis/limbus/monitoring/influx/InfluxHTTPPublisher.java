package com.remondis.limbus.monitoring.influx;

import java.util.concurrent.TimeUnit;

import org.influxdb.InfluxDB;
import org.influxdb.InfluxDB.LogLevel;
import org.influxdb.InfluxDBFactory;
import org.influxdb.dto.Point;
import com.remondis.limbus.monitoring.PublisherUtils;
import com.remondis.limbus.utils.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is a publisher implementation to write monitoring events via HTTP to an Influx database.
 *
 * <h2>Thread safety</h2>
 * <p>
 * This implementation is
 * thread safe because the underlying HTTP implementation that handles the write events is thread-safe.
 * <a href="https://github.com/influxdata/influxdb-java/issues/35">This issue</a> explains the thread-safetyness of
 * {@link InfluxDB} objects.
 * <p>
 *
 * @author schuettec
 *
 */
public class InfluxHTTPPublisher extends AbstractInfluxPublisher {

  private static final Logger log = LoggerFactory.getLogger(InfluxHTTPPublisher.class);

  private String databaseUrl;
  private String username;
  private String password;
  private String database;
  private String retentionPolicy;

  private transient InfluxDB influxDB;

  @Override
  protected void performInitialize() throws Exception {
    super.performInitialize();

    PublisherUtils.denyRequired("databaseUrl", databaseUrl);
    PublisherUtils.denyRequired("username", username);
    PublisherUtils.denyRequired("password", password);
    PublisherUtils.denyRequired("database", database);
    Lang.defaultIfNull(retentionPolicy, "default");

    influxDB = InfluxDBFactory.connect(databaseUrl, username, password);
    influxDB.enableBatch(2000, 1000, TimeUnit.MILLISECONDS);
    influxDB.setLogLevel(LogLevel.NONE);
  }

  @Override
  protected void writePoint(Point point) {
    try {
      influxDB.write(database, retentionPolicy, point);
    } catch (Exception e) {
      logError(e);
    }
  }

  private void logError(Throwable e) {
    log.debug(String.format("Error while publishing a runtime measurement to InfluxDB @ %s", databaseUrl), e);
  }

  @Override
  protected void performFinish() {
    try {
      if (influxDB != null) {
        influxDB.disableBatch();
        influxDB = null;
      }
    } catch (Exception e) {
      log.warn("Finishing the Influx HTTP publisher threw an exception.", e);
    }
  }

  /**
   * @return the username
   */
  public String getUsername() {
    return username;
  }

  /**
   * @param username
   *        the username to set
   */
  public void setUsername(String username) {
    this.username = username;
  }

  /**
   * @return the password
   */
  public String getPassword() {
    return password;
  }

  /**
   * @param password
   *        the password to set
   */
  public void setPassword(String password) {
    this.password = password;
  }

  /**
   * @return the database
   */
  public String getDatabase() {
    return database;
  }

  /**
   * @param database
   *        the database to set
   */
  public void setDatabase(String database) {
    this.database = database;
  }

  /**
   * @return the retentionPolicy
   */
  public String getRetentionPolicy() {
    return retentionPolicy;
  }

  /**
   * @param retentionPolicy
   *        the retentionPolicy to set
   */
  public void setRetentionPolicy(String retentionPolicy) {
    this.retentionPolicy = retentionPolicy;
  }

}
