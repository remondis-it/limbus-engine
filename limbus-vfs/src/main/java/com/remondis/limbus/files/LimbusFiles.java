package com.remondis.limbus.files;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.remondis.limbus.utils.Lang;

/**
 * This is a util class providing util methods to access the filesystem.
 *
 * 
 *
 */
public class LimbusFiles {

  private static final String DAILY_FILE_WITH_EXTENSION = "%s_%s.%s";
  private static final String DAILY_FILE_WITHOUT_EXTENSION = "%s_%s";

  private LimbusFiles() {
  }

  /**
   * Checks if the specified file is a file and if it is accessible (existing and readable).
   *
   * @param toCheck
   *        The file to check
   * @return Returns <code>true</code> if the file is an existing regular file and readable.
   */
  protected static boolean isAccessibleFile(File toCheck) {
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
  protected static boolean isAccessibleFolder(File toCheck) {
    Lang.denyNull("file", toCheck);
    return toCheck.isDirectory() && isAccessible(toCheck);
  }

  protected static boolean isAccessible(File toCheck) {
    Lang.denyNull("file", toCheck);
    return toCheck.exists() && toCheck.canRead();
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
  protected static void createIfMissingDirectory(File directory) throws Exception {
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
