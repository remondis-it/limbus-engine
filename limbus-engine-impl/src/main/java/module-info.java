open module com.remondis.limbus.engine.implementation {
  exports com.remondis.limbus.engine;
  exports com.remondis.limbus.engine.logging;
  exports com.remondis.limbus.engine.aether;
  exports com.remondis.limbus.engine.actions;

  requires com.remondis.limbus.launcher;
  requires com.remondis.limbus.api;
  requires com.remondis.limbus.engine.interfaces;
  requires com.remondis.limbus.system.api;
  requires com.remondis.limbus.system;
  requires transitive com.remondis.limbus.utils;
  requires maven.aether.provider;
  requires maven.settings;
  requires maven.settings.builder;
  requires aether.api;
  requires aether.connector.basic;
  requires aether.impl;
  requires aether.spi;
  requires aether.transport.file;
  requires aether.transport.http;
  requires aether.util;
  requires org.slf4j;
  requires com.remondis.limbus.vfs;
  requires com.googlecode.lanterna;
  requires java.desktop;
  requires org.apache.commons.io;
  requires com.remondis.limbus.tasks;
  requires com.remondis.limbus.properties;
  requires com.remondis.limbus.events;
}