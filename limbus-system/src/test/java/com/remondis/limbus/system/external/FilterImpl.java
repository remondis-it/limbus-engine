package com.remondis.limbus.system.external;

import com.remondis.limbus.api.Initializable;

/**
 * 
 *
 */
public class FilterImpl extends Initializable<RuntimeException> implements Filter {

  @Override
  public String filter(String message) {
    return new StringBuilder(message).reverse()
        .toString();
  }

  @Override
  protected void performInitialize() throws RuntimeException {
  }

  @Override
  protected void performFinish() {
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "[ Filter ]";
  }

}
