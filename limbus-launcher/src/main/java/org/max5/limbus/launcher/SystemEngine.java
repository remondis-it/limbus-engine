package org.max5.limbus.launcher;

import static org.max5.limbus.utils.Files.getConfigurationDirectoryUnchecked;
import static org.max5.limbus.utils.Files.getOrFailFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;

import org.max5.limbus.IInitializable;
import org.max5.limbus.system.LimbusSystem;
import org.max5.limbus.utils.Lang;
import org.max5.limbus.utils.SerializeException;

/**
 * The {@link SystemEngine} is an implementation of {@link AbstractEngine} and integrates the Limbus System into a
 * runnable engine. It is then possible to use the component management of {@link LimbusSystem}.
 *
 * <h2>Configuration</h2>
 * The {@link SystemEngine} needs a system description to initialize the Limbus System component management. The system
 * description is assumed to be present in the classpath with filename {@value #DEFAULT_SYSTEM_FILENAME}. The descriptor
 * can also be passed as {@link InputStream} using the constructor {@link #SystemEngine(InputStream)} or the
 * uninitialized instance of {@link LimbusSystem} can be passed using {@link #SystemEngine(LimbusSystem)}.
 *
 * @author schuettec
 *
 */
public class SystemEngine extends AbstractEngine {
  private static final String DEFAULT_SYSTEM_FILENAME = "limbus-system.xml";
  /**
   * Holds the Limbus System to manage components of this engine.
   */
  protected LimbusSystem system;

  /**
   * Constructs a System Engine that reads the system descriptor using the path configured in the Limbus Properties for
   * this class.
   */
  public SystemEngine() {

  }

  /**
   * Constructs a System Engine that reads the system descriptor from the specified input stream.
   *
   * @param descriptorInput
   *        The input stream containing the XML system descriptor.
   * @throws SerializeException
   *         Thrown if the Limbus System cannot be deserialized.
   */
  public SystemEngine(InputStream descriptorInput) throws SerializeException {
    Lang.denyNull("descriptorInput", descriptorInput);
    try {
      this.system = fromInputStream(descriptorInput);
    } finally {
      Lang.closeQuietly(descriptorInput);
    }
  }

  /**
   * Constructs a System Engine that uses the specified uninitialized {@link LimbusSystem}.
   *
   * @param system
   */
  public SystemEngine(LimbusSystem system) {
    this.system = system;
  }

  @Override
  protected void performInitialize() throws Exception {
    if (system == null) {
      this.system = configureLimbusSystem();
    }
    this.system.initialize();
  }

  private LimbusSystem configureLimbusSystem() throws Exception {
    File confDir = getConfigurationDirectoryUnchecked();
    File systemConfig = getOrFailFile("limbus system description", new File(confDir, DEFAULT_SYSTEM_FILENAME));
    try (FileInputStream fin = new FileInputStream(systemConfig)) {
      return fromInputStream(fin);
    }
  }

  private LimbusSystem fromInputStream(InputStream fin) throws SerializeException {
    LimbusSystem limbusSystem = LimbusSystem.deserializeConfiguration(fin);
    return limbusSystem;
  }

  @Override
  protected void performFinish() {
    // Stop the Limbus system
    if (this.system != null) {
      this.system.finish();
      this.system = null;
    }
  }

  /**
   * Provides access to a Limbus component.
   *
   * @param requestType
   *        The request type of the component.
   * @return Returns the Limbus component.
   * @see org.max5.limbus.system.LimbusSystem#getComponent(java.lang.Class)
   */
  public <T extends IInitializable<?>> T getComponent(Class<T> requestType) {
    return system.getComponent(requestType);
  }

}
