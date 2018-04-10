package org.max5.limbus;

import java.lang.ref.ReferenceQueue;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class maintains a {@link ReferenceQueue} to determine the time when an object is garbage collected. This class
 * is mainly used to observe references to {@link PluginClassLoader}.
 *
 * @author schuettec
 *
 */
class LimbusReferenceObserver<T> extends Initializable<RuntimeException> {

  private static final Logger log = LoggerFactory.getLogger(LimbusReferenceObserver.class);

  private List<ObservedPhantomReference<T>> references;
  private ReferenceQueue<T> queue;
  private Thread referenceObserver;

  LimbusReferenceObserver() {
  }

  public void observeReferenceTo(T object) {
    if (object == null) {
      return;
    }
    checkState();
    ObservedPhantomReference<T> observedRef = new ObservedPhantomReference<T>(object, queue);
    this.references.add(observedRef);
    log.info("Observing a new object of type {} for garbage collection.", object.getClass()
        .getName());
  }

  @Override
  protected void performInitialize() throws RuntimeException {
    this.queue = new ReferenceQueue<T>();
    this.references = new LinkedList<ObservedPhantomReference<T>>();
    this.referenceObserver = new Thread("Limbus Reference Observer") {
      @SuppressWarnings("unchecked")
      @Override
      public void run() {
        log.info("Limbus Reference Observer started.");
        while (!Thread.interrupted()) {
          ObservedPhantomReference<T> reference;
          try {
            log.info("Waiting for objects to be enqueued.");
            reference = (ObservedPhantomReference<T>) queue.remove();
            references.remove(reference);
            reference.clear();
            String objectType = reference.getObjectType();
            long difference = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - reference.getObservedSince());
            log.info("An object of type {} was enqueued for garbage collection after {}s.", objectType, difference);
            // log.info("There are {} observed references left.", references.size());
          } catch (InterruptedException e) {
            Thread.currentThread()
                .interrupt();
          }
        }
        log.info("Limbus Reference Observer was terminated.");
      }
    };
    this.referenceObserver.start();
  }

  @Override
  protected void performFinish() {
    try {
      this.referenceObserver.interrupt();
      this.referenceObserver.join();
    } catch (InterruptedException e) {
    } finally {
      this.referenceObserver = null;
      this.references.clear();
      this.references = null;
    }
  }

}
