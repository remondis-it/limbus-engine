package com.remondis.limbus.system;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Annotation that can be attached on fields of a class to request injection of a Limbus component managed by a
 * {@link LimbusSystem}. This annotation can also be used on classes make this component eligible for component scan.
 * <p>
 * <b>Note: The injection of components can only be performed on objects that are registered and managed by a
 * {@link LimbusSystem}</b>
 * </p>
 * <p>
 * <b>Note: The injection can only target objects known by the same {@link LimbusSystem} the requesting component
 * is managed by.</b>
 * </p>
 * <h2>Component scan</h2>
 * <p>
 * This annotation can be used on Limbus component interfaces as well as their implementations. When starting the
 * {@link LimbusSystem} using {@link LimbusApplication}, all annotated interfaces are collected and respective
 * implementations are searched. If a single implementation was found for an annotated interface, a public component is
 * registered on the {@link LimbusSystem} that can be requested using the interface type. If multiple implementations of
 * an interface were found, a specific binding is required using {@link PublicComponent} on the application class.
 * </p>
 * 
 * @author schuettec
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Inherited
@Target({
    ElementType.FIELD, ElementType.TYPE
})
public @interface LimbusComponent {
  /**
   * Specified the request type of the component to inject.
   */
  Class<?> value() default Void.class;
}
