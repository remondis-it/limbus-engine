package com.remondis.limbus.maven;

import java.io.File;

import org.eclipse.aether.resolution.ArtifactResult;

import com.remondis.limbus.engine.api.maven.MavenArtifact;

public class ArtifactResultAdapter implements MavenArtifact {

  private ArtifactResult result;

  public ArtifactResultAdapter(ArtifactResult result) {
    super();
    this.result = result;
  }

  @Override
  public String getGroupId() {
    return result.getArtifact()
        .getGroupId();
  }

  @Override
  public String getArtifactId() {
    return result.getArtifact()
        .getArtifactId();
  }

  @Override
  public String getVersion() {
    return result.getArtifact()
        .getVersion();
  }

  @Override
  public String getClassifier() {
    return result.getArtifact()
        .getClassifier();
  }

  @Override
  public String getExtension() {
    return result.getArtifact()
        .getExtension();
  }

  @Override
  public File getFile() {
    return result.getArtifact()
        .getFile();
  }

}
