package com.remondis.limbus.monitoring.publisher;

import com.remondis.limbus.monitoring.ClientContext;
import com.remondis.limbus.monitoring.PublisherUtils;

public class ClientContextTestPublisherImpl implements ClientContextTestPublisher {

  private ClientContext clientContext;

  @Override
  public void saveClientContext() {
    ClientContext clientContext = PublisherUtils.getClientContext();
    this.clientContext = clientContext;
  }

  public ClientContext getAndClearClientContext() {
    ClientContext local = this.clientContext;
    this.clientContext = null;
    return local;
  }

}
