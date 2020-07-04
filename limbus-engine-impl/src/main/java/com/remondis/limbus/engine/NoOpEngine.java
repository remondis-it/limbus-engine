package com.remondis.limbus.engine;

import com.remondis.limbus.engine.api.LimbusEngine;

/**
 * This is an implementation of a {@link LimbusEngine} that does nothing. This class is used to have a valid engine to
 * be started for demo purposes. This engine is the default engine used by the default build of this project.
 *
 * 
 *
 */
public class NoOpEngine extends LimbusEngineImpl {

  @Override
  protected String[] getPublicAccessPackages() {
    return new String[] {};
  }

}
