package com.remondis.limbus.events;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.security.AccessController;
import java.security.PrivilegedAction;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AsyncMulticastHandler<I> extends MulticastHandler<I> implements AsyncEventMulticaster<I> {

  private ExecutorService executor;

  AsyncMulticastHandler(Class subscriberInterface) {
    super(subscriberInterface);
    this.executor = AccessController.doPrivileged(new PrivilegedAction<ExecutorService>() {

      @Override
      public ExecutorService run() {
        return Executors.newSingleThreadExecutor();
      }
    });
  }

  @Override
  protected InvocationHandler createExceptionInvocationHandler() {
    InvocationHandler synchronousHandler = super.createExceptionInvocationHandler();
    return new InvocationHandler() {

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

          @Override
          public Void run() {
            executor.submit(new Runnable() {

              @Override
              public void run() {
                try {
                  synchronousHandler.invoke(proxy, method, args);
                } catch (InvocationTargetException e) {
                  // Translate to the cause of the invocation target exception because thats the business logic
                  // exception.
                  throw new RuntimeException(e.getCause());
                } catch (Throwable e) {
                  throw new RuntimeException("Unexpected exception while asynchronously multicast event.", e);
                }
              }
            });
            return null;
          }
        });
        return null;
      }
    };
  }

  @Override
  protected InvocationHandler createSilentInvocationHandler() {
    InvocationHandler synchronousHandler = super.createSilentInvocationHandler();
    return new InvocationHandler() {

      @Override
      public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        AccessController.doPrivileged(new PrivilegedAction<Void>() {

          @Override
          public Void run() {
            executor.submit(new Runnable() {

              @Override
              public void run() {
                try {
                  synchronousHandler.invoke(proxy, method, args);
                } catch (InvocationTargetException e) {
                  logInvocationError(e.getCause());
                } catch (Throwable e) {
                  logInvocationError(e.getCause());
                }
              }

              protected void logInvocationError(Throwable e) {
                log.debug("Unexpected exception while asynchronously multicast event silently.", e);
              }
            });
            return null;
          }
        });
        return null;
      }
    };
  }

  @Override
  public void close() throws Exception {
    AccessController.doPrivileged(new PrivilegedAction<Void>() {

      @Override
      public Void run() {
        executor.shutdown();
        return null;
      }
    });
  }

}
