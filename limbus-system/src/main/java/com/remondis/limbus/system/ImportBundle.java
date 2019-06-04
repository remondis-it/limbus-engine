package com.remondis.limbus.system;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used to import a {@link LimbusBundle}. Use this annotation in conjunction with
 * {@link LimbusApplication} to import the specified bundle.
 * 
 * @author schuettec
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface ImportBundle {

  Class<?>[] value();

}
