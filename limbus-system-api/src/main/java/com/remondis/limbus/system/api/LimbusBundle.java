package com.remondis.limbus.system.api;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * This annotation is used on classes to easily bundle multiple Limbus
 * Components. Bundles can be used by other applications to import multiple
 * components at the same time, where the component scan would not pick them up
 * automatically.
 * 
 * <h2>Bundle components</h2>
 * <p>
 * Creating a bundle of Limbus Components helps applications in other packages
 * to import multiple components at the same time without specifying all the
 * types. Use this annotation on a class that marks the bundle. Then use
 * {@link PublicComponent} or {@link PrivateComponent} on the same class to
 * reference {@link LimbusComponent}s that should be part of the bundle.
 * </p>
 * 
 * @author schuettec
 *
 */
@Documented
@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface LimbusBundle {

}
