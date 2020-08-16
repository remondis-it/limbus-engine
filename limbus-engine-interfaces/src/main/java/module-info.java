open module com.remondis.limbus.engine.interfaces {
  exports com.remondis.limbus.activators.logging;
  exports com.remondis.limbus.activators.monitoring;
  exports com.remondis.limbus.engine.api;
  exports com.remondis.limbus.engine.api.security;
  exports com.remondis.limbus.engine.api.maven;

  requires transitive com.remondis.limbus.api;
  requires transitive com.remondis.limbus.system.api;
}