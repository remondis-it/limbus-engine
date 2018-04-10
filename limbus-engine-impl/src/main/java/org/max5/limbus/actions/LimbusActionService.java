package org.max5.limbus.actions;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.max5.limbus.IInitializable;
import org.max5.limbus.Initializable;
import org.max5.limbus.LimbusEngine;
import org.max5.limbus.system.LimbusContainer;
import org.max5.limbus.system.LimbusSystem;
import org.max5.limbus.utils.ReflectionUtil;

/**
 * This is an implementation of the {@link ActionService}, that provides access to system components. The result of a
 * reflective method call will be wrapped in an {@link ActionResult}.
 *
 * @param <T>
 *        The type of the {@link LimbusContainer} extension, this {@link ActionService} can operate on.
 *
 * @author schuettec
 *
 */
public class LimbusActionService<T extends LimbusEngine> extends Initializable<Exception> implements ActionService {

  protected static final ActionResult<Object> DENIED_RESULT = new ActionResult<Object>(ActionStatus.DENIED, null);

  @LimbusContainer
  protected LimbusSystem container;

  @Override
  public ActionResult<?> executeAction(ActionExecution action) throws ActionException {
    checkState();

    @SuppressWarnings("unchecked")
    Class<? extends IInitializable<?>> actionInterfaceClass = (Class<? extends IInitializable<?>>) loadActionClass(
        action.getClassname());
    if (container.hasComponent(actionInterfaceClass)) {
      IInitializable<?> component = container.getComponent(actionInterfaceClass);
      ActionResult<?> result = executeMethod(component, action.getMethodname(), action.getMethodSignatureTypes(),
          action.getParameters());
      return result;
    } else {
      return DENIED_RESULT;
    }

  }

  /**
   * Gets a method per reflection and invokes a method call with given parameters.
   *
   * @param actionImplClassname
   *        the name of the action class
   * @param actionObject
   *        the {@link Action} object
   * @param methodname
   *        the name of the method to execute
   * @param methodSignatureTypes
   *        the expected types of the methods signature
   * @param parameters
   *        the parameters to call the method with
   * @return the result of the method call
   * @throws ActionException
   *         Thrown, if calling the method failed or an error disappears during execution.
   */
  private ActionResult<?> executeMethod(IInitializable<?> actionObject, String methodname,
      String[] methodSignatureTypes, Object... parameters) throws ActionException {
    String actionImplClassname = actionObject.getClass()
        .getName();
    Method method;
    Class<?>[] parameterClasses = getSignatureClasses(methodSignatureTypes);
    Object returnValue;
    try {
      method = actionObject.getClass()
          .getMethod(methodname, parameterClasses);
      returnValue = method.invoke(actionObject, parameters);
    } catch (NoSuchMethodException e) {
      throw new ActionException(String.format("Class \'%s\' has no method \'%s\'. Failed to execute action.",
          actionImplClassname, methodname), e);
    } catch (SecurityException e) {
      throw new ActionException(
          String.format("Security violation calling method \'%s\' of class \'%s\'. Failed to execute action.",
              methodname, actionImplClassname),
          e);
    } catch (IllegalAccessException e) {
      throw new ActionException(String.format("Illegal access. Failed to execute method \'%s\' from action \'%s\'.",
          methodname, actionImplClassname), e);
    } catch (IllegalArgumentException e) {
      throw new ActionException(
          String.format("Class \'%s\' has no method \'%s\' with given parameters. Failed to execute action.",
              actionImplClassname, methodname),
          e);
    } catch (InvocationTargetException e) {
      Throwable toThrow = e;
      if (e.getCause() != null) {
        toThrow = e.getCause();
      }
      throw new ActionException(
          String.format("Method \'%s\' from class \'%s\' threw an exception.", methodname, actionImplClassname),
          toThrow);
    }
    return new ActionResult<Object>(ActionStatus.FINISHED, returnValue);
  }

  private Class<?>[] getSignatureClasses(String[] methodSignatureTypes) throws ActionException {
    Class<?>[] classes = new Class<?>[methodSignatureTypes.length];
    for (int i = 0; i < methodSignatureTypes.length; i++) {
      try {
        classes[i] = Class.forName(methodSignatureTypes[i]);
      } catch (ClassNotFoundException e) {
        throw new ActionException(String.format("Class %s not found.", methodSignatureTypes[i]), e);
      }
    }
    return classes;
  }

  /**
   * Loads a class of type {@link Action} from a given classname.
   *
   * @param classname
   *        the classname
   * @return the action class
   * @throws ActionException
   *         Thrown, if class could not be found.
   */
  protected Class<?> loadActionClass(String classname) throws ActionException {
    try {
      Class<?> actionClass = ReflectionUtil.loadServiceClass(classname, IInitializable.class,
          LimbusActionService.class.getClassLoader());
      return actionClass;
    } catch (Exception e) {
      throw new ActionException(String.format("The action class \'%s\' was not found.", classname), e);
    }
  }

  @Override
  protected void performInitialize() throws Exception {
  }

  @Override
  protected void performFinish() {
    // Forget the container
    this.container = null;
  }

}
