package org.max5.limbus.monitoring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Publisher methods marked with this annotation are called immediately by the monitoring framework instead of
 * scheduling the method call for later asynchronous execution. This annotation is useful for methods that return object
 * references to the calling client. <b>Note: This annotation is mandatory for methods that have a return value because
 * those methods cannot be scheduled asynchronoulsy by design.</b>
 *
 * @author schuettec
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.METHOD)
@Inherited
public @interface CallImmediately {

}
