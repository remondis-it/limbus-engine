package com.remondis.limbus.engine.logging;

import java.io.OutputStream;

import com.remondis.limbus.launcher.EngineLauncher;

public class SystemOutLogTarget extends AbstractLogTarget<OutputStream> {

  @Override
  protected void performInitialize() throws Exception {
    super.performInitialize();
  }

  @Override
  protected void performFinish() {
    super.performFinish();

  }

  @Override
  protected OutputStream createStdOutTarget(final String deployName) {
    return EngineLauncher.getOriginalSystemOut();
  }

  @Override
  protected OutputStream createStdErrTarget(String deployName) {
    return EngineLauncher.getOriginalSystemErr();
  }

  @Override
  protected TargetWriter<OutputStream> getTargetWriter() {
    return new OutputStreamTargetWriter();
  }

  @Override
  protected OutputStream getDefaultTargetStdOut() {
    return EngineLauncher.getOriginalSystemOut();
  }

  @Override
  protected OutputStream getDefaultTargetStdErr() {
    return EngineLauncher.getOriginalSystemErr();
  }

  @Override
  protected void destroyTarget(OutputStream target) {
    // Do not close
    // We would have to close the original System.out and that is not what we really want.
  }

}
