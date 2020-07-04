package com.remondis.limbus.launcher;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import com.remondis.limbus.utils.Lang;

/**
 * This class wrappes objects of type {@link PrintStream}. This class is used to proxy std/out and std/err streams.
 * Therefore the close operation is not supported.
 *
 * 
 *
 */
final class WrappedOutputStream extends OutputStream {

  private Lock delegateLock = new ReentrantLock();
  private OutputStream delegate;

  protected WrappedOutputStream(OutputStream delegate) {
    setDelegate(delegate);
  }

  protected void setDelegate(OutputStream delegate) {
    Lang.denyNull("delegate", delegate);
    delegateLock.lock();
    try {
      this.delegate = delegate;
    } finally {
      delegateLock.unlock();
    }
  }

  @Override
  public void write(int b) throws IOException {
    delegateLock.lock();
    try {
      delegate.write(b);
    } finally {
      delegateLock.unlock();
    }
  }

  @Override
  public void write(byte[] b) throws IOException {
    delegateLock.lock();
    try {
      delegate.write(b);
    } finally {
      delegateLock.unlock();
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    delegateLock.lock();
    try {
      delegate.write(b, off, len);
    } finally {
      delegateLock.unlock();
    }
  }

  @Override
  public void flush() throws IOException {
    delegateLock.lock();
    try {
      delegate.flush();
    } finally {
      delegateLock.unlock();
    }
  }

  @Override
  public void close() throws IOException {
    delegateLock.lock();
    try {
      delegate.flush();
      // DO NOT DELEGATE THE CLOSE
      // delegate.close();
    } finally {
      delegateLock.unlock();
    }
  }

}
