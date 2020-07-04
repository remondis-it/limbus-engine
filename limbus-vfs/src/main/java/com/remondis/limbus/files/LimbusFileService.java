package com.remondis.limbus.files;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;

import com.remondis.limbus.api.IInitializable;

/**
 * The {@link LimbusFileService} is a file system abstraction service. It either simulates or performs read, write,
 * delete and browse operations on the filesystem. <b>The file service does not support direct access of absolute paths.
 * All paths will be relative to the current Limbus environment directory.</b>
 *
 * 
 *
 */
public interface LimbusFileService extends IInitializable<Exception> {

  /**
   * The name of the default logging directory.
   */
  public static final String LOGGING_DIRECTORY = "logs";

  /**
   * The name of the default Limbus configuration directory.
   */

  public static final String CONFIG_DIRECTORY = "conf";

  /**
   * Checks if the specified file exists.
   *
   * @param filePath
   *        The path identifying a file.
   * @return Returns <code>true</code> if the specified file exists, <code>false</code>
   *         otherwise.
   */
  public boolean hasFile(String filePath);

  /**
   * Returns the specified file represented by {@link URL}.
   *
   * @param filePath
   *        The path identifying a file.
   * @return Returns the {@link URL} to access the file if exists.
   * @throws FileNotFoundException
   *         Thrown if the file does not exist.
   * @throws FileAccessException
   *         Thrown if the file cannot be accessed.
   */
  public URL getFile(String filePath) throws FileNotFoundException, FileAccessException;

  /**
   * Returns the specified file represented by {@link InputStream}.
   *
   * @param filePath
   *        The path identifying a file.
   * @return Returns the {@link InputStream} of the file.
   * @throws FileNotFoundException
   *         Thrown if the file does not exist.
   * @throws FileAccessException
   *         Thrown if the file cannot be accessed.
   */
  public InputStream getFileAsStream(String filePath) throws FileNotFoundException, FileAccessException;

  /**
   * Returns the content of the file.
   *
   * @param filePath
   *        The path identifying a file.
   * @return Returns the content of the file represented by byte array.
   * @throws FileNotFoundException
   *         Thrown if the file does not exist.
   * @throws FileAccessException
   *         Thrown if the file cannot be accessed.
   */
  public byte[] getFileContent(String filePath) throws FileNotFoundException, FileAccessException;

  /**
   * Creates the specified file. If it exists already the file will be overridden. This method acts like
   * <tt>createFile(String, false)</tt>.
   *
   * @param filePath
   *        The path identifying the file to be created.
   * @return Returns the {@link OutputStream} of the created file.
   * @throws FileAccessException
   *         Thrown if the file could not be created.
   */
  public OutputStream createFile(String filePath) throws FileAccessException;

  /**
   * Creates the specified file. If it exists already the file will be overridden.
   *
   * @param filePath
   *        The path identifying the file to be created.
   * @param append
   *        If <code>true</code> the content written to the resulting output stream is appended to the file if the
   *        file already exists. On <code>false</code> the file will be recreated.
   * @return Returns the {@link OutputStream} of the created file.
   * @throws FileAccessException
   *         Thrown if the file could not be created.
   */
  public OutputStream createFile(String filePath, boolean append) throws FileAccessException;

  /**
   * Deletes the specified file.
   *
   * @param filePath
   *        The path identifying a file.
   * @throws FileNotFoundException
   *         Thrown if the file was not found
   * @throws FileAccessException
   *         Thrown if the file could not be accessed.
   */
  public void deleteFile(String filePath) throws FileNotFoundException, FileAccessException;

  /**
   * Checks if a folder exists.
   *
   * @param folderPath
   *        The path identifying a folder.
   * @return Returns <code>true</code> if the folder exists, <code>false</code>
   *         otherwise.
   */
  public boolean hasFolder(String folderPath);

  /**
   * Enumerates the files contained in the specified folder. <b>Note: Only files are returned.</b> To get the containing
   * folder names, use {@link #getFolderEntries(String)}.
   *
   * @param folderPath
   *        The path identifying a folder.
   * @return Returns the items of the folder.
   * @throws FileNotFoundException
   *         Thrown if the folder was not found.
   * @throws FileAccessException
   *         Thrown if the files could not be accessed.
   */
  public List<URL> getFolderFiles(String folderPath) throws FileNotFoundException, FileAccessException;

  /**
   * Enumerates the items contained in the specified folder.
   *
   * @param folderPath
   *        The path identifying a folder.
   * @return Returns the items of the folder.
   * @throws FileNotFoundException
   *         Thrown if the folder was not found.
   */
  public List<String> getFolderEntries(String folderPath) throws FileNotFoundException;

  /**
   * Concatenates the specified path segments using the system's file separator.
   *
   * @param pathSegments
   *        The path segments to concatenate.
   * @return Returns the resulting path.
   */
  public String toPath(String... pathSegments);

  /**
   * Checks if a folder contains elements.
   *
   * @param folderPath
   *        The path idenfiying a folder.
   * @return Returns <code>true</code> if the specified folder is empty, otherwise <code>false</code>
   *         is returned.
   * @throws FileNotFoundException
   *         Thrown if the folder does not exist.
   */
  public boolean isFolderEmpty(String folderPath) throws FileNotFoundException;

  /**
   * Creates the folder identified by the specified path. If the folder already exists this method does nothing.
   *
   * @param folderPath
   *        The path identifying a folder.
   * @param createSubsequent
   *        If <code>true</code> all folders within the path thad does not exist will be created. If
   *        <code>false</code> only the folder identified by the path will be created, which only succeeds if the
   *        parent folders are already peresent.
   * @throws FileAccessException
   *         Thrown if the folder could not be created.
   */
  public void createFolder(String folderPath, boolean createSubsequent) throws FileAccessException;

  /**
   * Deletes a folder identified by the specified path. If the folder is not empty it will be deleted recursively.
   *
   * @param folderPath
   *        The path identifying a folder.
   * @throws FileNotFoundException
   *         Thrown if the folder was not found
   * @throws FileAccessException
   *         Thrown if the folder could not be accessed.
   */
  public void deleteFolder(String folderPath) throws FileNotFoundException, FileAccessException;

  /**
   * Renames a file.
   *
   * @param filePath
   *        The old file. Specify the full path here.
   * @param newFilename
   *        The new filename. Only specify the new filename here.
   * @throws FileAccessException
   *         Thrown if the folder or file could not be accessed.
   */
  public void renameFile(String filePath, String newFilename) throws FileAccessException;

}
