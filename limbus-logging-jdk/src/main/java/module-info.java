import com.remondis.limbus.activators.logging.LoggingActivator;
import com.remondis.limbus.logging.JDKLoggingActivator;

module com.remondis.limbus.logging.jdk {
  exports com.remondis.limbus.logging;

  requires java.logging;
  requires com.remondis.limbus.utils;
  requires com.remondis.limbus.api;
  requires com.remondis.limbus.engine.interfaces;

  provides LoggingActivator with JDKLoggingActivator;

}