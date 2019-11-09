package com.remondis.limbus.system;

import java.lang.annotation.Documented;
import java.lang.annotation.Repeatable;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

import com.remondis.limbus.IInitializable;

/**
 * This annotation is used in conjunction with
 * classes annotated with {@link LimbusApplication} to import other
 * public components in packages that are not picked up by the component scan.
 * 
 * @author schuettec
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Repeatable(PublicComponent.List.class)
public @interface PublicComponent {
  /**
   * @return If <code>true</code> initializing the {@link LimbusSystem} fails if this coponent fails to initialize.
   *         Default is <code>true</code>.
   */
  boolean failOnError() default true;

  /**
   * @return The interface that defines a public component.
   */
  Class<? extends IInitializable<?>> requestType();

  /**
   * @return The type of the public component implementation.
   */
  Class<? extends IInitializable<?>> type();

  /**
   * Container annotation to enumerate {@link PublicComponent} declarations.
   * 
   * @author schuettec
   *
   */
  @Documented
  @Retention(RetentionPolicy.RUNTIME)
  public @interface List {

    PublicComponent[] value();

  }

}
