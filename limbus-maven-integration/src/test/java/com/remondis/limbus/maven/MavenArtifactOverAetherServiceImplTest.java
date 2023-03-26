package com.remondis.limbus.maven;

import java.util.List;

import com.remondis.limbus.engine.api.maven.MavenArtifact;
import com.remondis.limbus.engine.api.maven.MavenArtifactService;

public class MavenArtifactOverAetherServiceImplTest {

  public static void main(String[] args) throws Exception {

    String groupId, artifactId, extension, version;
    groupId = "com.remondis.modules.transportmanager";
    artifactId = "tpm-server";
    extension = "jar";
    version = "0.5.3-SNAPSHOT";

    MavenArtifactService service = new MavenArtifactOverAetherServiceImpl();

    List<MavenArtifact> artifactResults = service.resolveArtifactAndTransitiveDependencies(groupId, artifactId,
        extension, version);

    // Process results
    for (MavenArtifact artifactResult : artifactResults) {
      System.out
          .println("Artifact with coordingates: " + artifactResult.getGroupId() + ":" + artifactResult.getArtifactId()
              + ":" + artifactResult.getVersion() + "\n resolved to \n\t" + artifactResult.getFile());
    }
  }
}
