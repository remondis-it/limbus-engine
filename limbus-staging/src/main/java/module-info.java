module com.remondis.limbus.staging {
  exports com.remondis.limbus.staging;
  exports com.remondis.limbus.staging.staging;

  requires com.remondis.limbus.api;
  requires com.remondis.limbus.engine.interfaces;
  requires com.remondis.limbus.system;
  requires com.remondis.limbus.launcher;
  requires com.remondis.limbus.utils;
  requires shrinkwrap.api;
  requires shrinkwrap.resolver.api.maven;
  requires shrinkwrap.resolver.api;
}