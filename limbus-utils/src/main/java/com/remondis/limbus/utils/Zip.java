/**
 *
 */
package com.remondis.limbus.utils;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

/**
 * This class contains useful methods for processing zip files.
 *
 * @author schuettec
 *
 */
public class Zip {

  /**
   * Holds the hash filename pattern.
   */
  protected static final String HASH_FILENAME_PATTERN = "%s.sha1";

  /**
   * Unpacks a ZIP file to the specified folder. Check the specified file objects before calling this method.
   *
   * <p>
   * If an exception occurs while unpacking, created files within the output folder are not deleted by this method.
   * </p>
   *
   * @param zipFile
   *        input zip file (check the file before calling)
   * @param outputFolder
   *        zip file output folder (check the file before calling)
   * @throws Exception
   *         Thrown on any error.
   */
  public static void unpack(File zipFile, File outputFolder) throws Exception {

    byte[] buffer = new byte[1024];

    if (!outputFolder.isDirectory()) {
      throw new Exception("The output folder is not a directory.");
    }
    if (!zipFile.isFile()) {
      throw new Exception("The input file is not a regular file.");
    }

    // get the zip file content
    ZipInputStream zis = null;
    try {
      zis = new ZipInputStream(new FileInputStream(zipFile));

      // get the zipped file list entry
      ZipEntry ze = zis.getNextEntry();

      while (ze != null) {

        String fileName = ze.getName();
        File newFile = new File(outputFolder, fileName);

        // create all non exists folders
        // else you will hit FileNotFoundException for compressed folder
        new File(newFile.getParent()).mkdirs();

        if (!ze.isDirectory()) {
          FileOutputStream fos = null;
          try {
            fos = new FileOutputStream(newFile);
            int len;
            while ((len = zis.read(buffer)) > 0) {
              fos.write(buffer, 0, len);
            }
          } catch (Exception e) {
            throw e;
          } finally {
            Lang.closeQuietly(fos);
            zis.closeEntry();
          }
        }
        ze = zis.getNextEntry();
      }

    } catch (Exception e) {
      throw new Exception(String.format("Cannot unpack file %s ", zipFile.getAbsolutePath()), e);
    } finally {
      Lang.closeQuietly(zis);
    }
  }

  /**
   * This method checks if a file content is a ZIP file. This method tries to read the first ZIP entry. If this succeeds
   * it is assumed that the specified file content represents a valid ZIP.
   *
   * @param file
   *        The file to check.
   * @return Returns <code>true</code> if the specified file is a ZIP file. Otherwise <code>false</code> is returned.
   */
  public static boolean isZipFile(byte[] file) {
    if (file == null) {
      return false;
    }
    try {
      return new ZipInputStream(new ByteArrayInputStream(file)).getNextEntry() != null;
    } catch (IOException e) {
      return false;
    }
  }

  /**
   * Generates a zip of hashes. The hashes are generated for every file item in the zip. The naming pattern for the hash
   * files is
   * {@value #HASH_FILENAME_PATTERN}.
   *
   * @param zipFile
   *        The zip file.
   * @return Returns a new zip file containing only the hashes of the files in the original zip.
   * @throws Exception
   *         On any error.
   */
  public static byte[] hashZipItems(byte[] zipFile) throws Exception {
    ZipInputStream zin = new ZipInputStream(new ByteArrayInputStream(zipFile));
    ByteArrayOutputStream bout = new ByteArrayOutputStream();
    ZipOutputStream zout = new ZipOutputStream(bout);

    try {
      ZipEntry entry = null;
      while ((entry = zin.getNextEntry()) != null) {
        // Get the file data
        byte[] data = Lang.toByteArray(zin);
        byte[] hash = Hash.toSHA(data);

        // Put the hash entry
        String hashEntryName = toHashEntryName(entry.getName());
        ZipEntry hashEntry = new ZipEntry(hashEntryName);
        zout.putNextEntry(hashEntry);
        zout.write(hash);
        zout.closeEntry();
      }

      Lang.closeQuietly(zin);
      Lang.closeQuietly(bout);
      Lang.closeQuietly(zout);

      return bout.toByteArray();

    } catch (Exception e) {
      throw new Exception("Cannot hash ZIP file items.", e);
    } finally {
      Lang.closeQuietly(zin);
      Lang.closeQuietly(bout);
      Lang.closeQuietly(zout);
    }
  }

  /**
   * Assumes that the zip files contain SHA-1 hashes. All the hash file items from the first zip file will be checked
   * against a matching hash file item from the second zip file. Only if the two zip files contain the same file items
   * and their hashes equal, <code>true</code> is returned. Otherwise this method returns <code>false</code>.
   *
   * <p>
   * Note: This method does not support duplicate entries in ZIP files.
   * </p>
   *
   * @param zipFile1
   *        The first zip file.
   * @param zipFile2
   *        The second zip file.
   *
   * @return Returns <code>true</code> if and only if all the hashes from the first file are available and equal in the
   *         second file. Otherwise <code>false</code> is returned.
   * @throws Exception
   *         On any error.
   */
  public static boolean isZipHashItemsEqual(byte[] zipFile1, byte[] zipFile2) throws Exception {
    ZipInputStream zin1 = new ZipInputStream(new ByteArrayInputStream(zipFile1));
    ZipInputStream zin2 = new ZipInputStream(new ByteArrayInputStream(zipFile2));

    try {

      // Get the hash mapping from the second file
      Map<String, byte[]> hashMapping = new Hashtable<>();
      {
        ZipEntry entry = null;
        while ((entry = zin2.getNextEntry()) != null) {
          // Get the file data
          byte[] hash1 = Lang.toByteArray(zin2);
          hashMapping.put(entry.getName(), hash1);
        }
        Lang.closeQuietly(zin2);
      }

      ZipEntry entry = null;
      boolean equal = true;
      while ((entry = zin1.getNextEntry()) != null && equal) {
        // If hash mapping does not provide the entry name, return false.
        if (hashMapping.containsKey(entry.getName())) {
          byte[] hash1 = Lang.toByteArray(zin1);
          byte[] hash2 = hashMapping.get(entry.getName());
          hashMapping.remove(entry.getName());
          // If entry name exists in both, hashes must be equal.
          equal = Arrays.equals(hash1, hash2);
        } else {
          equal = false;
        }
      }

      Lang.closeQuietly(zin1);

      return equal && hashMapping.isEmpty();

    } catch (Exception e) {
      throw new Exception("Cannot compare hashes of the specified ZIP files.");
    } finally {
      Lang.closeQuietly(zin1);
      Lang.closeQuietly(zin2);
    }
  }

  /**
   * Returns the entry name for a hashed file.
   *
   * @param name
   *        The original entry name.
   * @return Returns the entry name for the hashed file.
   */
  private static String toHashEntryName(String name) {
    return String.format(HASH_FILENAME_PATTERN, name);
  }

}
