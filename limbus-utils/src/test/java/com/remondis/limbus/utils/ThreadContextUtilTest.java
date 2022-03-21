package com.remondis.limbus.utils;

import static java.util.Objects.nonNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

import java.lang.Thread.UncaughtExceptionHandler;
import java.util.concurrent.atomic.AtomicReference;

import org.junit.jupiter.api.Test;

public class ThreadContextUtilTest {

  private static final int TEST_INT = 100;
  private static final String TEST_STRING = "string";

  @Test
  public void shouldAddAndRemoveThreadBean() {
    AtomicReference<Throwable> throwableHolder = new AtomicReference<Throwable>();

    Thread t = new Thread(new Runnable() {

      @Override
      public void run() {
        assertFalse(ThreadContextUtil.hasThreadContext(MyBean.class));
        MyBean bean = ThreadContextUtil.getThreadContext(MyBean.class);
        assertNotNull(bean);
        bean.setString(TEST_STRING);
        bean.setInteger(TEST_INT);
        bean = ThreadContextUtil.getThreadContext(MyBean.class);
        assertEquals(TEST_STRING, bean.getString());
        assertEquals(TEST_INT, bean.getInteger());
        ThreadContextUtil.clearThreadContext();
        assertFalse(ThreadContextUtil.hasThreadContext(MyBean.class));
      }
    });
    t.setUncaughtExceptionHandler(new UncaughtExceptionHandler() {

      @Override
      public void uncaughtException(Thread t, Throwable e) {
        throwableHolder.set(e);
      }
    });
    t.start();
    try {
      t.join();
      Throwable throwable = throwableHolder.get();
      if (nonNull(throwable)) {
        throw throwable;
      }
    } catch (AssertionError e) {
      throw e;
    } catch (Throwable throwable) {
      fail(throwable);
    }

  }

}
