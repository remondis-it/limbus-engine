package com.remondis.limbus.utils;

/**
 * This interface defines a dispose strategy that manages the life cycle of an item in the cache. There is a set of
 * events that trigger a dispose strategy to either dispose an item in the cache or calculate statistics for further
 * disposal. <br>
 * <br>
 * Note: An instance of the dispose strategy is created for every item in the cache. Therefore it is allowed to manage a
 * state within the strategy implementation, although it is recommended to keep the state simple. <br>
 * <br>
 *
 * @param <K>
 *        The type of the key used for the mapping
 * @param <V>
 *        The type of cached values.
 * 
 *
 */
public interface DisposeStrategy<K, V> {

  /**
   * Called if the item, this strategy applies to, was added to the cache.
   *
   * @param cache
   *        The instance of the cache this strategy is applied on
   * @param cacheToken
   *        The cache token
   * @param cacheValue
   *        The cache value
   */
  public void eventItemAdded(Cache<K, V> cache, K cacheToken, V cacheValue);

  /**
   * Called when the item is requested from the cache. This method can decide if the request should be fulfilled by
   * returning the item or null should be returned to signal that the item is not available anymore.
   *
   * @param cache
   *        The instance of the cache this strategy is applied on
   * @param cacheToken
   *        The cache token
   * @param cacheValue
   *        The cache value
   *
   * @return True if the value should be returned to the caller, false if null should be returned (to signal that the
   *         item is not available)-
   */
  public boolean eventItemRequested(Cache<K, V> cache, K cacheToken, V cacheValue);

  /**
   * Called when the cached item is about to be removed from the cache.
   *
   * @param cache
   *        The instance of the cache this strategy is applied on
   * @param cacheToken
   *        The cache token
   * @param cacheValue
   *        The cache value
   */
  public void eventRemoved(Cache<K, V> cache, K cacheToken, V cacheValue);

  /**
   * Initializes this {@link DisposeStrategy}.
   */
  public void initialize();

  /**
   * Deinitialize this {@link DisposeStrategy}.
   */
  public void finish();

}
