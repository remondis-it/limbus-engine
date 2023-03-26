package com.remondis.limbus.system.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to mark the package of an application. Use this
 * method on a class that marks the application's root package to configure the
 * automatic component scan for building the {@link LimbusSystem} environment.
 * <p>
 * This annotation enables the component scan that collects all
 * {@link LimbusComponent}s from classpath that are available in the package of
 * the annotated application class as well as in all subpackages.
 * </p>
 * 
 * <h2>Importing</h2>
 * <p>
 * Sometimes components are located in a package that is not part of the
 * application's root package. This often happens when using components from a
 * library. In this case {@link LimbusComponent}s can be imported using
 * {@link PrivateComponent}, {@link PublicComponent} or
 * </p>
 * 
 * @author schuettec
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LimbusApplication {

  /**
   * @return The type of object factory to be used, when creating objects for
   *         {@link LimbusSystem}.
   */
  Class<? extends ObjectFactory> objectFactory();

}
