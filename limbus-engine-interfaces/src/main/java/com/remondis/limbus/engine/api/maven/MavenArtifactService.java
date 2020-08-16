package com.remondis.limbus.engine.api.maven;

import java.io.File;
import java.util.List;

import com.remondis.limbus.api.IInitializable;

/**
 * Service providing features to resolve and download Maven artifacts by their coordinates. Service implementations may
 * rely on the local Maven configuration so retrieve the requires settings.
 * 
 */
public interface MavenArtifactService extends IInitializable<Exception> {

  /**
   * Provides access to the user settings file.
   * 
   * @return Returns the user settings {@link File}.
   * @throws Exception Thrown on any error.
   */
  File getUserSettingsFile() throws Exception;

  /**
   * Provides access to the local repository directory.
   * 
   * @return Returns a {@link File}.
   * @throws Exception Thrown on any error.
   */
  File getUserLocalRepository() throws Exception;

  /**
   * Provides access to the local Maven configuration directory.
   * 
   * @return Returns a {@link File}.
   * @throws Exception Thrown on any error.
   */
  File getUserMavenConfigurationHome() throws Exception;

  /**
   * Provides access to the current user's home directory.
   * 
   * @return Returns a {@link File}.
   * @throws Exception Thrown on any error.
   */
  File getUserHome() throws Exception;

  /**
   * This method resolves a Maven artifact and all of its transitive dependencies by its coordinates. This method tries
   * to resolve the artifacts from the local repository and in case they are not available from there it downloads them
   * from the known repositories discovered in the settings.xml.
   *
   * <p>
   * Note: This method excludes the following scopes from the dependencies: <tt>provided, system, test</tt>.
   * </p>
   *
   * @param groupId
   *        The groupId
   * @param artifactId
   *        The artifactId
   * @param extension
   *        The file extension
   * @param version
   *        The version
   * @return Returns the list of the requestes artifact and all of its transitive dependencies.
   * @throws Exception
   */
  List<MavenArtifact> resolveArtifactAndTransitiveDependencies(String groupId, String artifactId, String extension,
      String version) throws Exception;

}