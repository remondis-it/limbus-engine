package com.remondis.limbus.activators.logging;

import com.remondis.limbus.api.IInitializable;

/**
 * This interface is used by the engine launcher to activate the concrete logging framework. Normally one would
 * initialize the logging framework by setting a JVM system property pointing to a logging configuration. This is a
 * problem in a shared environment like the Limbus Engine, because plugins that may use the same logging framework will
 * try to read the configuration set for the host. This results in multiple errors:
 * <ul>
 * <li>A plugin might not have the permissions to read a file at the specified location</li>
 * <li>A plugin should use only the std/out for logging where the Limbus Engine in contrast should use only file
 * logging.</li>
 * </ul>
 *
 * The {@link LoggingActivator} is intended to activate and configure the target logging framework without using
 * globally visible system properties.
 *
 * 
 *
 */
public interface LoggingActivator extends IInitializable<LoggingActivatorException> {

}
