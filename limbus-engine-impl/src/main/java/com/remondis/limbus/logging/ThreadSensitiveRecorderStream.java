package com.remondis.limbus.logging;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.remondis.limbus.utils.Lang;

/**
 * This is an implementation of {@link OutputStream} that is used to record the output of a plugin that is produced by a
 * specified thread. This stream filters the output from the specified {@link OutputStream} if it was produced by a
 * specified thread running specified plugin code and writes a copy to the registered {@link ByteArrayOutputStream}.
 * This class
 *
 * @author schuettec
 *
 */
public class ThreadSensitiveRecorderStream extends OutputStream {

  @FunctionalInterface
  interface SubscriberOperation {
    public void operation(OutputStream target) throws IOException;
  }

  private Map<ClassloaderThreadID, ByteArrayOutputStream> recordSubscriber;

  private OutputStream delegate;

  public ThreadSensitiveRecorderStream(OutputStream delegate) {
    Lang.denyNull("delegate", delegate);
    this.delegate = delegate;
    this.recordSubscriber = new ConcurrentHashMap<ClassloaderThreadID, ByteArrayOutputStream>();
  }

  @Override
  public void write(int b) throws IOException {
    delegate.write(b);
    delegateOnDemand((o) -> {
      o.write(b);
    });
  }

  @Override
  public void write(byte[] b) throws IOException {
    delegate.write(b);
    delegateOnDemand((o) -> {
      o.write(b);
    });
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    delegate.write(b, off, len);
    delegateOnDemand((o) -> {
      o.write(b, off, len);
    });
  }

  @Override
  public void flush() throws IOException {
    delegate.flush();
    delegateOnDemand((o) -> {
      o.flush();
    });
  }

  @Override
  public void close() throws IOException {
    delegate.close();
    delegateOnDemand((o) -> {
      o.close();
    });
  }

  private void delegateOnDemand(SubscriberOperation consumer) throws IOException {
    ClassloaderThreadID subscriberID = getSubscriberID();
    if (recordSubscriber.containsKey(subscriberID)) {
      OutputStream outputStream = recordSubscriber.get(subscriberID);
      consumer.operation(outputStream);
    }
  }

  private ClassloaderThreadID getSubscriberID() {
    int classLoaderHashCode = ContextClassloaderSelector.getCurrentClassLoaderHashCode();
    int threadHashCode = ContextClassloaderSelector.getCurrentThreadHashCode();
    return ClassloaderThreadID.getID(classLoaderHashCode, threadHashCode);
  }

  public int size() {
    return recordSubscriber.size();
  }

  public boolean isEmpty() {
    return recordSubscriber.isEmpty();
  }

  public boolean hasSubscriber(ClassloaderThreadID key) {
    return recordSubscriber.containsKey(key);
  }

  public ByteArrayOutputStream getSubscriber(ClassloaderThreadID key) {
    return recordSubscriber.get(key);
  }

  public ByteArrayOutputStream addSubscriber(ClassloaderThreadID key, ByteArrayOutputStream value) {
    return recordSubscriber.put(key, value);
  }

  public ByteArrayOutputStream removeSubscriber(ClassloaderThreadID key) {
    return recordSubscriber.remove(key);
  }

  public void clear() {
    recordSubscriber.clear();
  }

  public Set<ClassloaderThreadID> keySet() {
    return recordSubscriber.keySet();
  }

}
