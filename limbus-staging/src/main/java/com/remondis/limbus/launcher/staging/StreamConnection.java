/**
 *
 */
package com.remondis.limbus.launcher.staging;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URL;
import java.net.URLConnection;
import java.security.Permission;

/**
 * This class is used by the ReBind stream handler.
 *
 * @author schuettec
 *
 */
public class StreamConnection extends URLConnection {

  private InputStream inputStream;
  private String contentType;
  private int contentLength;
  private byte[] fileContent;

  public StreamConnection(URL u, byte[] fileContent) {
    super(u);
    this.fileContent = fileContent;
  }

  @Override
  public Permission getPermission() throws IOException {
    return null;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.net.URLConnection#connect()
   */
  @Override
  public void connect() throws IOException {
    try {
      this.inputStream = new ByteArrayInputStream(fileContent);
      FileNameMap map = java.net.URLConnection.getFileNameMap();
      this.contentType = map.getContentTypeFor(url.getFile());
      this.contentLength = fileContent.length;

    } catch (Exception e) {
      throw new IOException(String.format("Cannot deliver in-memory resource %s", this.url.toString()), e);
    }

  }

  @Override
  public int getContentLength() {
    return contentLength;
  }

  @Override
  public long getContentLengthLong() {
    return contentLength;
  }

  @Override
  public String getContentType() {
    return contentType;
  }

  @Override
  public long getLastModified() {
    return System.currentTimeMillis();
  }

  /**
   * @return the inputStream
   */
  @Override
  public InputStream getInputStream() {
    return inputStream;
  }

}
