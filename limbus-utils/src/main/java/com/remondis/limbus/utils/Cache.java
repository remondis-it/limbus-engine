package com.remondis.limbus.utils;

import java.util.Arrays;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * <p>
 * The RebindCache is a cache for any datastructure. For every item there
 * will be a key that identifies the datastructure at runtime. The item can be accessed multiple times using the
 * key. A resource stored in the cache can be disposed if it is not needed any longer. <br>
 * </p>
 * <p>
 * There are multiple strategies to dispose resources automatically. A timeout strategy for example is used to free
 * resources after a specific time. Multiple strategies can be added to the cache. <br>
 * </p>
 * <h2>Memory-sensitive Cache</h2>
 * <p>
 * This cache holds its items with {@link SoftReference}. The characteristics of this reference type is, that objects
 * are hold in memory as long as memory is in plentiful supply. If there is too little heap space available in the JVM
 * the garbage collector may decide to drop the referenced objects to free memory.
 * </p>
 * <p>
 * <b>As a consequence it is not guaranteed that resources that were added to the
 * cache are available at any time. On the other side this cache will not cause {@link OutOfMemoryError} because of
 * holding to heavy objects.</b>
 * </p>
 * <p>
 * <b>Note: This implementation is thread safe and optimized for high frequented concurrent access.</b>
 * </p>
 *
 * @param <K>
 *        The type of the key used for the mapping
 * @param <V>
 *        The type of cached values.
 * 
 */
public class Cache<K, V> {

  protected Map<K, V> cache;

  /**
   * This strategy applies to every item in the cache.
   */
  protected List<DisposeStrategy<K, V>> disposeStrategies;

  /**
   * This constuctor is used to create a cache.
   *
   * @throws Exception
   */
  @SuppressWarnings("unchecked")
  public Cache() throws Exception {
    this(new DisposeStrategy[] {});
  }

  /**
   * This constuctor is used to create a RebindCache.
   *
   * @param disposeStrategy
   *        <K,V>
   *        The dispose strategy that applies to every item in the cache.
   * @throws Exception
   *         on any error;
   */
  @SuppressWarnings("unchecked")
  public Cache(DisposeStrategy<K, V> disposeStrategy) throws Exception {
    this(new DisposeStrategy[] {
        disposeStrategy
    });
  }

  /**
   * This constuctor is used to create a RebindCache.
   *
   * @param disposeStrategies
   *        <K,V>
   *        The dispose strategy that applies to every item in the cache.
   * @throws Exception
   *         on any error;
   */
  public Cache(DisposeStrategy<K, V>[] disposeStrategies) throws Exception {
    this.cache = new ConcurrentHashMap<K, V>();
    this.disposeStrategies = Arrays.asList(disposeStrategies);
    _fireEvent(null, CacheEvent.INIT);
  }

  /**
   * This constuctor is used to create a RebindCache.
   *
   * @param disposeStrategies
   *        <K,V>
   *        The dispose strategy that applies to every item in the cache.
   * @throws Exception
   *         Thrown on any error
   */
  public Cache(List<DisposeStrategy<K, V>> disposeStrategies) throws Exception {
    this.cache = new ConcurrentHashMap<K, V>();
    this.disposeStrategies = disposeStrategies;
    _fireEvent(null, CacheEvent.INIT);
  }

  /**
   * @param d
   *        Adds the specified dispose strategy to this cache.
   */
  public void addDisposeStrategy(DisposeStrategy<K, V> d) {
    disposeStrategies.add(d);
  }

  /**
   * @param d
   *        Removes the specified dispose strategy from this cache
   */
  public void remove(DisposeStrategy<K, V> d) {
    disposeStrategies.remove(d);
  }

  /**
   * Fire a specific event on a specific cache token.
   *
   * @param token
   *        The cache token this event belongs to.
   * @param event
   *        The event type.
   */
  protected void _fireEvent(K token, CacheEvent event) {
    // Check if the mapping exists
    for (DisposeStrategy<K, V> s : disposeStrategies) {
      switch (event) {
        case INIT:
          s.initialize();
          break;
        case GET:
          s.eventItemRequested(this, token, cache.get(token));
          break;
        case ADD:
          s.eventItemAdded(this, token, cache.get(token));
          break;
        case REMOVE:
          s.eventRemoved(this, token, cache.get(token));
          break;
        case DISPOSE:
          s.finish();
          break;
      }
    }
  }

  /**
   * Returns the set of all cache tokens.
   *
   * @return The key set containing all cache tokens.
   */
  public Set<K> keySet() {
    return this.cache.keySet();
  }

  /**
   * Fires a get event to the dispose strategies. If any strategy signals to not return the requested value, false is
   * returned to signal that this resource has disposed.
   *
   * @param token
   *        The token that identifies the item in the cache.
   * @return Returns true if the request should be fulfilled by returning the value to the caller. Returns false if any
   *         strategy signals to dispose the requested resource.
   */
  protected boolean _fireGetEvent(K token) {
    boolean dispose = true;
    for (DisposeStrategy<K, V> s : disposeStrategies) {
      if (!s.eventItemRequested(this, token, cache.get(token))) {
        dispose = false;
      }
    }
    return dispose;
  }

  protected void _fireDisposeAll(CacheEvent event) {
    _fireEvent(null, CacheEvent.DISPOSE);
  }

  public void add(K cacheToken, V item) {
    // Put the item to the cach
    cache.put(cacheToken, item);
    // Fire add event
    _fireEvent(cacheToken, CacheEvent.ADD);
  }

  /**
   * Removes an element from the cache.
   *
   * @param cacheToken
   *        The token identifying the item in the cache.
   */
  public void remove(K cacheToken) {
    // Fire remove event
    _fireEvent(cacheToken, CacheEvent.REMOVE);

    // Remove the cached item
    if (cache.containsKey(cacheToken))
      cache.remove(cacheToken);

  }

  /**
   * @param cacheToken
   * @return Returns the element identified by cache token or <code>null</code> if it is not available in the cache. The
   *         element, if available, remains in the cache. The dispose strategies are
   *         triggered to perform any actions on the cache.
   */
  public V get(K cacheToken) {
    // The value object referenced by cacheToken
    V value = null;

    // If item is or was known by cache
    if (cache.containsKey(cacheToken)) {
      value = cache.get(cacheToken);
    }

    // Fire get event
    boolean available = _fireGetEvent(cacheToken);
    if (available) {
      return value;
    } else {
      // dudzik: remove from cache
      cache.remove(cacheToken);
      return null;
    }
  }

  /**
   * See {@link Cache#get(Object)} and see {@link Cache#remove(Object)}
   *
   * @param cacheToken
   *        The cache token to perform "get" and "remove" on.
   * @return Returns the element identified by cache token or <code>null</code> if it is not available in the cache. The
   *         element, if available, will be removed from the cache.
   */
  public V getAndRemove(K cacheToken) {
    V value = get(cacheToken);
    // Remove item and fire remove event.
    remove(cacheToken);
    return value;
  }

  /**
   * Clears the cache and removes all items.
   */
  public void clear() {
    Iterator<K> it = cache.keySet()
        .iterator();
    while (it.hasNext()) {
      K cacheToken = it.next();
      remove(cacheToken);
    }
    this.cache.clear();
  }

  public int size() {
    return cache.size();
  }

  public boolean containsValueForKey(K cacheToken) {
    if (cache.containsKey(cacheToken)) {
      return cache.get(cacheToken) != null;
    } else {
      return false;
    }
  }

  /**
   * Clears the cache and drops all of its items.
   */
  public void dispose() {
    // Fire dispose event
    _fireDisposeAll(CacheEvent.DISPOSE);

    // Clear all members
    this.cache.clear();
    // Clear on dispose strategies is unsupported due to Arrays.asList...
    this.disposeStrategies = null;
  }

  public boolean isEmpty() {
    return cache.isEmpty();
  }

  public boolean containsKey(Object key) {
    return cache.containsKey(key);
  }

  public boolean containsValue(Object value) {
    return cache.containsValue(value);
  }

}
