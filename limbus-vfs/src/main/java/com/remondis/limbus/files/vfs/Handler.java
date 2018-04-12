/**
 *
 */
package com.remondis.limbus.files.vfs;

import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;

import com.remondis.limbus.files.InMemoryFilesystemImpl;
import com.remondis.limbus.files.StreamConnection;

/**
 * This stream handler makes in-memory resources available via {@link URL}.
 *
 *
 * @author schuettec
 *
 */
public class Handler extends URLStreamHandler {

  static String CONTENT_LENGTH = "content-length";
  static String CONTENT_TYPE = "content-type";
  static String LAST_MODIFIED = "last-modified";

  private static Object lock = new Object();
  private static InMemoryFilesystemImpl memoryFs;

  public static void setMemoryFilesystem(InMemoryFilesystemImpl memoryFilesystem) {
    synchronized (lock) {
      memoryFs = memoryFilesystem;
    }
  }

  @Override
  protected URLConnection openConnection(URL u) throws IOException {
    synchronized (lock) {
      try {
        byte[] fileContent;
        fileContent = memoryFs.getFileContent(u.getPath());
        StreamConnection connection = new StreamConnection(u, fileContent);
        connection.connect();
        return connection;
      } catch (Exception e) {
        throw new IOException("Cannot connect to in-memory filesystem.", e);
      }
    }
  }

}
