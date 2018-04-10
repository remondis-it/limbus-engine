/**
 *
 */
package org.max5.limbus.utils;

import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.Array;
import java.net.URL;
import java.util.logging.LogManager;

/**
 * A utility class to provide some language features.
 *
 * @author schuettec
 *
 */
public class Lang {

  /**
   * Initializes the JDK logger and loads the configuration file using the specified {@link URL}.
   *
   * @param url
   *        The url to be used as logging configuration.
   */
  public static void initializeJDKLogging(URL url) {
    denyNull("url", url);
    try {
      LogManager.getLogManager()
          .readConfiguration(url.openStream());
    } catch (SecurityException | IOException e) {
      System.err.println("Cannot apply logging configuration.");
      e.printStackTrace();
    }
  }

  /**
   * Initializes the JDK logger and loads the configuration file </b>logging.properties</b> from classpath's root
   * folder.
   */
  public static void initializeJDKLogging() {
    InputStream logStream = Lang.class.getResourceAsStream("/logging.properties");
    if (logStream == null) {
      System.err.println("Cannot load logging configuration.");
    }
    try {
      LogManager.getLogManager()
          .readConfiguration(logStream);
    } catch (SecurityException | IOException e) {
      System.err.println("Cannot apply logging configuration.");
      e.printStackTrace();
    }
  }

  /**
   * Performs a range check of the specified index and throws an {@link IllegalArgumentException} if the specified index
   * exceeds the array bounds.
   *
   * @param index
   *        The index to access the array.
   * @param array
   *        The array to perform the range check on.
   */

  public static void denyOutOfBounds(int index, Object array) {
    denyNull("array", array);

    int length = -1;
    if (array.getClass()
        .isArray()) {
      length = Array.getLength(array);
    } else {
      throw new IllegalArgumentException("The specified object is not an array.");
    }

    if (index < 0 || index >= length) {
      throw new IllegalArgumentException(String.format(
          "The specified index exceeds the array's bounds. Valid bounds: 0 <= index < %d, but index was %s.", length,
          index));
    }
  }

  /**
   * This method returns the object specified by actual if it is not <code>null</code>. If actual is <code>null</code>
   * the default value is returned.
   *
   * @param actual
   *        The current value.
   * @param defaultValue
   *        The default.
   * @return Returns actual if not null, otherwise default value is returned.
   */
  public static <T> T defaultIfNull(T actual, T defaultValue) {
    if (actual == null) {
      return defaultValue;
    } else {
      return actual;
    }
  }

  /**
   * This method returns the string specified by actual if it is not <code>null</code> or empty. If actual is
   * <code>null</code> or empty the default string is returned.
   *
   * @param actual
   *        The current value.
   * @param defaultValue
   *        The default.
   * @return Returns actual if not null or empty, otherwise default value is returned.
   */
  public static String defaultIfNull(String actual, String defaultValue) {
    if (isEmpty(actual)) {
      return defaultValue;
    } else {
      return actual;
    }
  }

  /**
   * @param string
   *        The string.
   * @return Returns <code>true</code> if the specified string is <code>null</code> or empty.
   */
  public static boolean isEmpty(String string) {
    return string == null || string.trim()
        .isEmpty();
  }

  /**
   * The default buffer size ({@value}) to use for
   * {@link #copy(InputStream, OutputStream)}
   * and
   * {@link #copy(Reader, Writer)}
   */
  private static final int DEFAULT_BUFFER_SIZE = 1024 * 4;
  private static final int EOF = -1;

  /**
   * This method throws an {@link IllegalArgumentException} if the specified string is null or empty.
   *
   * @param fieldName
   *        The parameter name.
   * @param argument
   *        The string to check-
   * @return Returns the argument.
   * @throws IllegalArgumentException
   *         Thrown with a detailed message if the string is <code>null</code> or empty. Returns immediately
   *         otherwise.
   */
  public static String denyNullOrEmpty(String fieldName, String argument) throws IllegalArgumentException {
    denyNull(fieldName, argument);
    if (argument.trim()
        .isEmpty()) {
      throw new IllegalArgumentException(String.format("Argument %s may not be empty.", fieldName));
    }
    return argument;
  }

  /**
   * This method throws an {@link IllegalArgumentException} if the specified argument is null.
   *
   * @param fieldName
   *        The parameter name.
   * @param argument
   *        The actual argument.
   * @return Returns the argument
   * @throws IllegalArgumentException
   *         Thrown with a detailed message if argument is <code>null</code>. Returns immediately otherwise.
   */
  public static <T> T denyNull(String fieldName, T argument) throws IllegalArgumentException {
    if (argument == null) {
      if (fieldName == null) {
        throw new IllegalArgumentException("Argument may not be null.");
      } else {
        throw new IllegalArgumentException(String.format("Argument %s may not be null.", fieldName));
      }
    }
    return argument;
  }

  /**
   * This method is used for reflective operations. It checks the specified object to be of a specific type.
   * If the object is not an instance of the specified type, an {@link IllegalTypeException}.
   *
   * @param expectedType
   *        The expected type.
   * @param object
   *        The object to be checked.
   * @return Returns the object.
   * @throws IllegalTypeException
   *         Thrown if the object is not an instance of the expected type.
   */
  public static Object denyWrongType(Class<?> expectedType, Object object) throws IllegalTypeException {
    if (!expectedType.isInstance(object)) {
      throw new IllegalTypeException("The specified object is not an instance of type " + expectedType.getName());
    }
    return object;
  }

  /**
   * Closes the specified {@link Closeable}s without throwing exception.
   *
   * @param closeables
   *        The closeable.
   */
  public static void closeQuietly(Closeable... closeables) {
    for (Closeable c : closeables) {
      closeQuietly(c);
    }
  }

  /**
   * Closes the specified {@link Closeable} without throwing exception.
   *
   * @param closeable
   *        The closeable.
   */
  public static void closeQuietly(Closeable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (Exception e) {
      // Nothing to do.
    }
  }

  /**
   * Closes the specified {@link AutoCloseable} without throwing exception.
   *
   * @param closeables
   *        The closeables.
   */
  public static void closeQuietly(AutoCloseable... closeables) {
    for (AutoCloseable c : closeables) {
      closeQuietly(c);
    }
  }

  /**
   * Closes the specified {@link AutoCloseable} without throwing exception.
   *
   * @param closeable
   *        The closeable.
   */
  public static void closeQuietly(AutoCloseable closeable) {
    try {
      if (closeable != null) {
        closeable.close();
      }
    } catch (Exception e) {
      // Nothing to do.
    }
  }

  /**
   * Copies an input stream into a byte array.
   *
   * @param input
   *        The input stream.
   * @return Returns the byte array
   * @throws IOException
   *         Thrown on any IO Error.
   */
  public static byte[] toByteArray(InputStream input) throws IOException {
    ByteArrayOutputStream output = new ByteArrayOutputStream();
    try {
      copy(input, output);
    } catch (IOException e) {
      throw e;
    } finally {
      Lang.closeQuietly(output);
    }
    return output.toByteArray();
  }

  /**
   * Copy bytes from a large (over 2GB) <code>InputStream</code> to an
   * <code>OutputStream</code>.
   * <p>
   * This method uses the provided buffer, so there is no need to use a
   * <code>BufferedInputStream</code>.
   * <p>
   *
   * @param input
   *        the <code>InputStream</code> to read from
   * @param output
   *        the <code>OutputStream</code> to write to
   * @return the number of bytes copied
   * @throws NullPointerException
   *         if the input or output is null
   * @throws IOException
   *         if an I/O error occurs
   * @since 2.2
   */
  public static long copy(InputStream input, OutputStream output) throws IOException {
    long count = 0;
    int n = 0;
    byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
    while (EOF != (n = input.read(buffer))) {
      output.write(buffer, 0, n);
      count += n;
    }
    return count;
  }

  /**
   * @param string
   * @return Returns <code>true</code> if the specified string is a numeric value. <code>false</code> otherwise.
   */
  public static boolean isNumeric(String string) {
    try {
      Integer.parseInt(string);
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  /**
   * Einige APIs fangen ungünstigerweise Throwable. Das ist gefährlich, denn
   * damit werden auch Throwables vom Typ Error gefangen, was man tunlichst
   * vermeiden sollte. Siehe hier {@link Error}
   *
   * Diese Methode nimmt ein {@link Throwable} entgegen und wirft entweder eine {@link Exception} oder ein {@link Error}
   * je nach Typ des übergebenen Parameters.
   *
   * @param t
   *        Das Objekt vom Typ Throwable
   * @throws Exception
   *         Das Throwable wird als Exception geworfen, wenn es kein Error
   *         ist. Ist das Objekt vom Typ {@link Error} so wird es an die
   *         JVM weitergegeben.
   */
  public static void handleThrowableToAvoidJVMErrorSupression(Throwable t) throws Exception {
    if (t == null) {
      return;
    }
    throw getExceptionFromThrowable(t);
  }

  /**
   * Einige APIs fangen ungünstigerweise Throwable. Das ist gefährlich, denn
   * damit werden auch Throwables vom Typ Error gefangen, was man tunlichst
   * vermeiden sollte. Siehe hier {@link Error}
   *
   * Diese Methode nimmt ein {@link Throwable} entgegen und gibt entweder eine {@link Exception} zurück oder wirft ein
   * {@link Error}
   * je nach Typ des übergebenen Parameters.
   *
   * @param t
   *        The throwable
   * @return Returns the {@link Exception} if the specified object is an exception. In case the specified object is an
   *         {@link Error} this method throws an Error.
   */
  public static Exception getExceptionFromThrowable(Throwable t) {
    // Prüfen ob t ein Error ist
    if (t instanceof Error) {
      // Throw as error
      throw (Error) t;
    } else {
      // Throw as exception
      return (Exception) t;
    }
  }

  /**
   * Checks the specified array (or vararg) if it is null or empty. If the array is null or its length is 0 an
   * {@link IllegalArgumentException} is thrown.
   *
   * @param fieldName
   *        The field name.
   * @param array
   *        The array to check.
   */
  public static <T> void denyNullOrEmpty(String fieldName, T[] array) {
    if (isNullOrEmpty(array)) {
      throw new IllegalArgumentException(String.format("Argument %s may not be empty.", fieldName));
    }
  }

  /**
   * Checks if an array is null or empty.
   *
   * @param array
   *        The array to check
   * @return Returns <code>true</code> if the array is null or empty. <code>false</code> otherwise.
   */
  public static <T> boolean isNullOrEmpty(T[] array) {
    return (array == null || array.length == 0);
  }

  /**
   * Prints the specified stacktrace to the {@link PrintStream} object.
   *
   * @param stackTrace
   *        The stacktrace
   * @param s
   *        The {@link PrintStream} as target.
   */
  public static void printStackTrace(StackTraceElement[] stackTrace, PrintStream s) {
    for (int i = 0; i < stackTrace.length; i++) {
      s.println("\tat " + stackTrace[i]);
    }
  }

  /**
   * Converts the stacktrace of an exception to string.
   *
   * @param exception
   *        The exception
   * @return The stacktrace of the exception as string.
   */
  public static String exceptionAsString(Exception exception) {
    Lang.denyNull("exception", exception);
    StringWriter sw = new StringWriter();
    PrintWriter pw = new PrintWriter(sw);
    exception.printStackTrace(pw);
    String exceptionStackTrace = sw.toString();
    return exceptionStackTrace;
  }

  /**
   * Converts the specified stacktrace to a string representation. Useful to get rid of class references that hold their
   * classloaders.
   *
   * @param stackTrace
   *        The stacktrace
   * @return Returns the stacktracte represented as sting.
   */
  public static String stackTraceAsString(StackTraceElement[] stackTrace) {
    StringBuilder b = new StringBuilder();
    for (int i = 0; i < stackTrace.length; i++) {
      b.append(String.format("\tat %s\n", stackTrace[i]));
    }
    return b.toString();
  }

}
