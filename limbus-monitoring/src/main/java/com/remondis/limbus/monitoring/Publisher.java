package com.remondis.limbus.monitoring;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * A publisher is an entity that can publish monitoring events. A
 * publisher implements a certain interface - the publisher interface - that
 * must met the following conditions to be a valid monitoring publisher:
 * <ul>
 * <li>A publisher interface has the annotation {@link Publisher} on its
 * type to signal that all conventions are met.</li>
 * <li>Methods of a publisher interface must not have a return type.</li>
 * <li>Methods of a publisher interface may be annotated with {@link CallImmediately} to signal that this method call
 * must be performed immediately instead of scheduling the method call for asynchronous execution.</li>
 * </ul>
 *
 * <p>
 * <b>The above requirements can be checked at runtime using the methods from
 * {@link Conventions}.</b>
 * </p>
 *
 *
 * <h2>Publisher implementations</h2>
 * <p>
 * A publisher processes monitoring calls. This interface defines methods a publisher must implement. Monitoring
 * calls may occur very often with a high frequency and concurrency. Therefore <b>implementations must support a
 * non-blocking and highly thread-safe processing of monitoring calls</b>.
 * </p>
 *
 * @author schuettec
 *
 *
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(value = ElementType.TYPE)
@Inherited
public @interface Publisher {
}
