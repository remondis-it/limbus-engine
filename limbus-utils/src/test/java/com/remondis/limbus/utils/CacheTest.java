package com.remondis.limbus.utils;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

/**
 * Simple JUnit case after removing SoftReferences from the Cache
 * 
 * @author dudzik
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
      assertArrayEquals("Cache contains elements", new Object[] {
          4
      }, new Object[] {
          cache.size()
      });

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
