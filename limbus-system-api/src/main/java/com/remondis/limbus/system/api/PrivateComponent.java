package com.remondis.limbus.system.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import com.remondis.limbus.api.IInitializable;

/**
 * This annotation is used in conjunction with
 * classes annotated with {@link LimbusApplication} to import other
 * components in packages that are not picked up by the component scan.
 * 
 * 
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
@Repeatable(PrivateComponent.List.class)
public @interface PrivateComponent {

  /**
   * @return If <code>true</code> initializing the {@link LimbusSystem} fails if this coponent fails to initialize.
   *         Default is <code>true</code>.
   */
  boolean failOnError() default true;

  /**
   * @return The type of the private component implementation.
   */
  Class<? extends IInitializable<?>> value();

  /**
   * Container annotation to enumerate {@link PublicComponent} declarations.
   * 
   * 
   *
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  @Target(ElementType.TYPE)
  public @interface List {

    PrivateComponent[] value();

  }

}
