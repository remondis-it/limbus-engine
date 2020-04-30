package com.remondis.limbus.engine;

import java.lang.ref.PhantomReference;
import java.lang.ref.ReferenceQueue;

/**
 * This reference is an extension of {@link WeakReference}. It stores some leight-weight information about the
 * referenced object to make identifying easier when the object is enqueued.
 *
 * @author schuettec
 *
 * @param <T>
 */
class ObservedPhantomReference<T> extends PhantomReference<T> {
  // Do not hold any references to Class or Classloaders here. Only primitive types allowed.
  private String objectType;

  /**
   * Holds the timestamp from when this reference was created.
   */
  private long observedSince;

  ObservedPhantomReference(T referent, ReferenceQueue<? super T> q) {
    super(referent, q);
    this.objectType = referent.getClass()
        .getName();
    this.observedSince = System.nanoTime();
  }

  public String getObjectType() {
    return objectType;
  }

  public long getObservedSince() {
    return observedSince;
  }

}