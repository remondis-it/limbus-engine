package com.remondis.limbus.aether;

import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.resolution.ArtifactResult;

public class AetherUtilTest {

  public static void main(String[] args) throws Exception {

    String groupId, artifactId, extension, version;
    groupId = "com.remondis.modules.transportmanager";
    artifactId = "tpm-server";
    extension = "jar";
    version = "0.5.3-SNAPSHOT";

    List<ArtifactResult> artifactResults = AetherUtil.resolveArtifactAndTransitiveDependencies(groupId, artifactId,
        extension, version);

    // Process results
    {
      for (ArtifactResult artifactResult : artifactResults) {
        Artifact resolved = artifactResult.getArtifact();
        System.out.println(resolved + "\n resolved to \n\t" + artifactResult.getArtifact()
            .getFile());
      }
    }

  }

}
