package com.remondis.limbus.maven;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.util.List;

import org.eclipse.aether.resolution.ArtifactResult;

import com.remondis.limbus.engine.api.maven.MavenArtifact;
import com.remondis.limbus.engine.api.maven.MavenArtifactService;

public class MavenArtifactOverAetherServiceImpl implements MavenArtifactService {

  @Override
  public void initialize() throws Exception {
  }

  @Override
  public void finish() {
  }

  public File getUserSettingsFile() throws Exception {
    return AetherUtil.getUserSettingsFile();
  }

  public File getUserLocalRepository() throws Exception {
    return AetherUtil.getUserLocalRepository();
  }

  public File getUserMavenConfigurationHome() throws Exception {
    return AetherUtil.getUserMavenConfigurationHome();
  }

  public File getUserHome() throws Exception {
    return AetherUtil.getUserHome();
  }

  @Override
  public List<MavenArtifact> resolveArtifactAndTransitiveDependencies(String groupId, String artifactId,
      String extension, String version) throws Exception {
    List<ArtifactResult> artifactResult = AetherUtil.resolveArtifactAndTransitiveDependencies(groupId, artifactId,
        extension, version);
    return artifactResult.stream()
        .map(ar -> new ArtifactResultAdapter(ar))
        .collect(toList());
  }

}
