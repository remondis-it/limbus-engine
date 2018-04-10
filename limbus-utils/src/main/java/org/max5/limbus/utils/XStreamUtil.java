package org.max5.limbus.utils;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.locks.ReentrantLock;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.io.HierarchicalStreamDriver;
import com.thoughtworks.xstream.io.ReaderWrapper;
import com.thoughtworks.xstream.io.xml.Xpp3DomDriver;

/**
 * The system configurator allows to read and write a Limbus System configuration serialized from and to a XML format.
 *
 * @author schuettec
 *
 */
public class XStreamUtil {
  /**
   * Note: This is a shared XStream instance. Every access to this instance is synchronized with ReentrantLock.
   * If you get performance issues implement a XStream object pool that is
   * synchronized.
   */
  private XStream xStream = null;
  private final ReentrantLock xStreamLock = new ReentrantLock();

  /**
   * This array enumerates all ReBind configuration classes that may be serialized and make use of XStream annotations.
   */
  private Class<?>[] annotatedClasses;

  /**
   * Creates a new access to an {@link XStream} instance. This constructor does not register any "known classes" on the
   * {@link XStream} instance, so the annotations that can be used are limited - see
   * {@link com.thoughtworks.xstream.XStream#autodetectAnnotations(boolean)} for more information.
   */
  public XStreamUtil() {
  }

  /**
   * This constructor creates a new access to an {@link XStream} instance. This constructor registers the specified
   * classes that make use of XStream annotations. The annotations of these classes will be processes before - see
   * {@link com.thoughtworks.xstream.XStream#autodetectAnnotations(boolean)} for more information.
   *
   * @param classes
   *        The classes to be annotation-processed.
   */
  public XStreamUtil(Class<?>... classes) {
    annotatedClasses = classes;
  }

  /**
   * Reads a serialized object using the current {@link XStream} instance. Converts the result to the specified expected
   * type.
   *
   * @param targetType
   *        The expected object type.
   * @param xmlInput
   *        The serialized input
   * @return Returns the deserialized object.
   * @throws SerializeException
   *         Thrown on any error.
   */
  public <T> T readObject(Class<T> targetType, InputStream xmlInput) throws SerializeException {
    Lang.denyNull("Target type", targetType);
    Lang.denyNull("Configuration input stream", xmlInput);

    try {
      xStreamLock.lock();
      XStream xstream = getXStream();
      HierarchicalStreamDriver driver = new Xpp3DomDriver();
      Object object = xstream.unmarshal(new ReaderWrapper(driver.createReader(xmlInput)) {
        @Override
        public String getValue() {
          return super.getValue().trim();
        }
      });
      // Object object = xstream.fromXML(xmlInput);
      xStreamLock.unlock();
      Lang.denyWrongType(targetType, object);
      return targetType.cast(object);
    } catch (Exception e) {
      throw new SerializeException("Cannot load object configuration.", e);
    } finally {
      Lang.closeQuietly(xmlInput);
    }
  }

  /**
   * This method lazy-initializes the shared XStream engine. The initialization is synchronized.
   *
   * <p>
   * Note: The XStream instance is shared for multiple thread access. XStream itself is <b>not thread-safe on
   * configuration<b/> - see {@link XStream#autodetectAnnotations(boolean)} for more information. If you get performance
   * issues see {@link #xStream};
   * </p>
   *
   * @return Returns the shared XStream instance.
   */
  public XStream getXStream() {
    xStreamLock.lock();
    if (xStream == null) {
      // XStream xstream = new XStream((HierarchicalStreamDriver)null);
      xStream = new XStream();
      if (annotatedClasses != null) {
        xStream.processAnnotations(annotatedClasses);
      }
      xStream.autodetectAnnotations(true);
    }
    xStreamLock.unlock();
    return xStream;
  }

  /**
   * Writes an object to the outputstream by converting it to its XML representation. This method closes the stream.
   *
   * @param object
   *        The object.
   * @param xmlOutput
   *        The target {@link OutputStream}
   * @throws SerializeException
   *         Thrown on any error.
   */
  public void writeObject(Object object, OutputStream xmlOutput) throws SerializeException {
    Lang.denyNull("Object", object);
    Lang.denyNull("Configuration output stream", xmlOutput);
    try {
      xStreamLock.lock();
      XStream xstream = getXStream();
      xstream.toXML(object, xmlOutput);
      xStreamLock.unlock();
    } catch (Exception e) {
      throw new SerializeException("Cannot write object configuration.", e);
    } finally {
      Lang.closeQuietly(xmlOutput);
    }
  }

}
