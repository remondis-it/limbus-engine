package org.max5.limbus;

import java.io.IOException;
import java.io.InputStream;

import org.max5.limbus.utils.Lang;

public class InputStreamWrapper extends InputStream {

  boolean wasClosed = false;
  InputStream delegate;

  public InputStreamWrapper(InputStream delegate) {
    super();
    Lang.denyNull("delegate", delegate);
    this.delegate = delegate;
  }

  @Override
  public int read() throws IOException {
    return delegate.read();
  }

  @Override
  public void close() throws IOException {
    wasClosed = true;
    delegate.close();
  }

  public boolean WasClosed() {
    return wasClosed;
  }

  @Override
  public String toString() {
    return "InputStreamWrapper [wasClosed=" + wasClosed + ", delegate=" + delegate + "]";
  }

  @Override
  public int hashCode() {
    return delegate.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return delegate.equals(obj);
  }

}
