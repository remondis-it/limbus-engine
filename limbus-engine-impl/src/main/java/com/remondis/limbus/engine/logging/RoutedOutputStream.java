package com.remondis.limbus.engine.logging;

import java.io.IOException;
import java.io.OutputStream;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import com.remondis.limbus.utils.Lang;

/**
 * This class implements a routed print stream that handles multiple underlying print streams. A routing function
 * decides which call is mapped to which underlying print stream. This implementation acts like a demultiplexer that
 * splits a single stream into multiple target streams.
 *
 * <p>
 * <b>
 * After {@link #close()} was called this {@link RoutedOutputStream} cannot be used or reused.
 * </b>
 * </p>
 *
 * @param <ID>
 *        The datatype used to identify a target.
 * @param <T>
 *        The type of the targets
 *
 * @author schuettec
 *
 */
public class RoutedOutputStream<ID, T> extends OutputStream {

  protected Map<ID, T> targets;
  protected T defaultTarget;

  private TargetSelector<ID, T> selector;
  private TargetWriter<T> writer;

  /**
   * Constructs a routed output stream that delegates to a default target if no registered target was found with a
   * specified id.
   *
   * @param selector
   *        The selector function to select a target.
   * @param writer
   *        The target writer implementation
   */
  public RoutedOutputStream(TargetSelector<ID, T> selector, TargetWriter<T> writer) {
    this(selector, writer, null);
  }

  /**
   * Constructs a routed output stream that delegates to a default target if no registered target was found with a
   * specified id.
   *
   * @param selector
   *        The selector function to select a target.
   * @param writer
   *        The target writer implementation
   *
   * @param defaultTarget
   *        (Optionally) A default target to write to if there is no registered target for a specified id.
   */
  public RoutedOutputStream(TargetSelector<ID, T> selector, TargetWriter<T> writer, T defaultTarget) {
    Lang.denyNull("selector", selector);
    Lang.denyNull("writer", writer);
    this.selector = selector;
    this.writer = writer;
    this.targets = new ConcurrentHashMap<>();
    this.defaultTarget = defaultTarget;
  }

  public void removeTarget(ID id) {
    this.targets.remove(id);
  }

  private T selectTarget() {
    Map<ID, T> readOnlyCopy = Collections.unmodifiableMap(targets);
    T target = selector.selectTarget(readOnlyCopy);
    if (target == null) {
      target = defaultTarget;
    }
    return target;
  }

  @Override
  public void write(int b) throws IOException {
    // Simply select target and delegate
    T target = selectTarget();
    if (target != null) {
      this.writer.writeTo(b, target);
    }
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    // Simply select target and delegate
    T target = selectTarget();
    if (target != null) {
      this.writer.writeTo(b, off, len, target);
    }
  }

  @Override
  public void flush() throws IOException {
    boolean wasError = false;
    IOException exception = new IOException("Cannot flush targets.");
    if (defaultTarget != null) {
      try {
        writer.flush(defaultTarget);
      } catch (Exception e) {
        exception.addSuppressed(e);
        wasError = true;
      }
    }

    for (T target : targets.values()) {
      try {
        writer.flush(target);
      } catch (Exception e) {
        exception.addSuppressed(e);
        wasError = true;
      }
    }

    if (wasError) {
      throw exception;
    }
  }

  @Override
  public void close() throws IOException {
    boolean wasError = false;
    IOException exception = new IOException("Cannot flush targets.");

    // Close all targets
    // Clean all references.
    try {
      if (defaultTarget != null) {
        try {
          writer.close(defaultTarget);
        } catch (Exception e) {
          exception.addSuppressed(e);
          wasError = true;
        }
      }

      for (T target : targets.values()) {
        try {
          writer.close(target);
        } catch (Exception e) {
          exception.addSuppressed(e);
          wasError = true;
        }
      }
    } finally {
      targets.clear();
      defaultTarget = null;
      selector = null;
      targets = null;
      writer = null;
    }

    if (wasError) {
      throw exception;
    }

  }

  /**
   * Gets a target by id
   *
   * @param id
   *        The id
   * @return Returns a previously added target or <code>null</code>, if it does not exist.
   */
  public T getTarget(ID id) {
    return targets.get(id);
  }

  public int size() {
    return targets.size();
  }

  public boolean isEmpty() {
    return targets.isEmpty();
  }

  public boolean containsTargetID(ID key) {
    return targets.containsKey(key);
  }

  public T addTarget(ID id, T target) {
    return this.targets.put(id, target);
  }

  public void clear() {
    targets.clear();
  }

  public Set<ID> keySet() {
    return targets.keySet();
  }

}
