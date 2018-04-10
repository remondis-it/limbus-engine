package org.max5.limbus.logging;

import java.util.Map;

/**
 * The {@link TargetSelector} is used to select a write target from the {@link RoutedOutputStream}.
 *
 * @param <ID>
 *        The datatype of the id.
 * @param <T>
 *        The type of the targets.
 * @author schuettec
 *
 */
public interface TargetSelector<ID, T> {

  /**
   * Selects a target from the specified map.
   *
   * @param targets
   *        The map of registered targets.
   * @return Returns the target or <code>null</code> if not found.
   */
  public T selectTarget(Map<ID, T> targets);

}
