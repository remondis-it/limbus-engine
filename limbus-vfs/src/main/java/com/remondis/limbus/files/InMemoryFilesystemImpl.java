package com.remondis.limbus.files;

import static com.remondis.limbus.utils.Lang.closeQuietly;
import static com.remondis.limbus.utils.Lang.denyNull;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import com.remondis.limbus.api.Initializable;
import com.remondis.limbus.files.vfs.Handler;
import com.remondis.limbus.utils.Lang;

/**
 * This is an in-memory implementation of a file system. <b>Do not use the in memory representation for huge amount of
 * files.</b>
 *
 * @author schuettec
 *
 */
public class InMemoryFilesystemImpl extends Initializable<Exception> implements LimbusFileService {

  public static final String LIMBUS_MEMORY_PROTOCOL = "vfs";

  private Map<String, byte[]> filesystem;

  public InMemoryFilesystemImpl() {
    filesystem = Collections.synchronizedMap(new HashMap<String, byte[]>());
  }

  @Override
  protected void performInitialize() throws Exception {
    // schuettec - 02.03.2017 : We have no other chance to hold this instance of filesystem than putting it in a static
    // context. If theres is another solution that works better file an issue.
    Handler.setMemoryFilesystem(this);
    installURLStreamHandler();

  }

  @Override
  protected void performFinish() {
  }

  /**
   * This method installs the stream handler to make streamed resources available through URLs in this JVM.
   */
  public void installURLStreamHandler() {

    String property = System.getProperty("java.protocol.handler.pkgs");
    if (Lang.isEmpty(property)) {
      property = handlerProperty();
    } else {
      property = property + "|" + handlerProperty();
    }
    System.setProperty("java.protocol.handler.pkgs", property);
  }

  /**
   * This method deinstalls the stream handler from this JVM.
   */
  public void deinstallURLStreamHandler() {
    String property = System.getProperty("java.protocol.handler.pkgs");
    if (property != null) {
      if (property.contains(handlerProperty())) {
        property.replaceAll(handlerProperty(), "");
      }
      if (property.contains("||")) {
        property.replaceAll("||", "|");
      }
    }
    System.setProperty("java.protocol.handler.pkgs", property);

  }

  private String handlerProperty() {
    return InMemoryFilesystemImpl.class.getPackage()
        .getName();
  }

  protected boolean hasPath(String path) {
    return filesystem.containsKey(path);
  }

  protected boolean isFile(String path) {
    if (hasPath(path)) {
      byte[] content = filesystem.get(path);
      if (content == null) {
        return false;
      } else {
        return true;
      }
    } else {
      throw notFound(path);
    }
  }

  protected boolean isFolder(String path) {
    if (hasPath(path)) {
      byte[] content = filesystem.get(path);
      if (content == null) {
        return true;
      } else {
        return false;
      }
    } else {
      throw notFound(path);
    }
  }

  protected void denyNotAFile(String filePath) {
    filePath = normalizeAndValidate(filePath);
    if (hasPath(filePath)) {
      if (!isFile(filePath)) {
        throw notAFile(filePath);
      }
    } else {
      throw notFound(filePath);
    }
  }

  protected void denyNotAFolder(String folderPath) {
    folderPath = normalizeAndValidate(folderPath);
    if (hasPath(folderPath)) {
      if (!isFolder(folderPath)) {
        throw notAFolder(folderPath);
      }
    } else {
      throw notFound(folderPath);
    }
  }

  private FileNotFoundException notAFolder(String folderPath) {
    return new FileNotFoundException(String.format("The file %s is not a folder.", folderPath));
  }

  private FileNotFoundException notAFile(String filePath) {
    return new FileNotFoundException(String.format("The file %s is not a file.", filePath));
  }

  private FileNotFoundException notFound(String path) {
    return new FileNotFoundException(String.format("The file %s could not be found.", path));
  }

  private FileAccessException urlFailure(String filePath, Exception e) {
    return new FileAccessException(String.format("Cannot create URL for file %s", filePath), e);
  }

  @Override
  public boolean hasFile(String filePath) {
    filePath = normalize(filePath);
    return isAllowedPath(filePath) && hasPath(filePath) && isFile(filePath);
  }

  @Override
  public URL getFile(String filePath) throws FileNotFoundException, FileAccessException {
    filePath = normalizeAndValidate(filePath);
    denyNotAFile(filePath);
    try {
      return new URL(LIMBUS_MEMORY_PROTOCOL + ":" + filePath);
    } catch (MalformedURLException e) {
      throw urlFailure(filePath, e);
    }
  }

  @Override
  public InputStream getFileAsStream(String filePath) throws FileNotFoundException, FileAccessException {
    filePath = normalizeAndValidate(filePath);
    denyNotAFile(filePath);
    byte[] fileContent = filesystem.get(filePath);
    return new ByteArrayInputStream(fileContent);
  }

  @Override
  public byte[] getFileContent(String filePath) throws FileNotFoundException, FileAccessException {
    filePath = normalizeAndValidate(filePath);
    denyNotAFile(filePath);
    return filesystem.get(filePath);
  }

  @Override
  public OutputStream createFile(String filePath) throws FileAccessException {
    return createFile(filePath, false);
  }

  @Override
  public void deleteFile(String filePath) throws FileNotFoundException, FileAccessException {
    filePath = normalizeAndValidate(filePath);
    denyNotAFile(filePath);
    filesystem.remove(filePath);
  }

  @Override
  public boolean hasFolder(String folderPath) {
    folderPath = normalize(folderPath);
    return isAllowedPath(folderPath) && hasPath(folderPath) && isFolder(folderPath);
  }

  @Override
  public List<URL> getFolderFiles(String folderPath) throws FileNotFoundException, FileAccessException {
    folderPath = normalizeAndValidate(folderPath);
    denyNotAFolder(folderPath);
    Iterator<String> it = filesystem.keySet()
        .iterator();
    List<URL> urls = new LinkedList<>();
    while (it.hasNext()) {
      String filePath = it.next();
      if (isFileIn(folderPath, filePath)) {
        URL url = getFile(filePath);
        urls.add(url);
      }
    }
    return urls;
  }

  private boolean isFileIn(String folderPath, String filePath) {
    if (filePath.startsWith(folderPath)) {
      String leftover = filePath.replace(folderPath + File.separator, "");
      if (leftover.contains(File.separator)) {
        return false;
      } else {
        return hasFile(filePath) || hasFolder(filePath);
      }
    } else {
      return false;
    }
  }

  @Override
  public List<String> getFolderEntries(String folderPath) throws FileNotFoundException {
    folderPath = normalizeAndValidate(folderPath);
    denyNotAFolder(folderPath);
    Iterator<String> it = filesystem.keySet()
        .iterator();
    List<String> names = new LinkedList<>();
    while (it.hasNext()) {
      String filePath = it.next();
      if (isFileIn(folderPath, filePath)) {
        String name = filePath.replace(folderPath + File.separator, "");
        names.add(name);
      }
    }
    return names;
  }

  private String normalizeAndValidate(String path) {
    String n = normalize(path);
    denyIllegalPath(n);
    return n;
  }

  private String normalize(String path) throws FileNotFoundException {
    String normalized = path;
    // all path's get absolute and start with /
    if (!path.startsWith(File.separator)) {
      normalized = File.separator + normalized;
    }
    // paths does not end with /
    if (path.endsWith(File.separator)) {
      normalized.substring(normalized.length() - 1);
    }
    return normalized;
  }

  private void denyIllegalPath(String path) throws FileNotFoundException {
    if (!isAllowedPath(path)) {
      throw new FileNotFoundException("Illegal path: ./ ../ or ~ are not allowed in a path.");
    }
  }

  private boolean isAllowedPath(String path) {
    return !path.contains("./") && !path.contains("../") && !path.contains("~");
  }

  @Override
  public String toPath(String... pathSegments) {
    return Commons.toPath(pathSegments);
  }

  @Override
  public boolean isFolderEmpty(String folderPath) throws FileNotFoundException {
    folderPath = normalizeAndValidate(folderPath);
    denyNotAFolder(folderPath);
    return getFolderEntries(folderPath).isEmpty();
  }

  @Override
  public void createFolder(String folderPath, boolean createSubsequent) throws FileAccessException {
    Lang.denyNull("folderPath", folderPath);
    folderPath = normalizeAndValidate(folderPath);
    if (hasFile(folderPath)) {
      throw new FileAccessException("The specified folder path is a file!");
    }

    String[] folders = folderPath.substring(1)
        .split(Pattern.quote(File.separator));
    if (createSubsequent) {
      String path = null;
      for (String subFolder : folders) {
        if (path == null) {
          path = subFolder;
        } else {
          path = path + File.separator + subFolder;
        }
        filesystem.put(normalize(path), null);
      }
    } else {
      denyHasNotAllParentPaths(folders);
      filesystem.put(normalize(folderPath), null);
    }
  }

  private void denyHasNotAllParentPaths(String[] folders) {
    String path = null;
    for (int i = 0; i < folders.length - 1; i++) {
      String subFolder = folders[i];
      if (path == null) {
        path = subFolder;
      } else {
        path = path + File.separator + subFolder;
      }
      if (!hasFolder(path)) {
        throw new FileNotFoundException(String.format("Folder not found: %s", path));
      }
    }
  }

  @Override
  public void deleteFolder(String folderPath) throws FileNotFoundException, FileAccessException {
    folderPath = normalizeAndValidate(folderPath);
    if (hasFolder(folderPath)) {
      Iterator<String> it = new HashSet<>(filesystem.keySet()).iterator();
      while (it.hasNext()) {
        String filePath = it.next();
        if (filePath.startsWith(folderPath)) {
          filesystem.remove(filePath);
        }
      }
    } else {
      throw notAFile(folderPath);
    }
  }

  /**
   * Adds new content to this in-memory file system. If the content already exists it will be replaced.
   *
   * @param filePath
   *        The file path.
   * @param fileContent
   *        The content.
   */
  public void addContent(String filePath, byte[] fileContent) {
    Lang.denyNull("filePath", filePath);
    Lang.denyNull("fileContent", fileContent);
    filePath = normalizeAndValidate(filePath);
    if (hasFolder(filePath)) {
      throw notAFile(filePath);
    } else {
      filesystem.put(filePath, fileContent);
    }
  }

  /**
   * Adds a classpath resources to this virtual file system. If the content already exists it will be replaced.
   *
   * @param caller
   *        The caller of this method. Used to load the classpath resource.
   * @param targetFolder
   *        The target folder in the virtual file system.
   * @param resource
   *        The classpath resource name. <b>Use the leading '/' to load from the classpath's root.</b>
   * @throws IOException
   *         Thrown on any error.
   *
   */
  public void addClasspathContent(Class<?> caller, String targetFolder, String resource) throws IOException {
    InputStream input = caller.getResourceAsStream(resource);
    Lang.denyNull("input", input);
    byte[] content = Lang.toByteArray(input);
    // Cut leading / away on demand
    if (resource.startsWith("/")) {
      resource = resource.substring(1, resource.length());
    }
    addContent(targetFolder + File.separator + resource, content);
  }

  @Override
  public String toString() {
    StringBuilder b = new StringBuilder("Filesystem @ memory - Contents:\n");
    Iterator<String> it = filesystem.keySet()
        .iterator();
    while (it.hasNext()) {
      String bytes = "";
      String next = it.next();
      if (isFile(next)) {
        int length = filesystem.get(next).length;
        bytes = length + " bytes";
      }
      b.append(String.format("%-15s (%s) %s\n", bytes, isFile(next) ? "(File)" : "(Folder)", next));
    }
    return b.toString();
  }

  @Override
  public OutputStream createFile(String filePath, boolean append) throws FileAccessException {
    filePath = normalizeAndValidate(filePath);
    TransactionalOutputStream outputStream = new TransactionalOutputStream(filePath, this);
    if (append && hasFile(filePath)) {
      // Fill the output stream with the existent data
      byte[] fileContent = getFileContent(filePath);
      try {
        outputStream.write(fileContent);
      } catch (IOException e) {
        throw new FileAccessException(String.format("Could not append to the existing file %s.", filePath));
      }
    }
    return outputStream;
  }

  @Override
  public void renameFile(String filePath, String newFilename) throws FileAccessException {
    denyNull("filePath", filePath);
    denyNull("newFilename", newFilename);
    if (newFilename.contains(File.separator)) {
      throw new FileAccessException("Cannot move a file to another folder location.");
    }
    filePath = normalizeAndValidate(filePath);
    denyNotAFile(filePath);
    String[] parts = filePath.split(Pattern.quote(File.separator));
    // schuettec - 31.03.2017 : The last segment is the filename
    parts[parts.length - 1] = newFilename;
    String newFilePath = toPath(parts);
    // schuettec - 31.03.2017 : Perform the "move"
    byte[] file = getFileContent(filePath);
    deleteFile(filePath);
    addContent(newFilePath, file);
  }

  @Override
  public void touchFile(String filePath) throws FileAccessException {
    try (OutputStream output = createFile(filePath)) {
      closeQuietly(output);
    } catch (IOException e) {
      throw new FileAccessException("Could not create file: " + filePath, e);
    }
  }
}
