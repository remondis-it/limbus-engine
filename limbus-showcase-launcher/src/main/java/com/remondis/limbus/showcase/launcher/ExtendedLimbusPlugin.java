package com.remondis.limbus.showcase.launcher;

import java.io.IOException;

import com.remondis.limbus.api.LimbusPlugin;

/**
 * This is the {@link LimbusPlugin} type extension defining some new plugin
 * methods.
 *
 * 
 *
 */
public interface ExtendedLimbusPlugin extends LimbusPlugin {

  /**
   * @return Returns the time in milliseconds since the plugin was initialized.
   */
  public long getRuntimeInMilliseconds();

  /**
   * @param object
   *        The object to set on this plugin.
   */
  public void setObject(Object object);

  /**
   * @return Returns the current object.
   */
  public Object getObject();

  /**
   * @param message
   *        Writes this message to std/out.
   * @throws IOException
   */
  public void writeToSystemOut(byte[] message) throws IOException;

  /**
   * @param message
   *        Writes this message to std/err.
   * @throws IOException
   */
  public void writeToSystemErr(byte[] message) throws IOException;

}
