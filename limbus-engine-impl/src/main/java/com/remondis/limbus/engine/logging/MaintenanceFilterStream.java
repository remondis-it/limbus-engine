package com.remondis.limbus.engine.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.List;

import com.remondis.limbus.engine.LimbusMaintenanceConsoleImpl;
import com.remondis.limbus.launcher.EngineLauncher;

/**
 * The {@link MaintenanceFilterStream} is an implementation of {@link OutputStream} that accesses the original
 * std/out channel for every access initiated by the Limbus Maintenance console. This access needs to be written to
 * std/out without any modification. Any other access is delegated to the specified delegate stream.
 *
 * @author schuettec
 */
public class MaintenanceFilterStream extends OutputStream {

  // TODO - schuettec - 20.02.2017 : IMPROVE : This is currently an ugly solution to get thread names.
  private static final String[] MAINTENANCE_CONSOLE_THREAD_NAMES = new String[] {
      LimbusMaintenanceConsoleImpl.MAINTENANCE_CONSOLE_THREAD_NAME,
      LimbusMaintenanceConsoleImpl.MAINTENANCE_CONSOLE_OPERATION_THREAD_NAME, "Lanterna STTY restore", "BellSilencer",
      "LanternaGUI", "SIGWINCH handler"
  };

  private static final List<String> ALLOWED_THREAD_NAMES = Arrays.asList(MAINTENANCE_CONSOLE_THREAD_NAMES);

  private OutputStream delegate;

  public MaintenanceFilterStream(OutputStream delegate) {
    super();
    this.delegate = delegate;
  }

  private boolean checkThread() {
    String callerName = Thread.currentThread()
        .getName();
    boolean contains = ALLOWED_THREAD_NAMES.contains(callerName);
    return contains;
  }

  @Override
  public void write(int b) throws IOException {
    if (checkThread()) {
      EngineLauncher.getOriginalSystemOut()
          .write(b);
      EngineLauncher.getOriginalSystemOut()
          .flush();
    } else {
      delegate.write(b);
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    if (checkThread()) {
      EngineLauncher.getOriginalSystemOut()
          .write(b, off, len);
      EngineLauncher.getOriginalSystemOut()
          .flush();
    } else {
      delegate.write(b, off, len);
    }
  }

  @Override
  public void flush() throws IOException {
    if (checkThread()) {
      EngineLauncher.getOriginalSystemOut()
          .flush();
    } else {
      delegate.flush();
    }
  }

  @Override
  public void close() throws IOException {
    if (checkThread()) {
      EngineLauncher.getOriginalSystemOut()
          .flush();
    } else {
      delegate.close();
      delegate = null;
    }
  }

}
