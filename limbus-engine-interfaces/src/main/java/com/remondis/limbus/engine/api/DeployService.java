package com.remondis.limbus.engine.api;

import java.io.File;
import java.security.Permission;
import java.util.List;
import java.util.Set;

import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.api.IInitializable;
import com.remondis.limbus.api.LimbusException;

/**
 * This is the public deploy service interface providing deploy features for filesystem or Maven deploy.
 * <p>
 * <b>Note:</b> The {@link DeployService} only provides extended deploy features but does not take undeployment
 * operations into account. Use the deploy name of the classpath with {@link LimbusEngine#getClasspath(String)} in
 * conjunction with {@link LimbusEngine#undeployPlugin(Classpath)} to undeploy a classpath. The deploy service provides
 * methods to get the deploy name of classpaths that can be used with the {@link LimbusEngine}.
 * </p>
 *
 *
 *
 * @author schuettec
 *
 */
public interface DeployService extends IInitializable<LimbusException>, DeploymentListener {

  /**
   * This is the default Maven extension used for deployments in most cases.
   */
  public static final String DEFAULT_MAVEN_EXTENSION = "jar";

  /**
   * @return Returns <code>true</code> if the hot deploy listener is active, <code>false</code> otherwise.
   */
  public boolean isHotDeployFolderActive();

  /**
   * Deploys a Maven artifact to this container.
   *
   * @param groupId
   *        The group id
   * @param artifactId
   *        The artifact id
   * @param extension
   *        (optional) The extension, defaults to "jar".
   * @param version
   *        The version
   * @param permissions
   *        The permissions to be granted for classes of this classpath.
   * @throws LimbusException
   *         Thrown on any error while downloading, processing or deploying the Maven artifact.
   */
  public void deployMavenArtifact(String groupId, String artifactId, String extension, String version,
      Set<Permission> permissions) throws LimbusException;

  /**
   * Deploys the specified ZIP file to this container.
   *
   * @param zipDeployment
   *        The ZIP file.
   * @param permissions
   *        The permissions to be granted for classes of this classpath.
   * @throws LimbusException
   *         Thrown on any error while unpacking or deploying this ZIP file.
   */
  public void deployZipFile(File zipDeployment, Set<Permission> permissions) throws LimbusException;

  /**
   * Starts the deploy process for an artifact already present in the containers work directory.
   *
   * @param deployName
   *        The deploy name
   * @param permissions
   *        The permissions to be granted for classes of this classpath.
   * @throws LimbusException
   *         Thrown on any error while deploying.
   */
  public void deployFromFilesystem(String deployName, Set<Permission> permissions) throws LimbusException;

  /**
   * @return Returns <code>true</code> if the container was configured to clean its working directory.
   */
  public boolean isCleanWorkDirectory();

  /**
   * @return Returns the list of available deployment names.
   */
  public List<String> getDeployedComponents();

  /**
   * Creates the deploy name from the Maven coordinates.
   *
   * @param groupId
   *        The group id
   * @param artifactId
   *        The artifact Id
   * @param extension
   *        The file extension.
   * @param version
   *        The version
   * @return Returns the Limbus Engine deploy name.
   */
  public String toDeployName(String groupId, String artifactId, String extension, String version);

  /**
   * Creates the deploy name from a ZIP file.
   * 
   * @param zipFile
   *        The ZIP file to create a deploy name for.
   *
   * @return Returns the Limbus Engine deploy name.
   */
  public String toDeployName(File zipFile);

  /**
   * @return Returns the current deploy directory for monitoring purposes.
   */
  public File getDeployDirectoryUnchecked();

  /**
   * @return Returns the current work directory for monitoring purposes.
   */
  public File getWorkDirectoryUnchecked();
}
