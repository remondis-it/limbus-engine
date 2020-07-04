package com.remondis.limbus.utils;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;

/**
 * Simple JUnit case after removing SoftReferences from the Cache
 * 
 * 
 *
 */
public class CacheTest {

  @Test
  public void testWithoutStrategy() {
    try {
      Cache<String, Long> cache = new Cache<String, Long>();

      cache.add("hallo", 0l);
      assertTrue(cache.containsKey("hallo"));

      cache.remove("hallo");
      assertFalse(cache.containsKey("hallo"));

      cache.add("h", 0l);
      cache.add("a", 0l);
      cache.add("l", 0l);
      cache.add("l", 0l);
      cache.add("o", 0l);
      assertArrayEquals(new Object[] {
          4
      }, new Object[] {
          cache.size()
      }, "Cache contains elements");

      assertTrue(cache.containsKey("o"));
      cache.getAndRemove("o");
      assertFalse(cache.containsKey("o"));

      cache.dispose();
      assertTrue(cache.isEmpty());

    } catch (Exception e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
  }

}
