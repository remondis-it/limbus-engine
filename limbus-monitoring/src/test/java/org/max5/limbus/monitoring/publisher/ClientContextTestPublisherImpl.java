package org.max5.limbus.monitoring.publisher;

import org.max5.limbus.monitoring.ClientContext;
import org.max5.limbus.monitoring.PublisherUtils;

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
