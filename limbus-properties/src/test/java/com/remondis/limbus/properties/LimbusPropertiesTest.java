package com.remondis.limbus.properties;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.Properties;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.remondis.limbus.files.InMemoryFilesystemImpl;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.system.MockLimbusSystem;
import com.remondis.limbus.utils.Lang;

public class LimbusPropertiesTest {

  MockLimbusSystem system;

  InMemoryFilesystemImpl filesystem;

  @Before
  public void before() throws Exception {
    Lang.initializeJDKLogging();

    system = new MockLimbusSystem();

    filesystem = new InMemoryFilesystemImpl();

    system.addPublicComponentMock(LimbusFileService.class, filesystem);
    system.initialize();
  }

  @After
  public void after() {
    system.finish();
  }

  @Test
  public void test_effectiveDifferences() throws Exception {
    {
      Properties defaults = new Properties();
      defaults.put("key1", "value1");
      defaults.put("key2", "value2");
      defaults.put("key3", "value3");

      Properties effective = new Properties();
      effective.putAll(defaults);
      // Only the values changed by effective properties should be in the properties object.
      Properties differences = LimbusProperties.getEffectiveDifferences(defaults, effective);
      assertTrue(differences.isEmpty());

    }
    {
      Properties defaults = new Properties();
      defaults.put("key1", "value1");
      defaults.put("key2", "value2");
      defaults.put("key3", "value3");

      Properties effective = new Properties();
      effective.putAll(defaults);
      effective.put("key2", "changed2");
      effective.put("key3", "changed3");

      // Only the values changed by effective properties should be in the properties object.
      Properties differences = LimbusProperties.getEffectiveDifferences(defaults, effective);
      assertFalse(differences.containsKey("key1"));
      assertTrue(differences.containsKey("key2"));
      assertTrue(differences.containsKey("key3"));
      assertEquals("changed2", differences.get("key2"));
      assertEquals("changed3", differences.get("key3"));

    }
    {
      Properties defaults = new Properties();
      defaults.put("key1", "value1");
      defaults.put("key2", "value2");
      defaults.put("key3", "value3");

      Properties effective = new Properties();
      effective.putAll(defaults);
      effective.put("key4", "changed4");
      effective.put("key5", "changed5");

      // Only the values changed by effective properties should be in the properties object.
      Properties differences = LimbusProperties.getEffectiveDifferences(defaults, effective);
      assertFalse(differences.containsKey("key1"));
      assertFalse(differences.containsKey("key2"));
      assertFalse(differences.containsKey("key3"));
      assertTrue(differences.containsKey("key4"));
      assertTrue(differences.containsKey("key5"));
      assertEquals("changed4", differences.get("key4"));
      assertEquals("changed5", differences.get("key5"));
    }
  }

  @Test
  public void test_no_override() throws Exception {
    // Add config override for LimbusPropertiesTest because it is expected in this test
    filesystem.addClasspathContent(getClass(), LimbusFileService.CONFIG_DIRECTORY,
        "/com.remondis.limbus.properties.LimbusPropertiesTest_default.properties");
    filesystem.addContent(filesystem.toPath(LimbusFileService.CONFIG_DIRECTORY,
        "com.remondis.limbus.properties.LimbusPropertiesTest.properties"), new byte[] {});

    String testKey = "testKey";
    String addedKey = "addedKey";

    LimbusProperties conf = new LimbusProperties(filesystem, LimbusPropertiesTest.class, true, true);
    assertTrue(conf.containsKey(testKey));
    assertFalse(conf.containsKey(addedKey));

    assertEquals("original", conf.getProperty(testKey));
  }

  @Test
  public void test_override() throws Exception {
    // Add config override for LimbusPropertiesTest because it is expected in this test
    filesystem.addClasspathContent(getClass(), LimbusFileService.CONFIG_DIRECTORY,
        "/com.remondis.limbus.properties.LimbusPropertiesTest_default.properties");
    filesystem.addClasspathContent(getClass(), LimbusFileService.CONFIG_DIRECTORY,
        "/com.remondis.limbus.properties.LimbusPropertiesTest.properties");

    String testKey = "testKey";
    String addedKey = "addedKey";

    LimbusProperties conf = new LimbusProperties(filesystem, LimbusPropertiesTest.class, true, true);
    assertTrue(conf.containsKey(testKey));
    assertTrue(conf.containsKey(addedKey));

    assertEquals("overridden", conf.getProperty(testKey));
    assertEquals("addedValue", conf.getProperty(addedKey));
  }

  @Test(expected = Exception.class)
  public void test_fail_on_no_default() throws Exception {
    new LimbusProperties(filesystem, String.class, true, false);
  }

  @Test
  public void test_no_fail() throws Exception {
    LimbusProperties conf = new LimbusProperties(filesystem, LimbusPropertiesTest.class, false, false);
    Properties properties = conf.getProperties();
    assertTrue(properties.isEmpty());
  }

  @Test(expected = Exception.class)
  public void test_fail_on_no_file() throws Exception {
    new LimbusProperties(filesystem, LimbusPropertiesTest.class, false, true);
  }

}
