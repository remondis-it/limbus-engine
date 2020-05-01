package com.remondis.limbus.staging;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.security.CodeSource;
import java.security.Permission;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.PropertyPermission;
import java.util.Set;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.exporter.ZipExporter;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.JavaArchive;

import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.staging.staging.Handler;
import com.remondis.limbus.utils.Lang;

public class LimbusStagingDeployment {

  private Classpath classpath;
  private Set<Permission> permissions;
  private JavaArchive deployment;
  private String deployName;
  private List<JavaArchive> dependencies;

  LimbusStagingDeployment(List<JavaArchive> dependencies) {
    super();
    this.dependencies = dependencies;
    this.permissions = new HashSet<>();
    this.deployment = ShrinkWrap.create(JavaArchive.class);
  }

  /**
   * Use this method to generate a dump of all files contained in the current deployment archive that is deployed to the
   * Limbus engine.
   *
   * @return Returns this object for method chainging.
   */
  public LimbusStagingDeployment dumpDeploymentContent() {
    System.out.println("Printing out the deployment archive content:");
    deployment.writeTo(System.out, Formatters.VERBOSE);
    System.out.println();
    return this;
  }

  void getDeploymentArchive() throws Exception {
    List<URL> urls = new LinkedList<>();
    List<JavaArchive> archives = new LinkedList<>(dependencies);
    for (JavaArchive a : archives) {
      addJavaArchiveAsURL(urls, a, null);
    }
    addJavaArchiveAsURL(urls, deployment, null);
  }

  private byte[] archiveToBytes(JavaArchive a) throws IOException {
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    a.as(ZipExporter.class)
        .exportTo(bout);
    byte[] byteArray = bout.toByteArray();
    bout.close();
    return byteArray;
  }

  private void addJavaArchiveAsURL(List<URL> urls, JavaArchive a, String archiveName)
      throws IOException, MalformedURLException {
    byte[] resource = archiveToBytes(a);
    String jarURLStr = null;
    if (Lang.isEmpty(archiveName)) {
      jarURLStr = createJarURL(a);
    } else {
      jarURLStr = createJarURL(archiveName);
    }
    URL jarURL = new URL(jarURLStr);// , resourceHandler);
    urls.add(jarURL);
    addResourceToStagingHandler(resource, jarURL);
  }

  /**
   * @return Returns the deployName of the resulting plugin deployment.
   */
  public String getDeployName() {
    return deployName;
  }

  /**
   * Adds the specified classes to the Limbus plugin deployment.
   *
   * @param classes
   *        The classes to add to the classpath.
   * @return Returns this {@link LimbusStaging} for method chainging.
   */
  public LimbusStagingDeployment andClasses(Class<?>... classes) {
    deployment.addClasses(classes);
    return this;
  }

  /**
   * Returns the classpath that is available after starting this {@link LimbusStage}.
   *
   * <p>
   * <b>Only available after starting the {@link LimbusStage} using {@link #startStage()}.
   * </p>
   *
   * @return Returns the classpath of the deployment.
   */
  public Classpath getClasspath() {
    return classpath;
  }

  /**
   * @return Returns the {@link Set} of default permissions.
   */
  public static Set<Permission> getDefaultPermissions() {
    Set<Permission> defaultPermissions = new HashSet<>();
    defaultPermissions.add(new PropertyPermission("*", "read"));
    defaultPermissions.add(new RuntimePermission("accessClassInPackage.sun.util.logging.resources"));
    return defaultPermissions;
  }

  private void addResourceToStagingHandler(byte[] resource, URL jarURL) {
    if (Handler.CURRENT_INSTANCE == null) {
      throw new IllegalStateException(
          "The staging resource URL handler is not available. Call LimbusStaging.prepareEnvironment() before using LimbusStage.");
    } else {
      Handler.CURRENT_INSTANCE.addResource(jarURL, resource);
    }
  }

  private String createJarURL(String archiveName) {
    return String.format("staging:/%s.jar", archiveName);
  }

  private String createJarURL(JavaArchive jar) {
    return String.format("staging:/%s", jar.getName());
  }

  Set<Permission> getPermissions() {
    return permissions;
  }

  JavaArchive getDeployment() {
    return deployment;
  }

  /**
   * Adds all resources delivered by the {@link CodeSource} of the specified classpath member.
   * <p>
   * Example: If the classpath
   * member is contained in the local project, the whole code source with all of its resources and classes is added to
   * the deployment.
   * </p>
   * <p>
   * <b>Note: Classes and resources with a test scope (or any other scope different) may be included into the deployment
   * if your
   * IDE or build system produces a common classpath for this scopes. Otherwise (and this normally should happen) only
   * the classes and resources of the project's compile dependencies are included into the deployment.</b>
   * </p>
   *
   * @param someClasspathMember
   *        Some class that is contained in the local project in compile scope.
   * @return Returns this object for method chaining.
   * @throws LimbusStagingException
   *         Thrown if the classpath could not be analyzed.
   */
  public LimbusStagingDeployment andProjectProviding(Class<?> someClasspathMember) throws LimbusStagingException {
    try {
      CodeSource codeSource = someClasspathMember.getProtectionDomain()
          .getCodeSource();
      URL location = codeSource.getLocation();
      if (location.getProtocol()
          .equals("file")) {
        File file = new File(location.toURI());
        boolean directory = file.isDirectory();
        if (directory) {
          File[] resources = file.listFiles();
          for (File resource : resources) {
            deployment.addAsResource(resource);
          }
        } else {
          throw new LimbusStagingException(
              "The code source of the specified classpath member returned a single file. The classpath is corrupt.");
        }
      }
      return this;
    } catch (Exception e) {
      throw new LimbusStagingException("Cannot enumerate project resources due to an exception.", e);
    }
  }

}
