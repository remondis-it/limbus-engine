package com.remondis.limbus.engine;

import com.remondis.limbus.engine.api.DefaultComponents;
import com.remondis.limbus.engine.api.DefaultComponentsConsumer;
import com.remondis.limbus.engine.api.EmptySharedClasspathProvider;
import com.remondis.limbus.engine.api.LogTarget;
import com.remondis.limbus.engine.api.SharedClasspathProvider;
import com.remondis.limbus.engine.api.security.LimbusSecurity;
import com.remondis.limbus.engine.logging.SystemOutLogTarget;
import com.remondis.limbus.engine.security.LimbusSecurityImpl;

/**
 * This is the default components enumerator that can be used to get a minimal system configuration for use with
 * <tt>LimbusStaging.withDefaultLimbusComponents(DefaultComponents)</tt>.
 *
 * 
 *
 */
public class LimbusDefaultComponents implements DefaultComponents {

  /**
   * <p>
   * This method adds the most common default components to run a Limbus environment. These are:
   *
   * <table>
   * <tr>
   * <td>Request Type</td>
   * <td>Implementation</td>
   * <td>Remarks</td>
   * </tr>
   * <tr>
   * <td>{@link LimbusSecurity}</td>
   * <td>{@link LimbusSecurityImpl}</td>
   * <td>The security manager and infrastructure for sandboxing.</td>
   * </tr>
   * <tr>
   * <td>{@link SharedClasspathProvider}</td>
   * <td>{@link EmptySharedClasspathProvider}</td>
   * <td>The shared classpath provider. Often empty because the shared libraries are use-case specific.</td>
   * </tr>
   * <tr>
   * <td>{@link LogTarget}</td>
   * <td>{@link SystemOutLogTarget}</td>
   * <td>The component defining output redirects. For integration tests console output is desired.</td>
   * </tr>
   * </ul>
   * <b>Note: The {@link LimbusFileService} is not part of the default components because in case a mocked
   * version is needed, the mock instance must be created manually.</b>
   * </p>
   */
  @Override
  public void applyDefaultComponents(DefaultComponentsConsumer consumer) {
    consumer.addComponentConfiguration(LimbusSecurity.class, LimbusSecurityImpl.class);
    consumer.addComponentConfiguration(SharedClasspathProvider.class, EmptySharedClasspathProvider.class);
    consumer.addComponentConfiguration(LogTarget.class, SystemOutLogTarget.class);
  }

}
