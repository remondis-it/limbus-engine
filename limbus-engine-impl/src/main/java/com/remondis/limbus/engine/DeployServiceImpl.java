package com.remondis.limbus.engine;

import static com.remondis.limbus.utils.Files.createIfMissingDirectory;
import static com.remondis.limbus.utils.Files.getCurrentDirectory;
import static com.remondis.limbus.utils.Files.getOrFailDirectory;
import static com.remondis.limbus.utils.Files.isAccessibleDirectory;
import static java.nio.file.StandardWatchEventKinds.ENTRY_CREATE;
import static java.nio.file.StandardWatchEventKinds.ENTRY_DELETE;
import static java.nio.file.StandardWatchEventKinds.OVERFLOW;

import java.io.File;
import java.io.FileFilter;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.WatchEvent;
import java.nio.file.WatchKey;
import java.nio.file.WatchService;
import java.security.Permission;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.api.LimbusException;
import com.remondis.limbus.engine.api.DeployService;
import com.remondis.limbus.engine.api.LimbusEngine;
import com.remondis.limbus.engine.api.maven.MavenArtifact;
import com.remondis.limbus.engine.api.maven.MavenArtifactService;
import com.remondis.limbus.engine.api.security.LimbusSecurity;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.properties.LimbusProperties;
import com.remondis.limbus.system.api.LimbusComponent;
import com.remondis.limbus.utils.Zip;

/**
 * This class provides additional deploy features for a {@link LimbusEngine}. There are a few scenarios in deploying
 * a plugin:
 * <ol>
 * <li><b>Deploy a set of URLs directly:</b> The container will access the provided URLs directly for file reading.</li>
 * <li><b>Deploy using filesystem repository:</b> The deploy service retrieves the classpath files from the deployer and
 * caches the referenced files in the local filesystem <b>(in an exclusive readable 'work' directory)</b>. When copying
 * was finished the deploy event is triggered to read all files of the added folder exclusively.</li>
 * <li><b>Deploy by filesystem observation:</b> The deploy service listens to a specific location on the filesystem
 * <b>(a non exclusive readable directory)</b> for incoming deployments in ZIP format. The deployment will be unpacked
 * to 'work' and then <b>step 1</b> is performed.</li>
 * <li><b>Deploy by maven:</b> The deploy service gets maven coordinates from the deployer and resolves them to the set
 * of URLs. Then
 * <ul>
 * <li><b>Step 1</b> can be performed to not duplicate the file content (in the maven repo, in the 'work' directory
 * (step 2) and in the file system observation directory (step 3))
 * <p>
 * <b>Drawback:</b>
 * <ul>
 * <li>Cannot undeploy via filesystem</li>
 * <li>JARs in the Maven Repository (.m2) may be locked while reading. Other maven processes may not work correctly.
 * </li>
 * </ul>
 * </p>
 * </li>
 * <li><b>Step 2</b> can be performed. No JAR locking in maven (.m2). Duplicated the files. No undeploy via filesystem.
 * </li>
 * <li><b>Step 3</b> cannot be performed because a specific ZIP format is specified for filesystem deployment.</li>
 * </ul>
 * </li>
 * </ol>
 *
 * <p>
 * <b>Note: The deployment listener only accepts ZIP files containing JAR files. For now the only supported structure is
 * a ZIP file containing all JAR files in its root.</b>
 * </p>
 *
 * @author schuettec
 *
 */
public class DeployServiceImpl extends Initializable<LimbusException> implements DeployService {

  private static final String HOT_DEPLOY_THREAD_NAME = "HotDeploy Listener";

  private static final Logger log = LoggerFactory.getLogger(DeployService.class);

  @LimbusComponent
  protected LimbusEngine container;

  @LimbusComponent
  protected LimbusSecurity limbusSecurity;

  @LimbusComponent
  protected LimbusFileService filesystem;

  @LimbusComponent
  protected MavenArtifactService artifacts;

  private Thread directoryWatcher;
  private WatchService watcher;
  private WatchKey key;
  private Path dir;

  /**
   * Holds the limbus properties for the deploy service.
   */
  private LimbusProperties properties;

  /**
   * Reads the subfolder for hot deploy from the properties. Files here are intended to be added and removed to signal a
   * (un)deploy via filesystem.
   */
  private String deployFolder() {
    checkState();
    return this.properties.getProperty("deploy-folder");
  }

  /**
   * This is the subfolder for deployed classpath access by the limbus container. Files here are assumed to be readable
   * exclusively by the container.
   */
  private String workFolder() {
    checkState();
    return this.properties.getProperty("work-folder");
  }

  /**
   * @return Returns <code>true</code> if the work folder cleaning feature is enabled, <code>false</code> otherwise.
   */
  @Override
  public boolean isCleanWorkDirectory() {
    checkState();
    return this.properties.getBoolean("clean-work-folder");
  }

  /**
   * @return Returns <code>true</code> if the hot deploy feature is enabled, or <code>false</code> otherwise.
   */
  @Override
  public boolean isHotDeployFolderActive() {
    checkState();
    return this.properties.getBoolean("enable-hot-deploy");
  }

  @Override
  protected void performInitialize() throws LimbusException {
    try {
      // Create the properties
      this.properties = new LimbusProperties(filesystem, getClass(), true, false);

      // Register as a deployment listener
      container.addDeploymentListener(this);

      // When created by the container, clean old deployments
      cleanWorkDirectory();

      // Make sure work folder is available
      getCreateOrFailWorkDirectory();

      // Make sure deploy folder is available
      getCreateOrFailDeployDirectory();

      // Start hot deploy listener
      startFileDeployWatchService();

      // Deploy all artifacts from deploy directory.
      deployAll();

    } catch (Exception e) {
      throw new LimbusException("Cannot initialize Limbus Deploy Service.", e);
    }

  }

  /**
   * This method starts the deploy process for each zip file in the deploy folder.
   *
   * @throws LimbusException
   *         Thrown if the deploy directory is inaccessible or the deploy of an artifact fails.
   */
  private void deployAll() throws LimbusException {
    try {

      File deployDir = getCreateOrFailDeployDirectory();
      File[] toDeploys = deployDir.listFiles(new FileFilter() {

        @Override
        public boolean accept(File pathname) {
          return pathname.getName()
              .endsWith(".zip");
        }
      });

      List<Exception> exceptions = new LinkedList<>();

      for (File toDeploy : toDeploys) {
        try {
          // TODO - schuettec - 14.10.2016 : Only default sandbox permissions are granted for plugins deployed by the
          // filesystem.
          deployZipFile(toDeploy, limbusSecurity.getSandboxDefaultPermissions());
        } catch (LimbusException e) {
          exceptions.add(e);
        }
      }

      if (!exceptions.isEmpty()) {
        String deployErrorMessage = "Multiple errors encountered while deploying the artifacts from deploy directory!";
        Exception firstException = exceptions.get(0);
        exceptions.remove(firstException);
        LimbusException e = new LimbusException(deployErrorMessage, firstException, true, true);
        for (Exception suppressed : exceptions) {
          e.addSuppressed(suppressed);
        }
        throw e;
      }

    } catch (Exception e) {
      throw new LimbusException("Error while deploying from filesystem.", e);
    }
  }

  private void startFileDeployWatchService() throws LimbusException {
    if (isHotDeployFolderActive()) {
      try {
        this.dir = Paths.get(".", deployFolder())
            .toAbsolutePath()
            .normalize();
        log.info("Hot deploy detection started on folder {}", dir.toString());
        this.watcher = FileSystems.getDefault()
            .newWatchService();
        this.key = dir.register(watcher, ENTRY_CREATE, ENTRY_DELETE);

        this.directoryWatcher = new Thread(new Runnable() {

          @Override
          public void run() {
            while (!Thread.interrupted()) {

              // wait for key to be signalled
              WatchKey key;
              try {
                key = watcher.take();
              } catch (Exception x) {
                // This can happen any time on engine shutdown.
                return;
              }

              for (WatchEvent<?> event : key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                // TBD - provide example of how OVERFLOW event is handled
                if (kind == OVERFLOW) {
                  continue;
                }

                // Context for directory entry event is the file name of entry
                @SuppressWarnings("unchecked")
                WatchEvent<Path> ev = (WatchEvent<Path>) event;
                Path name = ev.context();
                Path child = dir.resolve(name);

                // reset key and remove from set if directory no longer accessible
                key.reset();

                // Create the plugin classpath
                File zipDeployment = child.toFile();

                // Check deployment format
                if (zipDeployment.getName()
                    .endsWith(".zip")) {
                  String deployName = toDeployName(zipDeployment.getName());
                  // Try to unpack the ZIP file to work directory.
                  if (kind == ENTRY_CREATE) {
                    try {
                      // Undeploy the classpath
                      // TODO - schuettec - 14.10.2016 : Only default sandbox permissions are granted for plugins
                      // deployed
                      // by the directory deploy.
                      deployZipFile(zipDeployment, limbusSecurity.getSandboxDefaultPermissions());
                    } catch (LimbusException e) {
                      log.error(String.format("Error while deploying a ZIP bundle from filesystem: %s (%s)", deployName,
                          zipDeployment.getAbsolutePath()));
                    }
                  } else if (kind == ENTRY_DELETE) {
                    // Undeploy the classpath
                    try {
                      undeployFromFilesystem(deployName);
                    } catch (LimbusException e) {
                      log.error("Error while undeploying plugin from container: {} ({})", deployName,
                          zipDeployment.getAbsolutePath());
                    }
                  }
                } else {
                  log.warn("A non-ZIP file was detected in the hot deploy directory - ignoring {}",
                      zipDeployment.getAbsolutePath());
                }
              }
            }
          }
        }, HOT_DEPLOY_THREAD_NAME);
        directoryWatcher.start();
      } catch (Exception e) {
        throw new LimbusException("Cannot start listener on hot deploy directory.", e);
      }
    }
  }

  private void undeployFromFilesystem(String deployName) throws LimbusException {
    checkState();
    if (hasPlugin(deployName)) {
      Classpath classpath = findPlugin(deployName);
      try {
        container.undeployPlugin(classpath);
      } finally {
        removePlugin(deployName);
      }
    }
  }

  private boolean hasPlugin(String deployName) {
    checkState();
    File pluginFolder = new File(getWorkDirectoryUnchecked(), deployName);
    return pluginFolder.isDirectory();
  }

  public File getOrFailPluginDirectory(String deployName) throws Exception {
    checkState();
    return getOrFailDirectory("plugin", getPluginDirectoryUnchecked(deployName));
  }

  public File getPluginDirectoryUnchecked(String deployName) throws Exception {
    checkState();
    File workDirectory = getOrFailWorkDirectory();
    return new File(workDirectory, deployName);
  }

  private File getCreateOrFailPluginDirectory(String deployName) throws Exception {
    checkState();
    File deployDirectory = getPluginDirectoryUnchecked(deployName);
    try {
      if (!deployDirectory.exists()) {
        deployDirectory.mkdir();
      }
      return getOrFailDirectory("plugin", deployDirectory);
    } catch (Exception e) {
      throw new LimbusException(
          String.format("Cannot create plugin deploy folder %s", deployDirectory.getAbsolutePath()), e);
    }
  }

  @Override
  public String deployMavenArtifact(String groupId, String artifactId, String extension, String version,
      Set<Permission> permissions) throws LimbusException {
    checkState();

    // Normalize extension
    extension = defaultExtensionIfNull(extension);
    String deployName = toDeployName(groupId, artifactId, extension, version);
    try {
      File pluginDirectory = getCreateOrFailPluginDirectory(deployName);

      List<MavenArtifact> mavenArtifacts = artifacts.resolveArtifactAndTransitiveDependencies(groupId, artifactId,
          extension, version);

      for (MavenArtifact artifact : mavenArtifacts) {
        File artifactFile = artifact.getFile();
        File pluginArtifact = new File(pluginDirectory, artifactFile.getName());
        try (FileInputStream fin = new FileInputStream(artifactFile);
            FileOutputStream fout = new FileOutputStream(pluginArtifact);) {
          IOUtils.copy(fin, fout);
        }
      }

      // Trigger deploy process
      deployFromFilesystem(deployName, permissions);

      return deployName;

    } catch (Exception e) {
      // Clean deployed files from work
      removePlugin(deployName);

      throw new LimbusException(String.format("Cannot deploy the Maven artifact %s:%s:%s:%s due to an exception.",
          groupId, artifactId, extension, version), e);
    }

  }

  @Override
  public String toDeployName(File file) {
    checkState();
    return toDeployName(file.getName());
  }

  public String toDeployName(String zipFileName) {
    checkState();
    if (zipFileName.endsWith(".zip")) {
      return zipFileName.substring(0, zipFileName.length() - 4);
    }
    return zipFileName;
  }

  @Override
  public String toDeployName(String groupId, String artifactId, String extension, String version) {
    checkState();
    extension = defaultExtensionIfNull(extension);
    return String.format("%s_%s_%s_%s", groupId, artifactId, extension, version);
  }

  private String defaultExtensionIfNull(String extension) {
    if (extension == null) {
      return DEFAULT_MAVEN_EXTENSION;
    } else {
      return extension;
    }
  }

  /**
   * Deploys the specified ZIP file to this container.
   *
   * @param zipDeployment
   *        The ZIP file.
   * @param permissions
   *        The permissions to be granted for classes of this classpath.
   * @throws LimbusException
   *         Thrown on any error while unpacking or deploying this ZIP file.
   */
  @Override
  public void deployZipFile(File zipDeployment, Set<Permission> permissions) throws LimbusException {
    checkState();

    String deployName = toDeployName(zipDeployment.getName());

    if (hasPlugin(deployName)) {
      throw new LimbusException(String.format("The plugin '%s' is already deployed.", deployName));
    }

    try {
      File deployDirectory = getCreateOrFailPluginDirectory(deployName);
      Zip.unpack(zipDeployment, deployDirectory);
    } catch (Exception e) {
      throw new LimbusException(String.format("Cannot unpack ZIP plugin bundle %s", zipDeployment.getAbsolutePath()),
          e);
    }

    // Trigger deploy
    deployFromFilesystem(deployName, permissions);
  }

  /**
   * Starts the deploy process for an artifact already present in the containers work directory.
   *
   * @param deployName
   *        The deploy name
   * @param permissions
   *        The permissions to be granted for classes of this classpath.
   * @throws LimbusException
   *         Thrown on any error while deploying.
   */
  @Override
  public void deployFromFilesystem(String deployName, Set<Permission> permissions) throws LimbusException {
    checkState();
    try {
      File pluginDirectory = getCreateOrFailPluginDirectory(deployName);

      // Create the classpath of the deployed files
      Classpath classpath = Classpath.create(deployName)
          .addAllFilesInDirectory(pluginDirectory);
      container.deployPlugin(classpath, permissions);
    } catch (Exception e) {
      // Clean deployed files from work
      removePlugin(deployName);

      // Forward deploy exception
      throw new LimbusException(String.format("Cannot deploy the specified classpath with deploy name %s", deployName),
          e);
    }
  }

  private void removePlugin(String deployName) {
    // In case of a deploy error the container already undeployed the corrupt plugin
    // Delete the plugin from work folder
    try {
      if (hasPlugin(deployName)) {
        File pluginDirectory = getPluginDirectoryUnchecked(deployName);
        FileUtils.deleteDirectory(pluginDirectory);
      }
    } catch (Exception e1) {
      log.warn("Could not delete plugin classpath files from the container. Check the plugin implementation!", e1);
    }
  }

  @Override
  protected void performFinish() {
    if (directoryWatcher != null) {
      try {
        directoryWatcher.interrupt();
      } catch (Exception e) {
        // Nothing to do here
      }
      directoryWatcher = null;
    }

    if (key != null) {
      try {
        key.cancel();
      } catch (Exception e) {
        // Nothing to do here
      }
      key = null;
    }

    // Stop directory watcher
    if (watcher != null) {
      try {
        watcher.close();
      } catch (Exception e) {
        // Nothing to do here
      }
      watcher = null;
    }

    // buschmann - 03.05.2017 : The Deploy Service is responsible for remove plugins deployed by this service. This
    // component should hold the control about his own resources in his hand and can not assume that other components
    // clear the resources.
    List<String> deployedComponents = getDeployedComponents();
    for (String deployName : deployedComponents) {
      try {
        container.undeployPlugin(findPlugin(deployName));
      } catch (LimbusException e) {
        log.error(String.format("Cannot undeploy the deployment with name \'%s\'.", deployName), e);
      }
    }

    // Deregister as a deployment listener. This must happen after undeploy of deployed components.
    container.removeDeploymentListener(this);

    this.properties = null;
  }

  private void cleanWorkDirectory() {
    if (isCleanWorkDirectory()) {
      File workDirectory = getWorkDirectoryUnchecked();
      try {
        if (workDirectory.isDirectory()) {
          FileUtils.deleteDirectory(workDirectory);
        }
      } catch (Exception e) {
        log.warn("Cannot delete work directory: {}", workDirectory.getAbsolutePath(), e);
      }
    }
  }

  @Override
  public File getWorkDirectoryUnchecked() {
    checkState();
    File folder = new File(getCurrentDirectory(), workFolder());
    return folder;
  }

  @Override
  public File getDeployDirectoryUnchecked() {
    checkState();
    File deployFolder = new File(getCurrentDirectory(), deployFolder());
    return deployFolder;
  }

  public File getCreateOrFailWorkDirectory() throws Exception {
    checkState();
    File folder = getWorkDirectoryUnchecked();
    createIfMissingDirectory(folder);
    return getOrFailDirectory("work", folder);
  }

  public File getCreateOrFailDeployDirectory() throws Exception {
    checkState();
    File deployFolder = getDeployDirectoryUnchecked();
    createIfMissingDirectory(deployFolder);
    return getOrFailDirectory("deploy", deployFolder);
  }

  public File getOrFailWorkDirectory() throws Exception {
    checkState();
    return getOrFailDirectory("work", getWorkDirectoryUnchecked());
  }

  public File getOrFailDeployDir() throws Exception {
    checkState();
    return getOrFailDirectory("deploy", getDeployDirectoryUnchecked());
  }

  /**
   * Searches a plugin in the {@link #workFolder()} folder. The plugin name is used as folder name. If
   * present, the classpath object is created and returned.
   *
   * @param deployName
   *        The plugin name is the folder that contains the deployment.
   *
   * @return Returns the classpath object if the plugin folder is present.
   * @throws LimbusException
   *         Thrown if the plugin is not deployed.
   */
  private Classpath findPlugin(String deployName) throws LimbusException {
    checkState();
    try {
      File pluginDirectory = getOrFailPluginDirectory(deployName);
      return Classpath.create(deployName)
          .addAllFilesInDirectory(pluginDirectory);
    } catch (Exception e) {
      throw new LimbusException("Cannot find plugin due to exception.", e);
    }
  }

  /**
   * @return Returns the list of known deploy names deployed by deploy service.
   */
  @Override
  public List<String> getDeployedComponents() {
    checkState();
    List<String> deployNames = new LinkedList<>();
    try {
      File workDirectory = getOrFailWorkDirectory();
      File[] listFiles = workDirectory.listFiles();
      for (File file : listFiles) {
        if (isAccessibleDirectory(file)) {
          deployNames.add(file.getName());
        }
      }
    } catch (Exception e) {
      log.error("Limbus deploy service cannot access the filesystem.", e);
    }
    return deployNames;

  }

  @Override
  public void classpathUndeployed(Classpath classpath) {
    if (classpath.hasDeployName()) {
      removePlugin(classpath.getDeployName());
    }
  }

}
