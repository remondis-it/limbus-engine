module com.remondis.limbus.engine.implementation {
  exports com.remondis.limbus.engine;
  exports com.remondis.limbus.engine.logging;

  requires com.remondis.limbus.launcher;
  requires com.remondis.limbus.api;
  requires com.remondis.limbus.engine.interfaces;
  requires com.remondis.limbus.system.api;
  requires com.remondis.limbus.system;
  requires com.remondis.limbus.utils;
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
  requires lanterna;
  requires java.desktop;
  requires com.remondis.limbus.tasks;
  requires com.remondis.limbus.properties;
  requires com.remondis.limbus.events;
}