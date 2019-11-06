module com.remondis.limbus.monitoring.influx {
  exports com.remondis.limbus.monitoring.influx;
  requires influxdb.java;
  requires com.remondis.limbus.monitoring;
  requires org.slf4j;
  requires com.remondis.limbus.utils;
}