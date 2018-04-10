package org.max5.limbus.monitoring;

import static org.max5.limbus.monitoring.Conventions.isCallImmediatelyMethod;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.util.Set;

import org.max5.limbus.utils.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This {@link InvocationHandler} is a no-Op invocation handler that always
 * returns null. This is used to create no-op {@link Publisher} instances.
 * Due to the fact, that {@link Publisher}s must have only methods with no
 * return types, this no-op implementation is suitable.
 *
 * @author schuettec
 *
 */
class RecordCallHandler implements InvocationHandler {

  private static final Logger log = LoggerFactory.getLogger(RecordCallHandler.class);

  protected String clientName;
  private Class<?> publisherType;

  public RecordCallHandler(String clientName, Class<?> publisherType) {
    this.publisherType = publisherType;
    this.clientName = clientName;
  }

  @Override
  public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
    try {
      MethodCall call = new MethodCall(clientName, method, args);
      if (isCallImmediatelyMethod(method)) {
        Set<Object> publishers = MonitoringFactory.getPublishers(clientName, publisherType);
        for (Object publisher : publishers) {
          try {
            call.replay(publisher);
          } catch (Throwable t) {
            PublisherUtils.logPublisherCallFailed(publisher, t);
          }
        }
      } else {
        if (ReflectionUtil.hasReturnType(method)) {
          log.warn(
              "Publisher method with return type but without CallImmediately annotation called - this is an implementation fault.");
        } else {
          MonitoringFactory.enqueueRecord(call);
        }
      }

      // schuettec - 08.05.2017 : Catch all possible throwables to avoid influencing client code. The monitoring
      // framework may not break the client's business logic.
    } catch (Throwable t) {
      log.warn("Unexpected exception while publishing monitoring record.", t);
    }
    return null;
  }

}
