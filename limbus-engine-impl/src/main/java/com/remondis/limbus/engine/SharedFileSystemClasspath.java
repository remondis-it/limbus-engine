package com.remondis.limbus.engine;

import java.net.URL;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.remondis.limbus.api.Classpath;
import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.api.LimbusClasspathException;
import com.remondis.limbus.engine.api.SharedClasspathProvider;
import com.remondis.limbus.files.LimbusFileService;
import com.remondis.limbus.system.api.LimbusComponent;

/**
 * This is a {@link SharedClasspathProvider} constructing a shared classpath from the lib directory. All containing
 * files from the lib directory will be added to the shared classpath.
 *
 * 
 *
 */
public class SharedFileSystemClasspath extends Initializable<Exception> implements SharedClasspathProvider {

  private static final Logger log = LoggerFactory.getLogger(SharedFileSystemClasspath.class);

  /**
   * This is the subfolder for the shared classpath
   */
  public static final String LIB_FOLDER = "lib";

  private Classpath classpath;

  @LimbusComponent
  private LimbusFileService filesystem;

  @Override
  public Classpath getSharedClasspath() {
    return classpath;
  }

  @Override
  public void checkClasspath() throws LimbusClasspathException {
    try {
      filesystem.createFolder(LIB_FOLDER, false);
      List<URL> classpathURLs = filesystem.getFolderFiles(LIB_FOLDER);
      // Create the shared classpath with a speaking name to give hints about the creator.
      this.classpath = Classpath.create("sharedClassPath_" + getClass().getName())
          .add(classpathURLs);
      LimbusUtil.logClasspath("shared", classpath, log);
    } catch (Exception e) {
      throw new LimbusClasspathException(String.format("Cannot create or access the shared classpath."), e);
    }
  }

  @Override
  protected void performInitialize() throws Exception {

  }

  @Override
  protected void performFinish() {

  }

}
