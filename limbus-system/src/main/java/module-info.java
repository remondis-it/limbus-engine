module com.remondis.limbus.system {
  exports com.remondis.limbus.system;
  exports com.remondis.limbus.system.visualize;

  requires com.remondis.limbus.api;
  requires com.remondis.limbus.events;
  requires com.remondis.limbus.utils;

  requires org.slf4j;
  requires xstream;
  requires static gs.core;
  requires static gs.ui;
  requires java.desktop;
}