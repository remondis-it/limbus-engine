package org.max5.limbus.launcher;

import java.io.InputStream;
import java.util.Properties;

import org.max5.limbus.utils.Lang;

/**
 * Contains util methods for internal use by engine extensions.
 *
 * @author schuettec
 *
 */
public final class EngineUtil {

  private static final String UNKNOWN = "unknown";

  static {
    Properties p = new Properties();
    InputStream inputStream = null;
    try {
      inputStream = EngineUtil.class.getResourceAsStream("/version.properties");
      p.load(inputStream);
      GROUP_ID = p.getProperty("engine.groupId", "unkown");
      ARTIFACT_ID = p.getProperty("engine.artifactId", "unkown");
      VERSION = p.getProperty("engine.version", "unkown");
    } catch (Exception e) {
      e.printStackTrace();
      GROUP_ID = UNKNOWN;
      ARTIFACT_ID = UNKNOWN;
      VERSION = UNKNOWN;
    } finally {
      Lang.closeQuietly(inputStream);
    }
  }

  public static String GROUP_ID;
  public static String ARTIFACT_ID;
  public static String VERSION;

  private EngineUtil() {
  }

  /**
   * @return Returns the version of the engine.
   */
  public static String getEngineVersion() {
    return VERSION;
  }

  public static String getEngineGroupId() {
    return GROUP_ID;
  }

  public static String getEngineArtifactId() {
    return ARTIFACT_ID;
  }

}
