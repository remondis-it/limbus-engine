module com.remondis.limbus.monitoring {
  exports com.remondis.limbus.monitoring;
  exports com.remondis.limbus.monitoring.publisher;

  // requires com.remondis.limbus.api;
  requires org.slf4j;
  // requires com.remondis.limbus.utils;
  requires com.remondis.limbus.engine.interfaces;
  requires com.remondis.limbus.system;
  requires xstream;

  requires transitive com.remondis.limbus.api;
  requires transitive com.remondis.limbus.utils;

}