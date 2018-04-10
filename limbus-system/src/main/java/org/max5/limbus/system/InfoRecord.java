package org.max5.limbus.system;

import org.max5.limbus.utils.StringUtil;

/**
 * This class holds log information to provide information about the initialization order and result in
 * {@link LimbusSystem}.
 *
 * @author schuettec
 *
 */
public class InfoRecord {

  private static final int maxStatusCol = ComponentStatus.getMaxStringLength();
  private static final int maxRequiredCol = 8;
  private static int maxRequestCol = 60;

  private Component component;
  private ComponentStatus status;

  InfoRecord(Component component, ComponentStatus status) {
    super();
    this.component = component;
    this.status = status;
    adjustMaxRequestCol(component);
  }

  private static void adjustMaxRequestCol(Component component) {
    ComponentConfiguration configuration = component.getConfiguration();
    if (configuration.isPublicComponent()) {
      String requestTypeName = configuration.getRequestType()
          .getName();
      maxRequestCol = Math.max(maxRequestCol, requestTypeName.length());
    }
  }

  /**
   * @return the status
   */
  public ComponentStatus getStatus() {
    return status;
  }

  /**
   * @return the component
   */
  public Component getComponent() {
    return component;
  }

  @Override
  public int hashCode() {
    return component.hashCode();
  }

  @Override
  public boolean equals(Object obj) {
    return component.equals(obj);
  }

  public static String toRecordHeader() {
    String statusCol = "Status";
    String requiredCol = "Required";
    String requestCol = "Request type";
    String componentCol = "Component type";
    return formatRow(statusCol, requiredCol, requestCol, componentCol);
  }

  private static String formatRow(String statusCol, String requiredCol, String requestCol, String componentCol) {
    return formatRow(statusCol, maxStatusCol, requiredCol, maxRequiredCol, requestCol, maxRequestCol, componentCol);
  }

  private static String formatRow(String statusCol, int maxStatusCol, String requiredCol, int maxRequiredCol,
      String requestCol, int maxRequestCol, String componentCol) {
    return String.format("| %s | %s | %-" + maxRequestCol + "s | %s", StringUtil.center(statusCol, maxStatusCol),
        StringUtil.center(requiredCol, maxRequiredCol), requestCol, componentCol);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    ComponentConfiguration c = component.getConfiguration();
    String requestType = null;
    if (c.isPublicComponent()) {
      requestType = c.getRequestType()
          .getName();
    } else {
      requestType = "<<private>>";
    }
    return formatRow(status.name(), String.valueOf(c.isFailOnError()), requestType, c.getComponentType()
        .getName());
  }

}
