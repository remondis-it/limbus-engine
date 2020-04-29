package com.remondis.limbus.system.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be attached on fields of a class to request injection of the containing {@link LimbusSystem}.
 * <p>
 * <b>Note: The injection of {@link LimbusSystem} can only be performed on objects that are registered and managed by a
 * {@link LimbusSystem}</b>
 * </p>
 * <p>
 * <b>Note: The injection can only target objects known by the same {@link LimbusSystem} the requesting component
 * is managed by.</b>
 * </p>
 *
 * @author schuettec
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target(ElementType.FIELD)
public @interface LimbusContainer {
  /**
   * Specified the request type of the component to inject.
   */
  Class<?> value() default Void.class;
}
