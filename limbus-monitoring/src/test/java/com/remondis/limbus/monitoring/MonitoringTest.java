package com.remondis.limbus.monitoring;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.junit.Test;
import com.remondis.limbus.monitoring.publisher.AbstractMessagePublisher;
import com.remondis.limbus.monitoring.publisher.DecoratedConsoleMessagePublisherImpl;
import com.remondis.limbus.monitoring.publisher.MessagePublisher;
import com.remondis.limbus.monitoring.publisher.SomeOtherPublisher;
import com.remondis.limbus.monitoring.publisher.SomeOtherPublisherImpl;
import com.testclient.packageB.InheritClient;

public class MonitoringTest {

  @Test
  public void test_call_immediately() {
    String oldThreadName = Thread.currentThread()
        .getName();

    try {
      String thisThreadName = UUID.randomUUID()
          .toString();
      Thread.currentThread()
          .setName(thisThreadName);

      com.testclient.packageB.Client bClient = new com.testclient.packageB.Client();

      bClient.someOtherMethod();
      bClient.callImmediately();

      Set<Object> bOtherPublishers = MonitoringFactory.getPublishers(bClient.getClass(), SomeOtherPublisher.class);
      assertEquals(1, bOtherPublishers.size());
      SomeOtherPublisherImpl bOtherPublisher = (SomeOtherPublisherImpl) bOtherPublishers.iterator()
          .next();

      MonitoringFactory.shutdown(true);

      assertEquals(thisThreadName, bOtherPublisher.getThreadNameCallImmediately());
      assertNotEquals(thisThreadName, bOtherPublisher.getThreadNameSomeOther());
    } finally {
      Thread.currentThread()
          .setName(oldThreadName);
    }
  }

  @Test // Happy path
  public void test_publisher() {
    // schuettec - 13.04.2017 : For this client the DecoratedConsoleMessagePublisherImpl is expected
    com.testclient.packageA.Client aClient = new com.testclient.packageA.Client();

    Set<Object> aMsgPublishers = MonitoringFactory.getPublishers(aClient.getClass(), MessagePublisher.class);
    Set<Object> aOtherPublishers = MonitoringFactory.getPublishers(aClient.getClass(), SomeOtherPublisher.class);
    assertEquals(1, aMsgPublishers.size());
    Object aPublisher = aMsgPublishers.iterator()
        .next();
    assertEquals(1, aMsgPublishers.size());
    assertEquals(DecoratedConsoleMessagePublisherImpl.class, aPublisher.getClass());
    assertEquals(0, aOtherPublishers.size());

    // schuettec - 13.04.2017 : For this two clients the ConsoleMessagePublisherImpl and SomeOtherPublisherImpl is
    // expected
    com.testclient.packageB.Client bClient = new com.testclient.packageB.Client();
    InheritClient iClient = new InheritClient();

    Set<Object> bMsgPublishers = MonitoringFactory.getPublishers(bClient.getClass(), MessagePublisher.class);
    Set<Object> iMsgPublishers = MonitoringFactory.getPublishers(iClient.getClass(), MessagePublisher.class);
    Set<Object> bOtherPublishers = MonitoringFactory.getPublishers(bClient.getClass(), SomeOtherPublisher.class);
    Set<Object> iOtherPublishers = MonitoringFactory.getPublishers(iClient.getClass(), SomeOtherPublisher.class);
    assertEquals(1, bMsgPublishers.size());
    assertEquals(1, iMsgPublishers.size());
    assertEquals(1, bOtherPublishers.size());
    assertEquals(1, iOtherPublishers.size());

    AbstractMessagePublisher aMsgPublisher = (AbstractMessagePublisher) aMsgPublishers.iterator()
        .next();

    AbstractMessagePublisher bMsgPublisher = (AbstractMessagePublisher) bMsgPublishers.iterator()
        .next();
    AbstractMessagePublisher iMsgPublisher = (AbstractMessagePublisher) iMsgPublishers.iterator()
        .next();
    SomeOtherPublisherImpl bOtherPublisher = (SomeOtherPublisherImpl) bOtherPublishers.iterator()
        .next();
    SomeOtherPublisherImpl iOtherPublisher = (SomeOtherPublisherImpl) iOtherPublishers.iterator()
        .next();

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    // The publishers of b and i are the same for all types:
    assertSame(bMsgPublisher, iMsgPublisher);
    assertSame(bOtherPublisher, iOtherPublisher);
    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!

    assertMessageCalls(0, bMsgPublisher);
    assertMessageCalls(0, iMsgPublisher);
    assertMessageCalls(0, aMsgPublisher);
    assertFormatMessageCalls(0, bMsgPublisher);
    assertFormatMessageCalls(0, iMsgPublisher);
    assertFormatMessageCalls(0, aMsgPublisher);

    String bMessage = bClient.sendMessage();
    String iMessage = iClient.sendMessage();
    // Client A publishes to DecoratedConsoleMessagePublisherImpl, so the expected string is decorated.
    String aMessage = "Decorated:" + aClient.sendMessage();

    String bFormattedMessage = bClient.sendFormattedMessage();
    String iFormattedMessage = iClient.sendFormattedMessage();
    String aFormattedMessage = aClient.sendFormattedMessage();

    MonitoringFactory.shutdown(true);

    assertEquals(0, PublisherTask.TASKS.size());

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    /*
     * The publishers of b and i are the same for all types.
     * Therefore 2 calls are expected.
     */
    assertMessageCalls(2, bMessage, bMsgPublisher);
    assertMessageCalls(2, iMessage, iMsgPublisher);

    // !!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!
    assertMessageCalls(1, aMessage, aMsgPublisher);

    assertFormatMessageCalls(2, bFormattedMessage, bMsgPublisher);
    assertFormatMessageCalls(2, iFormattedMessage, iMsgPublisher);
    // Client a publishes to DecoratedConsoleMessagePublisherImpl, so the expected string is decorated.
    assertFormatMessageCalls(1, "Decorated:" + aFormattedMessage, aMsgPublisher);
  }

  private void assertMessageCalls(int i, AbstractMessagePublisher publisher) {
    List<String> calls = publisher.getMessageCalls();
    assertEquals(i, calls.size());
  }

  private void assertFormatMessageCalls(int i, AbstractMessagePublisher publisher) {
    List<String> calls = publisher.getFormatMessageCalls();
    assertEquals(i, calls.size());
  }

  private void assertMessageCalls(int i, String message, AbstractMessagePublisher publisher) {
    List<String> calls = publisher.getMessageCalls();
    assertEquals(i, calls.size());
    assertTrue(calls.contains(message));
  }

  private void assertFormatMessageCalls(int i, String message, AbstractMessagePublisher publisher) {
    List<String> calls = publisher.getFormatMessageCalls();
    assertEquals(i, calls.size());
    assertTrue(calls.contains(message));
  }

}
