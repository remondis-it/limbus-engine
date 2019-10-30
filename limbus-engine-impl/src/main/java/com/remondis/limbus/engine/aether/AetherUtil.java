
package com.remondis.limbus.engine.aether;

import static com.remondis.limbus.utils.Files.getOrFailDirectory;
import static com.remondis.limbus.utils.Files.getOrFailFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import org.apache.maven.repository.internal.MavenRepositorySystemUtils;
import org.apache.maven.settings.Profile;
import org.apache.maven.settings.Repository;
import org.apache.maven.settings.Server;
import org.apache.maven.settings.Settings;
import org.apache.maven.settings.building.DefaultSettingsBuilder;
import org.apache.maven.settings.building.DefaultSettingsBuilderFactory;
import org.apache.maven.settings.building.DefaultSettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingException;
import org.apache.maven.settings.building.SettingsBuildingRequest;
import org.apache.maven.settings.building.SettingsBuildingResult;
import org.eclipse.aether.DefaultRepositorySystemSession;
import org.eclipse.aether.RepositorySystem;
import org.eclipse.aether.RepositorySystemSession;
import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.artifact.DefaultArtifact;
import org.eclipse.aether.collection.CollectRequest;
import org.eclipse.aether.connector.basic.BasicRepositoryConnectorFactory;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyFilter;
import org.eclipse.aether.impl.DefaultServiceLocator;
import org.eclipse.aether.repository.Authentication;
import org.eclipse.aether.repository.LocalRepository;
import org.eclipse.aether.repository.LocalRepositoryManager;
import org.eclipse.aether.repository.RemoteRepository;
import org.eclipse.aether.resolution.ArtifactRequest;
import org.eclipse.aether.resolution.ArtifactResult;
import org.eclipse.aether.resolution.DependencyRequest;
import org.eclipse.aether.resolution.DependencyResolutionException;
import org.eclipse.aether.resolution.DependencyResult;
import org.eclipse.aether.spi.connector.RepositoryConnectorFactory;
import org.eclipse.aether.spi.connector.transport.TransporterFactory;
import org.eclipse.aether.transport.file.FileTransporterFactory;
import org.eclipse.aether.transport.http.HttpTransporterFactory;
import org.eclipse.aether.util.artifact.JavaScopes;
import org.eclipse.aether.util.filter.ScopeDependencyFilter;
import org.eclipse.aether.util.graph.visitor.FilteringDependencyVisitor;
import org.eclipse.aether.util.repository.AuthenticationBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.api.LimbusException;

/**
 * This class handles the access to the Maven repositories for plugin deployment. It contains methods that can access
 * the local Maven installation and the settings (settings.xml) of the current user to retrieve dependencies.
 *
 * @author schuettec
 *
 */
public class AetherUtil {

  private static final Logger log = LoggerFactory.getLogger(AetherUtil.class);

  private AetherUtil() {
  }

  // public static File getGlobalSettingsFile() throws LimbusClasspathException {
  // return LimbusUtil.getOrFailFile("global maven settings", new File(getMavenHome(), "conf/settings.xml"));
  // }

  // public static File getMavenHome() throws LimbusClasspathException {
  // String m2Home = System.getenv("M2_HOME");
  // String prop = System.getProperty("maven.home");
  // File envM2Home = null;
  // if (prop != null) {
  // envM2Home = new File(prop);
  // }
  // if (envM2Home == null && m2Home != null) {
  // envM2Home = new File(m2Home);
  // }
  // return LimbusUtil.getOrFailDirectory("Maven Home", envM2Home);
  // }

  public static File getUserSettingsFile() throws Exception {
    return getOrFailFile("user mavens settings", new File(getUserMavenConfigurationHome(), "settings.xml"));
  }

  public static File getUserLocalRepository() throws Exception {
    return getOrFailDirectory("user local maven repository", new File(getUserMavenConfigurationHome(), "repository"));
  }

  public static File getUserMavenConfigurationHome() throws Exception {
    return getOrFailDirectory("maven user config home directory", new File(getUserHome(), ".m2"));
  }

  public static File getUserHome() throws Exception {
    return getOrFailDirectory("user home", System.getProperty("user.home"));
  }

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
  public static List<ArtifactResult> resolveArtifactAndTransitiveDependencies(String groupId, String artifactId,
      String extension, String version) throws Exception {

    log.info("Resolving Maven Artifact {}:{}:{}:{} - for more information enable debug log.", groupId, artifactId,
        extension, version);
    logMavenSessionInfo();

    LocalRepository localRepository = new LocalRepository(getUserLocalRepository());

    Settings effectiveSettings = getEffectiveSettings();

    List<RemoteRepository> remotes = getConfiguredRemoteRepositories(effectiveSettings);

    // Add the local repository to the list of repositories to resolve the artifact from
    // List<RemoteRepository> resolveFrom = addLocalRepositoryAsRemote(remotes);
    List<RemoteRepository> resolveFrom = remotes;

    logRepositories(resolveFrom);

    RepositorySystem system = newRepositorySystem();
    DefaultRepositorySystemSession session = MavenRepositorySystemUtils.newSession();
    // A local repository manager must be set on the session
    LocalRepositoryManager localRepositoryManager = system.newLocalRepositoryManager(session, localRepository);
    session.setLocalRepositoryManager(localRepositoryManager);

    Artifact artifact = new DefaultArtifact(groupId, artifactId, extension, version);
    ArtifactRequest artifactRequest = new ArtifactRequest();
    artifactRequest.setArtifact(artifact);
    artifactRequest.setRepositories(resolveFrom);
    ArtifactResult resolveArtifact = system.resolveArtifact(session, artifactRequest);

    List<ArtifactResult> artifactResults = getTransitiveCompileDependencies(resolveArtifact.getArtifact(), resolveFrom,
        system, session);
    return artifactResults;
  }

  private static void logMavenSessionInfo() throws LimbusException {
    try {
      StringBuilder logOverview = new StringBuilder("The following paths will be used for this Maven session:");
      logOverview.append("\n")
          .append("          User home path: ")
          .append(getUserHome())
          .append("\n")
          .append("Maven configuration path: ")
          .append(getUserMavenConfigurationHome())
          .append("\n")
          .append("   Local repository path: ")
          .append(getUserLocalRepository())
          .append("\n")
          // .append(" Maven path (M2_HOME): ").append(getMavenHome()).append("\n")
          // .append("Global settings XML file: ").append(getGlobalSettingsFile()).append("\n")
          .append("  User settings XML file: ")
          .append(getUserSettingsFile());
      log.debug(logOverview.toString());
    } catch (Exception e) {
      throw new LimbusException("Some of the required files or folders is not accessible!", e);
    }
  }

  private static void logRepositories(List<RemoteRepository> resolveFrom) {
    StringBuilder logOverview = new StringBuilder(
        "Using effective settings (incl. active profiles) the following remote repositories were calculated to resolve artifacts.\n");

    for (RemoteRepository r : resolveFrom) {
      logOverview.append("- ")
          .append(r.toString())
          .append("\n");
    }

    log.debug(logOverview.toString());
  }

  private static List<ArtifactResult> getTransitiveCompileDependencies(Artifact pArtifact,
      List<RemoteRepository> resolveFrom, RepositorySystem system, RepositorySystemSession session) throws Exception {

    List<String> included = Arrays.asList(new String[] {
        "compile", "runtime"
    });
    List<String> excluded = Arrays.asList(new String[] {
        "provided", "system", "test"
    });

    DependencyFilter filter = new ScopeDependencyFilter(included, excluded);
    try {
      CollectRequest collectRequest = new CollectRequest();
      collectRequest.setRoot(new Dependency(pArtifact, JavaScopes.COMPILE));
      collectRequest.setRepositories(resolveFrom);
      DependencyRequest dependencyRequest = new DependencyRequest(collectRequest, filter);
      DependencyResult resolveDependencies = system.resolveDependencies(session, dependencyRequest);

      // Note: The dependency graph is not filtered! So if a visitor walks through the graph it must apply the filters
      // again!
      resolveDependencies.getRoot()
          .accept(new FilteringDependencyVisitor(new ConsoleDependencyGraphDumper(), filter));

      return resolveDependencies.getArtifactResults();

    } catch (DependencyResolutionException e) {
      throw new Exception("Cannot resolve transitive dependencies for " + pArtifact, e);
    }
  }

  /**
   * Creates the list of remote repositories references in the settings.xml and configures the authentication for them
   * in order to access those repositories for later artifact resolution.
   *
   * <p>
   * This method also recognizes the configured active profiles from the settings.xml to get the effective
   * configuration.
   * </p>
   *
   * @param effectiveSettings
   *        The effective settings.
   * @return
   */
  private static List<RemoteRepository> getConfiguredRemoteRepositories(Settings effectiveSettings) {
    Map<String, Profile> profilesMap = effectiveSettings.getProfilesAsMap();

    List<RemoteRepository> remotes = new ArrayList<>(20);
    for (String profileName : effectiveSettings.getActiveProfiles()) {
      Profile profile = profilesMap.get(profileName);
      List<Repository> repositories = profile.getRepositories();
      for (Repository repo : repositories) {
        // Build repo authentication
        String serverId = repo.getId();
        Server server = effectiveSettings.getServer(serverId);
        Authentication repoAuth = new AuthenticationBuilder().addUsername(server.getUsername())
            .addPassword(server.getPassword())
            .build();

        /*
         * NOTE: At this point we ignore the proxy settings from settings.xml because that gives an error related to
         * NTLM proxies which are "currently not supported" by Maven.
         * According to this article
         * https://maven.apache.org/guides/mini/guide-proxies.html
         * we temporarily skip proxy configuration for the remotes.
         */
        // Build proxy config for this repo
        // org.apache.maven.settings.Proxy mavenProxy = effectiveSettings.getActiveProxy();
        // Proxy proxy = toProxy(mavenProxy);

        RemoteRepository remoteRepo = new RemoteRepository.Builder(repo.getId(), "default", repo.getUrl())
            .setAuthentication(repoAuth)
            // .setProxy(proxy)
            .build();
        remotes.add(remoteRepo);
      }
    }
    return remotes;
  }

  // private static RemoteRepository getLocalRepositoryAsRemote() throws MalformedURLException, LimbusException
  // {
  // RemoteRepository local = new RemoteRepository.Builder("local", "default",
  // getUserLocalRepository().toURI().toURL().toString()).build();
  // return local;
  // }

  private static Settings getEffectiveSettings() throws SettingsBuildingException, LimbusException {
    try {
      SettingsBuildingRequest settingsBuildingRequest = new DefaultSettingsBuildingRequest();
      settingsBuildingRequest.setSystemProperties(System.getProperties());
      settingsBuildingRequest.setUserSettingsFile(getUserSettingsFile());

      // settingsBuildingRequest.setGlobalSettingsFile(getGlobalSettingsFile());

      SettingsBuildingResult settingsBuildingResult;
      DefaultSettingsBuilderFactory mvnSettingBuilderFactory = new DefaultSettingsBuilderFactory();
      DefaultSettingsBuilder settingsBuilder = mvnSettingBuilderFactory.newInstance();
      settingsBuildingResult = settingsBuilder.build(settingsBuildingRequest);
      Settings effectiveSettings = settingsBuildingResult.getEffectiveSettings();
      return effectiveSettings;
    } catch (Exception e) {
      throw new LimbusException("Some of the required files or folders is not accessible!", e);
    }
  }

  // private static Proxy toProxy(org.apache.maven.settings.Proxy mavenProxy) {
  // Proxy result = null;
  // if (mavenProxy != null) {
  // AuthenticationBuilder authBuilder = new AuthenticationBuilder();
  // authBuilder.addUsername(mavenProxy.getUsername()).addPassword(mavenProxy.getPassword());
  // result = new Proxy(mavenProxy.getProtocol(), mavenProxy.getHost(), mavenProxy.getPort(), authBuilder.build());
  // }
  // return result;
  // }

  private static RepositorySystem newRepositorySystem() {
    DefaultServiceLocator locator = MavenRepositorySystemUtils.newServiceLocator();
    locator.addService(RepositoryConnectorFactory.class, BasicRepositoryConnectorFactory.class);
    locator.addService(TransporterFactory.class, FileTransporterFactory.class);
    locator.addService(TransporterFactory.class, HttpTransporterFactory.class);

    return locator.getService(RepositorySystem.class);
  }
}
