package org.max5.limbus.utils;

import java.io.File;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This is a util class providing util methods to access the filesystem. This class can be used as an independent
 * utility but also contains file system conventions that were specified for the Limbus environment.
 *
 *
 * @author schuettec
 *
 */
public class Files {

  /**
   * This field is public and non-final to set it to another path for tests.
   */
  public static File CURRENT_DIRECTORY = Paths.get(".")
      .toAbsolutePath()
      .normalize()
      .toFile();

  /**
   * Name of the configuration directory of a Limbus Engine.
   */
  public static final String CONFIGURATION_DIRECTORY = "conf";

  /**
   * Name of the logging directory of a Limbus Engine.
   */
  public static final String LOGGING_DIRECTORY = "log";

  private static final String DAILY_FILE_WITH_EXTENSION = "%s_%s.%s";
  private static final String DAILY_FILE_WITHOUT_EXTENSION = "%s_%s";

  private Files() {
  }

  /**
   * Checks if the specified file is a file and if it is accessible (existing and readable).
   *
   * @param toCheck
   *        The file to check
   * @return Returns <code>true</code> if the file is an existing regular file and readable.
   */
  public static boolean isAccessibleFile(File toCheck) {
    Lang.denyNull("file", toCheck);
    return toCheck.isFile() && isAccessible(toCheck);
  }

  /**
   * Checks if the specified file is a folder and if it is accessible (existing and readable).
   *
   * @param toCheck
   *        The file to check
   * @return Returns <code>true</code> if the file is an existing directory and readable.
   */
  public static boolean isAccessibleDirectory(File toCheck) {
    Lang.denyNull("file", toCheck);
    return toCheck.isDirectory() && isAccessible(toCheck);
  }

  private static boolean isAccessible(File toCheck) {
    Lang.denyNull("file", toCheck);
    return toCheck.exists() && toCheck.canRead();
  }

  /**
   * @return Returns the Limbus configuration directory without accessibility checks.
   */
  public static File getConfigurationDirectoryUnchecked() {
    return new File(getCurrentDirectory(), CONFIGURATION_DIRECTORY);
  }

  /**
   * @return Returns the Limbus configuration directory or creates it if it does not exist. Accessibility checks are
   *         performed!
   * @throws Exception
   *         Thrown if the directory cannot be created or is not accessible.
   */
  public static File getOrFailConfigurationDirectory() throws Exception {
    File directory = getConfigurationDirectoryUnchecked();
    return getOrFailDirectory("configuration", directory);
  }

  /**
   * @deprecated This method is deprecated because it is used by components that are likely to be part of an integration
   *             test. Therefore modifying the file system should be avoided. <b>Instead
   *             {@link #getConfigurationDirectoryUnchecked()} and an appropriate fallback should be used.</b>
   * @return Returns the Limbus configuration directory or creates it if it does not exist. Accessibility checks are
   *         performed!
   * @throws Exception
   *         Thrown if the directory cannot be created or is not accessible.
   */
  @Deprecated
  protected static File getCreateOrFailConfigurationDirectory() throws Exception {
    File directory = getConfigurationDirectoryUnchecked();
    createIfMissingDirectory(directory);
    return getOrFailDirectory("configuration", directory);
  }

  /**
   * Validates a path to a folder and returns its representation as {@link File} if the folder is accessible.
   *
   * @param folderType
   *        The folder type name used for exception messages.
   * @param uncheckedFolder
   *        The path to the folder.
   * @return Returns the path as {@link File} if it exists and is accessible.
   * @throws Exception
   *         Thrown if the folder does not exist or is not accessible.
   */
  public static File getOrFailDirectory(String folderType, String uncheckedFolder) throws Exception {
    if (uncheckedFolder == null) {
      throw new Exception(String.format("The value for the %s directory was null.", folderType));
    }
    return getOrFailDirectory(folderType, new File(uncheckedFolder));
  }

  /**
   * Validates a path to a file and returns its representation as {@link File} if the file is accessible.
   *
   * @param fileType
   *        The file type name used for exception messages.
   * @param uncheckedFile
   *        The path to the file.
   * @return Returns the path as {@link File} if it exists and is accessible.
   * @throws Exception
   *         Thrown if the file does not exist or is not accessible.
   */
  public static File getOrFailFile(String fileType, String uncheckedFile) throws Exception {
    if (uncheckedFile == null) {
      throw new Exception(String.format("The value for the %s file was null.", fileType));
    }
    return getOrFailFile(fileType, new File(uncheckedFile));
  }

  /**
   * Validates a file and returns its representation as {@link File} if the file is accessible.
   *
   * @param fileType
   *        The folder type name used for exception messages.
   * @param uncheckedFile
   *        The path to the folder.
   * @return Returns the file {@link File} if it exists and is accessible.
   * @throws Exception
   *         Thrown if the file does not exist or is not accessible.
   */
  public static File getOrFailFile(String fileType, File uncheckedFile) throws Exception {
    Lang.denyNull("fileType", fileType);
    Lang.denyNull("uncheckedFile", uncheckedFile);

    if (uncheckedFile.exists()) {
      if (uncheckedFile.isFile()) {
        if (uncheckedFile.canRead()) {
          return uncheckedFile;
        } else {
          throw new Exception(
              String.format("Cannot access the %s file: %s", fileType, uncheckedFile.getAbsolutePath()));

        }
      } else {
        throw new Exception(
            String.format("The %s file is not a valid file: %s", fileType, uncheckedFile.getAbsolutePath()));
      }
    } else {
      throw new Exception(String.format("The %s file does not exist: %s", fileType, uncheckedFile.getAbsolutePath()));
    }
  }

  /**
   * Validates a folder and returns its representation as {@link File} if the folder is accessible.
   *
   * @param folderType
   *        The folder type name used for exception messages.
   * @param uncheckedFolder
   *        The the folder.
   * @return Returns the path as {@link File} if it exists and is accessible.
   * @throws Exception
   *         Thrown if the folder does not exist or is not accessible.
   */
  public static File getOrFailDirectory(String folderType, File uncheckedFolder) throws Exception {
    Lang.denyNull("folderType", folderType);
    Lang.denyNull("uncheckedFolder", uncheckedFolder);

    if (uncheckedFolder.exists()) {
      if (uncheckedFolder.isDirectory()) {
        if (uncheckedFolder.canRead()) {
          return uncheckedFolder;
        } else {
          throw new Exception(
              String.format("Cannot access the %s directory: %s", folderType, uncheckedFolder.getAbsolutePath()));

        }
      } else {
        throw new Exception(String.format("The %s directory is not a valid directory: %s", folderType,
            uncheckedFolder.getAbsolutePath()));
      }
    } else {
      throw new Exception(
          String.format("The %s directory does not exist: %s", folderType, uncheckedFolder.getAbsolutePath()));
    }
  }

  /**
   * Creates a directory if missing. If the directory is a path, this method tries to create all folders within this
   * path, if they do not exist.
   *
   * @param directory
   *        The directory to create.
   * @throws Exception
   *         Thrown on any error.
   */
  public static void createIfMissingDirectory(File directory) throws Exception {
    Lang.denyNull("Directory", directory);

    if (directory.isFile()) {
      throw new Exception(String.format("Cannot access a class path - is this an accessible directory? %s",
          directory.getAbsolutePath()));
    }

    if (!directory.isDirectory() || !directory.canRead()) {
      try {
        directory.mkdirs();
      } catch (Exception e) {
        throw new Exception(String.format("Cannot create classpath directory - is this an accessible directory? %s",
            directory.getAbsolutePath()));
      }
    }
  }

  public static File getCurrentDirectory() {
    return CURRENT_DIRECTORY;
  }

  /**
   * Creates a filename with leading date with format 'yyyy_MM_dd_filename.extension'. This can be used for daily
   * logging files for example.
   *
   * @param filename
   *        The base filename
   * @param extension
   *        (Optionally) The extension, may be null if no extension should be used.
   * @return Returns the file name.
   */
  public static String getDailyFile(String filename, String extension) {
    SimpleDateFormat format = new SimpleDateFormat("yyyy_MM_dd");
    String today = format.format(new Date());
    String fileName = null;
    if (extension == null || extension.isEmpty()) {
      fileName = String.format(DAILY_FILE_WITHOUT_EXTENSION, today, filename);
    } else {
      fileName = String.format(DAILY_FILE_WITH_EXTENSION, today, filename, extension);
    }
    return fileName;
  }

}
