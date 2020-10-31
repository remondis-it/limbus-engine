package com.remondis.limbus.engine;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.api.LimbusClasspathException;
import com.remondis.limbus.engine.api.SharedClasspathProvider;
import com.remondis.limbus.files.FileAccessException;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.system.api.LimbusComponent;

/**
 * This is a {@link SharedClasspathProvider} constructing a shared classpath from the lib directory. All containing
 * files from the lib directory will be added to the shared classpath.
 *
 * @author schuettec
 *
 */
public class SharedFileSystemClasspath extends Initializable<Exception> implements SharedClasspathProvider {

  private static final Logger log = LoggerFactory.getLogger(SharedFileSystemClasspath.class);

  /**
   * This is the subfolder for the shared classpath
   */
  public static final String LIB_FOLDER = "lib";

  protected Classpath classpath;

  @LimbusComponent
  protected LimbusFileService filesystem;

  @Override
  public Classpath getSharedClasspath() {
    return classpath;
  }

  @Override
  public void checkClasspath() throws LimbusClasspathException {
    checkState();
    try {
      createSharedClassPathFolder();
      List<URL> classpathURLs = filesystem.getFolderFiles(LIB_FOLDER);
      // Create the shared classpath with a speaking name to give hints about the creator.
      this.classpath = Classpath.create("sharedClassPath_" + getClass().getName())
          .add(classpathURLs);
      LimbusUtil.logClasspath("shared", classpath, log);
    } catch (Exception e) {
      throw new LimbusClasspathException(String.format("Cannot create or access the shared classpath."), e);
    }
  }

  protected void createSharedClassPathFolder() throws FileAccessException {
    filesystem.createFolder(LIB_FOLDER, false);
  }

  @Override
  protected void performInitialize() throws Exception {

  }

  @Override
  protected void performFinish() {

  }

}
