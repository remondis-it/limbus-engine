module com.remondis.limbus.launcher {
  exports com.remondis.limbus.launcher;

  requires org.slf4j;
  requires com.remondis.limbus.api;
  requires com.remondis.limbus.utils;
  requires commons.cli;
  requires com.remondis.limbus.engine.interfaces;
  requires com.remondis.limbus.system;
  requires com.remondis.limbus.events;

  uses com.remondis.limbus.activators.logging.LoggingActivator;
  uses com.remondis.limbus.activators.monitoring.MonitoringActivator;
}