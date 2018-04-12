package com.remondis.limbus.monitoring;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.remondis.limbus.utils.Lang;
import com.remondis.limbus.utils.ReflectionUtil;

/**
 * Objects of this class hold all neccessary data to record a method call.
 *
 * @author schuettec
 *
 */
public class MethodCall {

  protected String clientName;
  protected Method method;
  protected Object[] args;

  protected ClientContext clientContext;

  /**
   * Records a call of a method without parameters. <b>Note: Calling this constructor means, that the called method has
   * no parameters.</b>
   *
   * <p>
   * To record a method call with parameters use {@link #MethodCall(String, Method, Object[])}.
   * </p>
   *
   * @param clientName
   *        The name of the client performing this call.
   * @param method
   *        The method to record a call for. This method may not have a parameter.
   */
  public MethodCall(String clientName, Method method) {
    this(clientName, method, null);
    // schuettec - 19.04.2017 : Do not move this call without determining the correct stack frame offset. Refactoring
    // the following call in another method will change the stack trace!
    this.clientContext = new ClientContext(Thread.currentThread(), 3);
  }

  /**
   * Records a call of a method with or without parameters.
   *
   * @param clientName
   *        The name of the client performing this call.
   *
   * @param method
   *        The method to record a call for.
   * @param args
   *        The arguments:
   *        <ul>
   *        <li><code>null</code> means no parameters</li>
   *        <li><tt>[null, ...]</tt> means passing a null argument to the method.</li>
   *        <li><tt>[elem1,elem2,...]</tt> means passing arguments to the method</li>
   *        </ul>
   */
  public MethodCall(String clientName, Method method, Object[] args) {
    super();
    // schuettec - 19.04.2017 : Do not move this call without determining the correct stack frame offset. Refactoring
    // the following call in another method will change the stack trace!
    this.clientContext = new ClientContext(Thread.currentThread(), 3);
    Lang.denyNull("clientName", clientName);
    Lang.denyNull("method", method);
    this.method = method;
    this.clientName = clientName;
    denyNonNullArgsIfMethodHasNoParams(method, args);
    denyNullArgsIfMethodHasParams(method, args);
    denyArgsLengthNotEqual(method, args);
    this.args = args;
  }

  private void denyArgsLengthNotEqual(Method method, Object[] args) {
    if (hasParameters()) {
      int paramsLength = method.getParameters().length;
      int argsLength = args.length;
      if (argsLength != paramsLength) {
        throw new IllegalArgumentException(
            String.format("Method has %d parameters but %d arguments were specified.", paramsLength, argsLength));
      }
    }
  }

  private void denyNonNullArgsIfMethodHasNoParams(Method method, Object[] args) {
    if (args != null && !hasParameters()) {
      throw new IllegalArgumentException("The specified method has no parameters but arguments were specified.");
    }
  }

  private void denyNullArgsIfMethodHasParams(Method method, Object[] args) {
    if (args == null && hasParameters()) {
      throw new IllegalArgumentException(
          "The specified method has parameters but null-arguments were specified. To record a call like method(null) specify an array with a null element.");
    }
  }

  /**
   * @return Returns <code>true</code> if the recorded method has parameters.
   */
  public boolean hasParameters() {
    return method.getParameters().length > 0;
  }

  /**
   * @return Returns the declaring class of the recorded method.
   */
  public Class<?> getDeclaringClass() {
    return method.getDeclaringClass();
  }

  public Object[] getArguments() {
    return this.args;
  }

  /**
   * Performs the recorded method call on the specified object.
   *
   * @param target
   *        The target object to perform the recorded method call on.
   * @return Returns the method's return value
   * @throws Throwable
   *         Thrown on any exception.
   */
  public Object replay(Object target) throws Throwable {
    Class<?> declaringType = getDeclaringClass();
    if (declaringType.isInstance(target)) {
      try {
        // schuettec - 19.04.2017 : Set the current clientContext via thread local.
        PublisherUtils.setClientContext(clientContext);
        return ReflectionUtil.invokeMethodProxySafe(method, target, args);
      } catch (InvocationTargetException e) {
        if (e.getCause() != null) {
          throw e.getCause();
        } else {
          throw e;
        }
      } finally {
        // schuettec - 19.04.2017 : IN ALL CASES remove the current clientContext from the thread locals.
        PublisherUtils.removeClientContext();
      }
    } else {
      throw new IllegalArgumentException(
          "The specified target object is not an instance of the method's declaring class.");
    }
  }

  /**
   * @return the clientName
   */
  public String getClientName() {
    return clientName;
  }

}
