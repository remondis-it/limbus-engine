package org.max5.limbus.files;

import static org.max5.limbus.files.LimbusFiles.isAccessibleFile;
import static org.max5.limbus.files.LimbusFiles.isAccessibleFolder;
import static org.max5.limbus.utils.Lang.denyNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.max5.limbus.Initializable;
import org.max5.limbus.utils.Lang;

/**
 * This is the Limbus file system implementation. It delegates all service methods to the real file system.
 * This implementation of the file system will redirect any relative paths using the current directory of this running
 * application. The service is intended for accessing the current Limbus environment.
 * <b>Therefore all file access is assumed to happen relative to the current directory.</b>
 *
 * @author schuettec
 *
 */
public class FileSystemServiceImpl extends Initializable<Exception> implements LimbusFileService {

  /**
   * Hold the current directory.
   */
  private File currentDirectory = null;

  public FileSystemServiceImpl() {
  }

  @Override
  protected void performInitialize() throws Exception {
    this.currentDirectory = Paths.get(".")
        .toAbsolutePath()
        .normalize()
        .toFile();
    createLimbusFolderOnDemand();
  }

  private void createLimbusFolderOnDemand() {

  }

  @Override
  protected void performFinish() {
  }

  @Override
  public boolean hasFile(String filePath) {
    checkState();
    denyNull("filePath", filePath);
    File file = getUnchecked(filePath);
    return isAccessibleFile(file);
  }

  /**
   * Returns the absolute file unchecked.
   *
   * @param filePath
   *        The relative file path.
   * @return Returns the filepath relative to the current directory.
   */
  private File getUnchecked(String filePath) {
    denyNull("filePath", filePath);
    return new File(currentDirectory, filePath);
  }

  @Override
  public URL getFile(String filePath) throws FileNotFoundException, FileAccessException {
    checkState();
    denyNull("filePath", filePath);
    File file = denyInAccessibleFile(filePath);
    try {
      return file.toURI()
          .toURL();
    } catch (MalformedURLException e) {
      throw new FileAccessException(file, e);
    }
  }

  @Override
  public InputStream getFileAsStream(String filePath) throws FileNotFoundException, FileAccessException {
    checkState();
    denyNull("filePath", filePath);
    File file = denyInAccessibleFile(filePath);
    try {
      return new FileInputStream(file);
    } catch (java.io.FileNotFoundException e) {
      throw new FileAccessException(file, e);
    }
  }

  @Override
  public byte[] getFileContent(String filePath) throws FileNotFoundException, FileAccessException {
    checkState();
    denyNull("filePath", filePath);
    try {
      return Lang.toByteArray(getFileAsStream(filePath));
    } catch (IOException e) {
      throw new FileAccessException(getUnchecked(filePath), e);
    }
  }

  @Override
  public OutputStream createFile(String filePath) throws FileAccessException {
    checkState();
    return createFile(filePath, false);
  }

  @Override
  public void deleteFile(String filePath) throws FileNotFoundException, FileAccessException {
    checkState();
    denyNull("filePath", filePath);
    File file = denyInAccessibleFile(filePath);
    boolean success = file.delete();
    if (!success) {
      throw new FileAccessException(String.format("Cannot delete the file %s", file.getAbsolutePath()));
    }
  }

  @Override
  public boolean hasFolder(String folderPath) {
    checkState();
    denyNull("folderPath", folderPath);
    File file = getUnchecked(folderPath);
    return isAccessibleFolder(file);
  }

  @Override
  public List<URL> getFolderFiles(String folderPath) throws FileNotFoundException, FileAccessException {
    checkState();
    denyNull("folderPath", folderPath);
    File file = denyNotAFolder(folderPath);
    File[] listFiles = file.listFiles();
    List<URL> urls = new LinkedList<URL>();
    for (File f : listFiles) {
      try {
        urls.add(f.toURI()
            .toURL());
      } catch (MalformedURLException e) {
        throw new FileAccessException(f, e);
      }
    }
    return urls;
  }

  @Override
  public List<String> getFolderEntries(String folderPath) throws FileNotFoundException {
    checkState();
    denyNull("folderPath", folderPath);
    File file = denyNotAFolder(folderPath);
    File[] listFiles = file.listFiles();
    List<String> names = new LinkedList<String>();
    for (File f : listFiles) {
      names.add(f.getName());
    }
    return names;
  }

  @Override
  public String toPath(String... pathSegments) {
    checkState();
    return Commons.toPath(pathSegments);
  }

  @Override
  public boolean isFolderEmpty(String folderPath) throws FileNotFoundException {
    checkState();
    denyNull("folderPath", folderPath);

    File folder = denyNotAFolder(folderPath);
    return folder.listFiles().length == 0;
  }

  @Override
  public void createFolder(String folderPath, boolean createSubsequent) throws FileAccessException {
    checkState();
    denyNull("folderPath", folderPath);

    File folder = getUnchecked(folderPath);
    if (isAccessibleFolder(folder)) {
      // The folder already exists.
      return;
    } else {
      boolean success = false;
      if (createSubsequent) {
        success = folder.mkdirs();
      } else {
        success = folder.mkdir();
      }
      if (!success) {
        throw new FileAccessException(String.format("Cannot create folder(s) in path %s.", folder.getAbsolutePath()));
      }
    }
  }

  @Override
  public void deleteFolder(String folderPath) throws FileNotFoundException, FileAccessException {
    checkState();
    denyNull("folderPath", folderPath);

    File folder = denyNotAFolder(folderPath);
    try {
      FileUtils.deleteDirectory(folder);
    } catch (IOException e) {
      throw new FileAccessException(folder, e);
    }
  }

  private File denyNotAFile(String filePath) throws FileNotFoundException {
    File file = getUnchecked(filePath);
    boolean accessible = isAccessibleFile(file);
    if (accessible) {
      return file;
    } else {
      throw new FileNotFoundException(String.format("The path %s is not a file.", file.getAbsolutePath()));
    }
  }

  private File denyNotAFolder(String folderPath) throws FileNotFoundException {
    File file = getUnchecked(folderPath);
    boolean accessible = isAccessibleFolder(file);
    if (accessible) {
      return file;
    } else {
      throw new FileNotFoundException(String.format("The path %s is not a folder.", file.getAbsolutePath()));
    }
  }

  private File denyInAccessibleFile(String filePath) throws FileAccessException {
    denyNotAFile(filePath);
    File file = getUnchecked(filePath);
    boolean accessible = isAccessibleFile(file);
    if (accessible) {
      return file;
    } else {
      throw new FileAccessException(String.format("The file %s is not accessible.", file.getAbsolutePath()));
    }
  }

  @Override
  public OutputStream createFile(String filePath, boolean append) throws FileAccessException {
    checkState();
    denyNull("filePath", filePath);
    File file = getUnchecked(filePath);
    try {
      return new FileOutputStream(file, append);
    } catch (java.io.FileNotFoundException e) {
      throw new FileAccessException(file, e);
    }
  }

  @Override
  public void renameFile(String filePath, String newFilename) throws FileAccessException {
    checkState();
    denyNull("filePath", filePath);
    denyNull("newFilename", newFilename);
    File file = denyInAccessibleFile(filePath);
    boolean success = file.renameTo(new File(file.getParent(), newFilename));
    if (!success) {
      throw new FileAccessException("Renaming the file was not successful. No cause reported.");
    }
  }

}
