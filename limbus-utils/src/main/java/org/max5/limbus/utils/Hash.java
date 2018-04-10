/**
 *
 */
package org.max5.limbus.utils;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Utility class to calculate SHA-1 hash of a byte array. SHA-1 hashes are always (20 bytes) long.
 *
 * @author schuettec
 *
 */
public class Hash {

  /**
   * Converts byte data to SHA-1 hash.
   *
   * @param data
   *        The data.
   * @return Returns the SHA-1 hash in as byte array.
   *
   * @throws NoSuchAlgorithmException
   *         Thrown if the algorithm is not available.
   */
  public static byte[] toSHA(byte[] data) throws NoSuchAlgorithmException {
    MessageDigest md = MessageDigest.getInstance("SHA-1");
    return md.digest(data);
  }
}
