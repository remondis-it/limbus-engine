package com.remondis.limbus.engine;

import static java.util.Objects.nonNull;

import java.io.File;
import java.util.List;
import java.util.ServiceLoader;

import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.engine.api.maven.MavenArtifact;
import com.remondis.limbus.engine.api.maven.MavenArtifactService;

/**
 * This class provides an instance of {@link MavenArtifactService} located by the {@link ServiceLoader}. This
 * integration was build because Maven/Aether-dependencies are not compatible with Java 9 module system. Since the
 * Aether project was terminated and Maven developers announced that no Java 9 compatibility is ever planned for Maven
 * "internal" APIs, the legacy libraries are loaded from class path via ServiceLoader to ensure that they stay available
 * for Limbus engine applications.
 */
public class MavenArtifactServiceIntegrator extends Initializable<Exception> implements MavenArtifactService {

  /**
   * Instance from Service Loader.
   */
  private MavenArtifactService delegate;

  @Override
  protected void performInitialize() throws Exception {
    super.performInitialize();

    ServiceLoader<MavenArtifactService> serviceLoader = ServiceLoader.load(MavenArtifactService.class);
    this.delegate = serviceLoader.findFirst()
        .orElseThrow(() -> new Exception("Cannot locate MavenArtifactService from ServiceLoader!\n"
            + "Please check dependencies and their respective META-INF/services providing this type of service."));
    this.delegate.initialize();
  }

  @Override
  protected void performFinish() {
    super.performFinish();
    if (nonNull(delegate)) {
      delegate.finish();
      this.delegate = null;
    }
  }

  @Override
  public File getUserSettingsFile() throws Exception {
    return delegate.getUserSettingsFile();
  }

  @Override
  public File getUserLocalRepository() throws Exception {
    return delegate.getUserLocalRepository();
  }

  @Override
  public File getUserMavenConfigurationHome() throws Exception {
    return delegate.getUserMavenConfigurationHome();
  }

  @Override
  public File getUserHome() throws Exception {
    return delegate.getUserHome();
  }

  @Override
  public List<MavenArtifact> resolveArtifactAndTransitiveDependencies(String groupId, String artifactId,
      String extension, String version) throws Exception {
    return delegate.resolveArtifactAndTransitiveDependencies(groupId, artifactId, extension, version);
  }

}
