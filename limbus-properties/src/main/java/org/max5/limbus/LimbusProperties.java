package org.max5.limbus;

import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.net.URL;
import java.net.URLConnection;
import java.util.Iterator;
import java.util.Map.Entry;
import java.util.Properties;

import org.max5.limbus.files.LimbusFileService;
import org.max5.limbus.utils.Lang;

/**
 * This class is a facade for {@link Properties}. It manages properties by class and supports an default and override
 * feature.
 *
 * <p>
 * There are two supported stages for properties:
 * <ul>
 * <li><b>The default:</b> A property file loaded from classpath as default. Not modifyable.</li>
 * <ul>
 * <li><b>Derived:</b> Some or all keys in the properties are overridden by a specified property file.</li>
 * </p>
 *
 * <h2>Property conventions</h2>
 * <p>
 * <ul>
 * <li>
 * The default property file should be available in the root package. The filename must have
 * the following format: <b>fullyQualifiedClassName_default.properties</b>
 * </li>
 * <li>
 * The properties loaded at runtime are available in the subdirectory <tt>conf</tt> of the current directory.
 * </li>
 * <li>
 * Classpath resources are always loaded from the classloader the {@link LimbusProperties} are loaded from. Plugin
 * classloaders or shared classloaders are not supported and this class is not intended to be used by plugins..
 * </li>
 *
 * </p>
 *
 * @author schuettec
 *
 */
public class LimbusProperties {

  private static final String FILE_PROPERIES_FORMAT = "%s.properties";
  private static final String DEFAULT_CP_FORMAT = "/%s_default.properties";

  private LimbusFileService filesystem;

  private Properties properties = null;
  private String forClassName;

  /**
   * Shorthand for {@link #LimbusProperties(Class, true, false)}
   *
   * @param filesystem
   *        The filesystem to access when searching for configuration overrides.
   * @param forClass
   *        The class that requests the properties.
   * @throws Exception
   *         Thrown on any non-ignored error.
   */
  protected LimbusProperties(LimbusFileService filesystem, Class<?> forClass) throws Exception {
    this(filesystem, forClass, true, false);
  }

  /**
   * This construct a properties manager for the specified class.
   *
   * @param filesystem
   *        The filesystem to access when searching for configuration overrides.
   * @param forClass
   *        The class that requests the properties.
   * @param failOnNoDefault
   *        If <code>true</code> an exception is thrown if the default properties are not available in the classpath.
   *        If <code>false</code> missing defaults are ignored.
   * @param failOnNoFile
   *        If <code>true</code> an exception is thrown if the configuration file is not available. If
   *        <code>false</code> a missing configuration is ignored.
   * @throws Exception
   *         Thrown on any non-ignored error.
   */
  public LimbusProperties(LimbusFileService filesystem, Class<?> forClass, boolean failOnNoDefault,
      boolean failOnNoFile) throws Exception {
    // Try to access conf directory
    this.forClassName = forClass.getName();
    this.filesystem = filesystem;

    // Load default.
    Properties defaultProperties = getConfigurationDefault(failOnNoDefault, forClassName);

    // Load filesystem properties
    Properties fileProperties = new Properties();
    String filePath = getFilePath(forClassName);
    if (filesystem.hasFile(filePath)) {
      URL file = filesystem.getFile(filePath);
      fileProperties = loadProperties(file, failOnNoFile);
    } else {
      if (failOnNoFile) {
        throw new Exception("Cannot load properties from missing file.");
      }
    }

    // Merge properties by overriding all items from default with the file properties.
    defaultProperties.putAll(fileProperties);

    this.properties = defaultProperties;
  }

  /**
   * Saves the configuration keys that differ from the default configuration to the configuration file in the
   * filesystem. Note: If the effective properties do not differ from the default values, nothing is written to file.
   *
   * @throws Exception
   *         Thrown if the configuration file cannot be written.
   */
  public void storeConfiguration() throws Exception {
    Properties defaults = getConfigurationDefault(false, forClassName);

    Properties effectiveDifferences = getEffectiveDifferences(defaults, properties);
    if (!effectiveDifferences.isEmpty()) {
      // Only write the effective differences between the default and the effective properties
      String filePath = getFilePath(forClassName);
      try (OutputStream output = filesystem.createFile(filePath)) {
        effectiveDifferences.store(output, String.format("Current configuration of %s.", forClassName));
      } catch (Exception e) {
        throw new Exception(String.format("Cannot write configuration for %s.", filePath), e);
      }
    }
  }

  private String getFilePath(String forClassName) {
    String propertiesFile = String.format(FILE_PROPERIES_FORMAT, forClassName);
    return filesystem.toPath(LimbusFileService.CONFIG_DIRECTORY, propertiesFile);
  }

  protected static Properties getEffectiveDifferences(Properties defaultProperties, Properties effectiveProperties) {
    Properties toWrite = new Properties();
    if (defaultProperties == null) {
      // If there were no defaults, add all values from the local properties
      toWrite.putAll(effectiveProperties);
    } else {
      Iterator<Entry<Object, Object>> it = effectiveProperties.entrySet()
          .iterator();
      while (it.hasNext()) {
        Entry<Object, Object> entry = it.next();

        Object key = entry.getKey();
        Object effectiveValue = effectiveProperties.get(key);
        // If a default was declared for this key...
        if (defaultProperties.containsKey(key)) {
          // Only write it if it differs from the effective value
          Object defaultValue = defaultProperties.get(key);
          // If the effective value differs from
          if (!defaultValue.equals(effectiveValue)) {
            toWrite.put(key, effectiveValue);
          }
        } else {
          toWrite.put(key, effectiveValue);
        }
      }
    }
    return toWrite;
  }

  /**
   * Puts a property into this property object.
   *
   * <p>
   * <b>
   * Note: Properties only support string values. This method is a shorthand for
   * <tt>put(String, String.valueOf(Object))</tt>.
   * </b>
   * </p>
   *
   *
   * @param key
   *        The property key
   * @param object
   *        The object to put as string value.
   */
  public void put(String key, Object object) {
    put(key, String.valueOf(object));
  }

  /**
   * Puts a property into this property object.
   *
   * @param key
   *        The property key
   * @param value
   *        The string value of the property.
   */
  public void put(String key, String value) {
    properties.put(key, value);
  }

  private Properties getConfigurationDefault(boolean failOnNoDefault, String className) throws Exception {
    String classpathResource = String.format(DEFAULT_CP_FORMAT, className);
    URL url = LimbusProperties.class.getResource(classpathResource);
    Properties defaultProperties = loadProperties(url, failOnNoDefault);
    return defaultProperties;
  }

  private void _checkKey(String key) {
    if (!containsKey(key)) {
      throw new RuntimeException(String.format("Configuration key '%s' not available.", key));
    }
  }

  public <T extends Enum<T>> T getEnum(String key, Class<T> enumType) {
    Lang.denyNull("key", key);
    Lang.denyNull("enumType", enumType);
    _checkKey(key);
    try {
      return Enum.valueOf(enumType, getProperty(key));
    } catch (Exception e) {
      throw new RuntimeException(String.format("Configuration key '%s' cannot be parsed.", key));
    }
  }

  public long getLong(String key) {
    Lang.denyNull("key", key);
    _checkKey(key);
    try {
      return Long.parseLong(getProperty(key));
    } catch (Exception e) {
      throw new RuntimeException(String.format("Configuration key '%s' cannot be parsed.", key));
    }
  }

  public boolean getBoolean(String key) {
    Lang.denyNull("key", key);
    _checkKey(key);
    try {
      return Boolean.parseBoolean(getProperty(key));
    } catch (Exception e) {
      throw new RuntimeException(String.format("Configuration key '%s' cannot be parsed.", key));
    }
  }

  public double getDouble(String key) {
    Lang.denyNull("key", key);
    _checkKey(key);
    try {
      return Double.parseDouble(getProperty(key));
    } catch (Exception e) {
      throw new RuntimeException(String.format("Configuration key '%s' cannot be parsed.", key));
    }
  }

  public float getFloat(String key) {
    Lang.denyNull("key", key);
    _checkKey(key);
    try {
      return Float.parseFloat(getProperty(key));
    } catch (Exception e) {
      throw new RuntimeException(String.format("Configuration key '%s' cannot be parsed.", key));
    }
  }

  public int getInt(String key) {
    Lang.denyNull("key", key);
    _checkKey(key);
    try {
      return Integer.parseInt(getProperty(key));
    } catch (Exception e) {
      throw new RuntimeException(String.format("Configuration key '%s' cannot be parsed.", key));
    }
  }

  public Properties getProperties() {
    return new Properties(properties);
  }

  public boolean containsKey(Object key) {
    Lang.denyNull("key", key);
    return properties.containsKey(key);
  }

  public String getProperty(String key) {
    Lang.denyNull("key", key);
    return properties.getProperty(key);
  }

  public void list(PrintStream out) {
    properties.list(out);
  }

  public void list(PrintWriter out) {
    properties.list(out);
  }

  /**
   * Loads the properties object from the specified URL.
   *
   * @param url
   *        The url
   * @param failOnError
   *        If <code>true</code> exceptions while loading will be wrapped and thrown as {@link Exception},
   *        otherwise <code>false</code> is returned. Use this flag to fail if the properties are unavailable.
   * @return Returns the loaded properties object. In case <tt>failOnError</tt> is false and the properties cannot be
   *         loaded, an empty properties object es returned.
   * @throws Exception
   *         Thrown on any error while loading. Only if <tt>failOnError</tt> is <code>true</code>.
   */
  private Properties loadProperties(URL url, boolean failOnError) throws Exception {
    InputStream inputStream = null;
    Properties properties = new Properties();
    if (url == null) {
      if (failOnError) {
        throw new Exception("Cannot load properties from missing file.");
      } else {
        return properties;
      }
    }
    try {
      URLConnection connection = url.openConnection();
      inputStream = connection.getInputStream();
      properties.load(inputStream);
      return properties;
    } catch (Exception e) {
      if (failOnError) {
        throw new Exception(String.format("Cannot load properties from %s", url.toString()), e);
      } else {
        return properties;
      }
    } finally {
      Lang.closeQuietly(inputStream);
    }
  }

  /**
   * @return Returns <code>true</code> if the properties are empty, <code>false</code> otherwise.
   * @see java.util.Hashtable#isEmpty()
   */
  public boolean isEmpty() {
    return properties.isEmpty();
  }

}
