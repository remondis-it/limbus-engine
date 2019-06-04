package com.remondis.limbus.system;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.remondis.limbus.IInitializable;

/**
 * This annotation is used in conjunction with
 * classes annotated with {@link LimbusApplication} to import other
 * components in packages that are not picked up by the component scan.
 * 
 * @author schuettec
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface PrivateComponent {

  boolean failOnError() default true;

  Class<? extends IInitializable<?>> value();

}
