package org.max5.limbus.logging;

import java.io.IOException;
import java.io.OutputStream;

public class OutputStreamTargetWriter implements TargetWriter<OutputStream> {

  @Override
  public void writeTo(byte[] b, int off, int len, OutputStream target) throws TargetWriteException {
    try {
      target.write(b, off, len);
      target.flush();
    } catch (IOException e) {
      throw new TargetWriteException("Cannot write to target");
    }
  }

  @Override
  public void writeTo(int b, OutputStream target) throws TargetWriteException {
    try {
      target.write(b);
      target.flush();
    } catch (IOException e) {
      throw new TargetWriteException("Cannot write to target");
    }
  }

  @Override
  public void flush(OutputStream target) throws TargetWriteException {
    try {
      target.flush();
    } catch (IOException e) {
      throw new TargetWriteException("Cannot write to target");
    }
  }

  @Override
  public void close(OutputStream target) throws TargetWriteException {
    try {
      target.flush();
      target.close();
    } catch (IOException e) {
      throw new TargetWriteException("Cannot write to target");
    }
  }
}
