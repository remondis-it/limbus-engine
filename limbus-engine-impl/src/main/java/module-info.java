import com.remondis.limbus.engine.api.maven.MavenArtifactService;

open module com.remondis.limbus.engine.implementation {
  exports com.remondis.limbus.engine;
  exports com.remondis.limbus.engine.logging;
  exports com.remondis.limbus.engine.actions;

  uses MavenArtifactService;

  requires com.remondis.limbus.api;
  requires com.remondis.limbus.engine.interfaces;
  requires com.remondis.limbus.system.api;
  requires com.remondis.limbus.system;
  requires transitive com.remondis.limbus.utils;
  requires com.remondis.limbus.vfs;
  requires com.remondis.limbus.tasks;
  requires com.remondis.limbus.properties;
  requires com.remondis.limbus.events;
  requires com.remondis.limbus.launcher;

  requires java.desktop;
  requires org.apache.commons.io;
  requires com.googlecode.lanterna;
  requires org.slf4j;
}