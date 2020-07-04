package com.remondis.limbus.engine.api;

/**
 * A simple thread-safe veto implementation.
 *
 * 
 *
 */
public class SimpleVeto implements Veto {

  protected boolean active = true;
  protected boolean confirmed = true;

  protected Object lock = new Object();

  /**
   * Returns the results of the veto object and denies further uses.
   *
   * @return Returns <code>true</code> if the vote passed with no vetos given, otherwise <code>false</code> is
   *         returned.
   */
  public boolean isConfirmed() {
    synchronized (lock) {
      active = false;
      return confirmed;
    }
  }

  @Override
  public void veto() {
    synchronized (lock) {
      if (active) {
        confirmed = false;
      } else {
        throw new IllegalStateException("This veto object cannot be used, the vote is passed.");
      }
    }
  }

}
