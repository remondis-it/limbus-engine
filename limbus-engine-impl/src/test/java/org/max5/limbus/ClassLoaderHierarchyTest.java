package org.max5.limbus;

import static org.junit.Assert.*;

import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URL;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.max5.limbus.files.InMemoryFilesystemImpl;
import org.max5.limbus.files.LimbusFileService;
import org.max5.limbus.utils.Lang;
import org.max5.limbus_notAllowed.NotSeenBySharedLoader;

/**
 * This class tests the isolating class loader hierarchy.
 *
 * @author schuettec
 *
 */
public class ClassLoaderHierarchyTest {

  private static final String A_JAR = "/libs/A.jar";

  private static final String B_JAR = "/libs/B.jar";

  private static final String RESOURCE_SEEN_BY_SHARED_LOADER = "org/dummy/test/b/B.file";

  /**
   * This resource may not be seen by shared loader because it does not match any of the allowed package prefixes.
   */
  private static final String RESOURCE_NOT_SEEN_BY_SHARED_LOADER = "org/max5/limbus_notAllowed/NotSeenBySharedLoader";

  private static final String A_CLASS = "org.dummy.test.a.A";

  private static URL B_URL;
  private static URL A_URL;

  private SharedClassLoader sharedLoader;

  private PluginClassLoader pluginLoader;

  private LimbusFileService filesystem;

  @BeforeClass
  public static void beforeClass() {
    B_URL = ClassLoaderHierarchyTest.class.getResource(B_JAR);
    A_URL = ClassLoaderHierarchyTest.class.getResource(A_JAR);
    assertNotNull(B_URL);
    assertNotNull(A_URL);

  }

  @Before
  public void before() {
    this.filesystem = new InMemoryFilesystemImpl();
    // schuettec - 02.11.2016 : Set the default allowed package prefixes to get the same isolation of Limbus Engine
    // classes and plugin classes like in a productive environment.
    this.sharedLoader = new SharedClassLoader(filesystem, ClassLoaderHierarchyTest.class.getClassLoader(),
        LimbusUtil.getDefaultAllowedPackagePrefixes(), B_URL);
    this.pluginLoader = new PluginClassLoader(filesystem, sharedLoader, A_URL);
    pluginLoader.deactivateCleaning();

  }

  @Test
  public void ensure_resources_fix_of_allowedPackagePrefix_ISSUE_117() throws Exception {
    // schuettec - 09.05.2017 : This resource may not be seen by shared loader because it does not match any of the
    // allowed package prefixes.
    InputStream resourceAsStream = sharedLoader.getResourceAsStream(RESOURCE_NOT_SEEN_BY_SHARED_LOADER);
    assertNull(resourceAsStream);

    resourceAsStream = pluginLoader.getResourceAsStream(RESOURCE_NOT_SEEN_BY_SHARED_LOADER);
    assertNull(resourceAsStream);
  }

  @Test
  public void ensure_classes_fix_of_allowedPackagePrefix_ISSUE_117() throws Exception {
    try {
      sharedLoader.loadClass(NotSeenBySharedLoader.class.getName());
      fail("Class not found exception was expcted for " + NotSeenBySharedLoader.class.getName());
    } catch (ClassNotFoundException e) {
      // Totally expected
    }
    try {
      pluginLoader.loadClass(NotSeenBySharedLoader.class.getName());
      fail("Class not found exception was expcted for " + NotSeenBySharedLoader.class.getName());
    } catch (ClassNotFoundException e) {
      // Totally expected
    }

  }

  @Test // Happy path
  public void test_resourceAccess_happy() throws Exception {
    // Request a allowed resource.
    {
      InputStream resourceAsStream = sharedLoader.getResourceAsStream(RESOURCE_SEEN_BY_SHARED_LOADER);
      assertNotNull(resourceAsStream);

      resourceAsStream = pluginLoader.getResourceAsStream(RESOURCE_SEEN_BY_SHARED_LOADER);
      assertNotNull(resourceAsStream);
    }

  }

  /**
   * Checks if the plugin uses log4j loaded by the shared loader and not contained in the plugins classpath.
   *
   * @throws Exception
   */
  @Test // Happy path
  public void test_class_hierarchy_1() throws Exception {
    // Create new instance
    Class<?> testClass = Class.forName(A_CLASS, true, pluginLoader);

    // Get the class loader log4j was loaded from - Expected to be the shared loader.
    ClassLoader actual = getBReferenceClassLoader(testClass);
    assertSame(sharedLoader, actual);
  }

  /**
   * Checks if the plugin uses log4j loaded by the PLUGIN loader where it is delivered this time.
   *
   * @throws Exception
   */
  @Test // Happy path
  public void test_class_hierarchy_2() throws Exception {
    // Load log4j as plugin library
    SharedClassLoader sharedLoader = new SharedClassLoader(filesystem, ClassLoaderHierarchyTest.class.getClassLoader(),
        LimbusUtil.getDefaultAllowedPackagePrefixes());
    PluginClassLoader pluginLoader = new PluginClassLoader(filesystem, sharedLoader, A_URL, B_URL);
    pluginLoader.deactivateCleaning();

    Class<?> testClass = Class.forName(A_CLASS, true, pluginLoader);

    // Get the class loader log4j was loaded from - Expected to be the plugin loader.
    ClassLoader actual = getBReferenceClassLoader(testClass);
    assertSame(pluginLoader, actual);
  }

  /**
   * Checks if the plugin uses log4j loaded by the PLUGIN loader although delivered by plugin and shared loader. Checks
   * the child first strategy.
   *
   * @throws Exception
   */
  @Test // Happy path
  public void test_class_hierarchy_3() throws Exception {
    // Load log4j as shared AND plugin library
    SharedClassLoader sharedLoader = new SharedClassLoader(filesystem, ClassLoaderHierarchyTest.class.getClassLoader(),
        LimbusUtil.getDefaultAllowedPackagePrefixes(), B_URL);
    PluginClassLoader pluginLoader = new PluginClassLoader(filesystem, sharedLoader, A_URL, B_URL);
    pluginLoader.deactivateCleaning();

    try {

      Class<?> testClass = Class.forName(A_CLASS, true, pluginLoader);

      {
        // Get the class loader log4j was loaded from - Expected to be the plugin loader.
        ClassLoader actual = getBReferenceClassLoader(testClass);
        assertSame(pluginLoader, actual);
      }

    } finally {
      Lang.closeQuietly(sharedLoader, pluginLoader);
    }
  }

  private ClassLoader getBReferenceClassLoader(Class<?> testClass) throws Exception {
    try {
      Object b = getBObject(testClass);
      return b.getClass()
          .getClassLoader();
    } catch (Exception e) {
      throw e;
    }
  }

  private Object getBObject(Class<?> aClass)
      throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
    Method getLog = aClass.getMethod("getB");
    Object log = getLog.invoke(null);
    return log;
  }

}
