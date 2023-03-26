package com.remondis.limbus.engine.api.maven;

import java.io.File;

/**
 * Provides access to a Maven artifact.
 */
public interface MavenArtifact {

  /**
   * Gets the group identifier of this artifact, for example "org.apache.maven".
   * 
   * @return The group identifier, never {@code null}.
   */
  String getGroupId();

  /**
   * Gets the artifact identifier of this artifact, for example "maven-model".
   * 
   * @return The artifact identifier, never {@code null}.
   */
  String getArtifactId();

  /**
   * Gets the version of this artifact, for example "1.0-20100529-1213". Note that in case of meta versions like
   * "1.0-SNAPSHOT", the artifact's version depends on the state of the artifact. Artifacts that have been resolved or
   * deployed will usually have the meta version expanded.
   * 
   * @return The version, never {@code null}.
   */
  String getVersion();

  /**
   * Gets the classifier of this artifact, for example "sources".
   * 
   * @return The classifier or an empty string if none, never {@code null}.
   */
  String getClassifier();

  /**
   * Gets the (file) extension of this artifact, for example "jar" or "tar.gz".
   * 
   * @return The file extension (without leading period), never {@code null}.
   */
  String getExtension();

  /**
   * Gets the file of this artifact. Note that only resolved artifacts have a file associated with them. In general,
   * callers must not assume any relationship between an artifact's filename and its coordinates.
   * 
   * @return The file or {@code null} if the artifact isn't resolved.
   */
  File getFile();

}