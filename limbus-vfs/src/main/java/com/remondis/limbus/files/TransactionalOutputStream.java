package com.remondis.limbus.files;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * This {@link OutputStream} implementation adds new content to a {@link InMemoryFilesystemImpl}-filesystem on
 * {@link #close()}.
 *
 * @author schuettec
 *
 */
public class TransactionalOutputStream extends OutputStream {

  private InMemoryFilesystemImpl memoryFS;
  private ByteArrayOutputStream stream;
  private String path;

  public TransactionalOutputStream(String path, InMemoryFilesystemImpl memoryFS) {
    this.memoryFS = memoryFS;
    this.path = path;
    this.stream = new ByteArrayOutputStream();
  }

  @Override
  public void write(int b) throws IOException {
    stream.write(b);
  }

  @Override
  public void write(byte[] b) throws IOException {
    stream.write(b);
  }

  @Override
  public void write(byte[] b, int off, int len) throws IOException {
    stream.write(b, off, len);
  }

  @Override
  public void close() throws IOException {
    try {
      stream.close();
      byte[] fileContent = stream.toByteArray();
      memoryFS.addContent(path, fileContent);
    } finally {
      stream = null;
      memoryFS = null;
    }
  }
}
