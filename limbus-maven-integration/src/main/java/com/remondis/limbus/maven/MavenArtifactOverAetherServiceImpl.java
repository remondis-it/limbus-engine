package com.remondis.limbus.maven;

import static java.util.stream.Collectors.toList;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Set;

import org.eclipse.aether.resolution.ArtifactResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.engine.api.DeployService;
import com.remondis.limbus.engine.api.maven.MavenArtifact;
import com.remondis.limbus.engine.api.maven.MavenArtifactService;
import com.remondis.limbus.engine.api.maven.MavenCoordinates;
import com.remondis.limbus.utils.Lang;

public class MavenArtifactOverAetherServiceImpl implements MavenArtifactService {

  private static final Logger log = LoggerFactory.getLogger(DeployService.class);

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

  public void downloadAndCopyMavenArtifacts(Set<MavenCoordinates> artifacts, File targetDirectory)
      throws Exception, IOException, FileNotFoundException {
    for (MavenCoordinates coordinates : artifacts) {
      String groupId = coordinates.getGroupId();
      String artifactId = coordinates.getArtifactId();
      String version = coordinates.getVersion();

      log.info("Downloading Maven artifact: " + coordinates);

      // Normalize extension
      String extension = MavenArtifactService.defaultExtensionIfNull(null);

      List<MavenArtifact> mavenArtifacts = resolveArtifactAndTransitiveDependencies(groupId, artifactId, extension,
          version);

      for (MavenArtifact artifact : mavenArtifacts) {
        File artifactFile = artifact.getFile();
        File pluginArtifact = new File(targetDirectory, artifactFile.getName());
        log.debug("...copying Maven artifact to: " + pluginArtifact.toString());
        try (FileInputStream fin = new FileInputStream(artifactFile);
            FileOutputStream fout = new FileOutputStream(pluginArtifact);) {
          Lang.copy(fin, fout);
        }
      }
    }
  }

}
