/**
 *
 */
package com.remondis.limbus.api;

import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.Test;

/**
 * @author schuettec
 *
 */
public class InitializableTest {

  @Test
  public void test_init_multiple_times() throws Exception {
    assertThrows(AlreadyInitializedException.class, () -> {
      Initializable<Exception> mock = new Initializable<Exception>() {

        @Override
        protected void performInitialize() throws Exception {
        }

        @Override
        protected void performFinish() {
        }
      };
      mock.initialize();
      mock.initialize();
    });
  }

  @Test
  public void test_Finish_after_exception_on_init() throws Exception {
    final Exception exception = new Exception("Thrown by JUnit test for test purposes.");
    final AtomicBoolean wasFinished = new AtomicBoolean(false);
    Initializable<Exception> mock = new Initializable<Exception>() {

      @Override
      protected void performInitialize() throws Exception {
        throw exception;
      }

      @Override
      protected void performFinish() {
        wasFinished.set(true);
      }
    };

    try {
      mock.initialize();
    } catch (Exception e) {
      // Exception totally expected
      assertSame(exception, e);
    }

    try {
      mock.checkState();
      fail("NotInitializedException was expected but not thrown.");
    } catch (NotInitializedException e) {
      // Expected
    }

    // Finished should have been called.
    assertTrue(wasFinished.get());

    try {
      mock.checkState();
      fail("NotInitializedException was expected but not thrown.");
    } catch (NotInitializedException e) {
      // Expected
    }
  }

  @Test
  public void test_NotInitializedAfterExceptionInFinish() throws Exception {
    assertThrows(NotInitializedException.class, () -> {
      Initializable<Exception> testObject = new Initializable<Exception>() {

        @Override
        protected void performInitialize() throws Exception {
        }

        @Override
        protected void performFinish() {
          throw new DummyRuntimeException("Thrown for test purposes.");
        }
      };

      testObject.initialize();
      testObject.checkState();
      try {
        testObject.finish();
      } catch (Exception e) {
        // Expected!
      }

      testObject.checkState();
    });
  }

  @Test
  public void test_UseWithoutInit() {
    assertThrows(NotInitializedException.class, () -> {
      Initializable<Exception> testObject = new Initializable<Exception>() {

        @Override
        protected void performInitialize() throws Exception {
        }

        @Override
        protected void performFinish() {
        }

      };

      testObject.checkState();
    });
  }

  @Test
  public void test_NotInitializedAfterFailedPerformInit() {
    Initializable<Exception> testObject = new Initializable<Exception>() {

      @Override
      protected void performInitialize() throws Exception {
        throw new Exception("Exception for testing");
      }

      @Override
      protected void performFinish() {
      }

    };

    try {
      testObject.initialize();
      fail("Exception was expected.");
    } catch (Exception e) {
      // Expected exception
    }

    try {
      testObject.checkState();
      fail("NotInitializedException was expected but not thrown!");
    } catch (NotInitializedException e) {
    }

  }

  @Test
  public void test_uninitedAfterFinish() throws Exception {
    assertThrows(NotInitializedException.class, () -> {
      Initializable<Exception> testObject = new Initializable<Exception>() {

        @Override
        protected void performInitialize() throws Exception {
        }

        @Override
        protected void performFinish() {
        }

      };

      try {
        testObject.initialize();
        testObject.checkState();
        testObject.finish();
      } catch (Exception e) {
        fail("Unexpected exception.");
      }

      // The NotInitializedException is expected here:
      testObject.checkState();
    });
  }

  @Test
  public void test_finish_without_init() throws Exception {
    Initializable<Exception> testObject = new Initializable<Exception>() {

      @Override
      protected void performInitialize() throws Exception {
      }

      @Override
      protected void performFinish() {
      }

    };

    testObject.finish();
  }

  @Test
  public void test_finish_twice_after_init() throws Exception {
    Initializable<Exception> testObject = new Initializable<Exception>() {

      @Override
      protected void performInitialize() throws Exception {
      }

      @Override
      protected void performFinish() {
      }

    };

    testObject.initialize();
    testObject.finish();
    testObject.finish();
  }

}
