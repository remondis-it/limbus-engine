package org.max5.limbus.logging;

import java.io.OutputStream;

import org.max5.limbus.files.FileAccessException;
import org.max5.limbus.files.LimbusFileService;
import org.max5.limbus.launcher.EngineLauncher;
import org.max5.limbus.system.LimbusComponent;
import org.max5.limbus.utils.Lang;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemLogTarget extends AbstractLogTarget<OutputStream> {

  private static final String ERROR_SUFFIX = "_error";

  private static final String DEFAULT_TARGET_FILE = "default";

  private static final Logger log = LoggerFactory.getLogger(FileSystemLogTarget.class);

  @LimbusComponent
  LimbusFileService fileService;

  @Override
  protected void performInitialize() throws Exception {

    // Create log folder
    fileService.createFolder(LimbusFileService.LOGGING_DIRECTORY, false);

    // Do this at the end, we do not want to initialize the log target if we fail in creating the file system log
    // environment.
    super.performInitialize();
  }

  @Override
  protected void performFinish() {
    super.performFinish();
  }

  @Override
  protected OutputStream createStdOutTarget(String deployName) {
    try {
      return new RolloverFileOutputStream(fileService, deployName, true);
    } catch (FileAccessException e) {
      log.error(String.format("Cannot create logging target for deploy name '%s'. Redirecting output to System.out.",
          deployName), e);
      return EngineLauncher.getOriginalSystemOut();
    }
  }

  @Override
  protected OutputStream createStdErrTarget(String deployName) {
    try {
      return new RolloverFileOutputStream(fileService, deployName + ERROR_SUFFIX, true);
    } catch (FileAccessException e) {
      log.error(String.format("Cannot create logging target for deploy name '%s'. Redirecting output to System.out.",
          deployName), e);
      return EngineLauncher.getOriginalSystemOut();
    }
  }

  @Override
  protected void destroyTarget(OutputStream target) {
    Lang.closeQuietly(target);
  }

  @Override
  protected TargetWriter<OutputStream> getTargetWriter() {
    return new OutputStreamTargetWriter();
  }

  @Override
  protected OutputStream getDefaultTargetStdOut() {
    return createStdOutTarget(DEFAULT_TARGET_FILE);
  }

  @Override
  protected OutputStream getDefaultTargetStdErr() {
    return createStdOutTarget(DEFAULT_TARGET_FILE + ERROR_SUFFIX);
  }

}
