package com.remondis.limbus.engine.api.maven;

/**
 * Simple Java Bean implementation of {@link MavenArtifact}.
 */
public class MavenCoordinates {

  private String groupId;

  private String artifactId;

  private String version;

  private String classifier;

  private String extension;

  public MavenCoordinates() {
    super();
  }

  public MavenCoordinates(String groupId, String artifactId, String version) {
    this(groupId, artifactId, version, null, null);
  }

  public MavenCoordinates(String groupId, String artifactId, String version, String classifier, String extension) {
    super();
    this.groupId = groupId;
    this.artifactId = artifactId;
    this.version = version;
    this.classifier = classifier;
    this.extension = extension;
  }

  public String getGroupId() {
    return groupId;
  }

  public void setGroupId(String groupId) {
    this.groupId = groupId;
  }

  public String getArtifactId() {
    return artifactId;
  }

  public void setArtifactId(String artifactId) {
    this.artifactId = artifactId;
  }

  public String getVersion() {
    return version;
  }

  public void setVersion(String version) {
    this.version = version;
  }

  public String getClassifier() {
    return classifier;
  }

  public void setClassifier(String classifier) {
    this.classifier = classifier;
  }

  public String getExtension() {
    return extension;
  }

  public void setExtension(String extension) {
    this.extension = extension;
  }

}
