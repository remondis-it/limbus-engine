package com.remondis.limbus.engine.logging;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayInputStream;
import java.io.PrintStream;
import java.util.Scanner;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remondis.limbus.files.FileAccessException;
import com.remondis.limbus.files.InMemoryFilesystemImpl;
import com.remondis.limbus.files.LimbusFileService;

public class RolloverFileOutputStreamTest {

  private InMemoryFilesystemImpl fs;

  public static void main(String[] args) throws Exception {
    InMemoryFilesystemImpl fs = new InMemoryFilesystemImpl();
    fs.initialize();
    // schuettec - 31.03.2017 : Create the logging folder
    fs.createFolder(LimbusFileService.LOGGING_DIRECTORY, false);

    RolloverFileOutputStream out = new RolloverFileOutputStream(fs, "yyyy_mm_dd_test", true);
    PrintStream ps = new PrintStream(out);
    String[] strings = testStrings();
    for (int i = 0; i < 10; i++) {
      logStrings(strings, ps);
      System.out.println(fs);
      System.out.println("----------------------");
      Thread.sleep(3000);
    }
    ps.close();

  }

  @BeforeEach
  public void before() throws Exception {
    this.fs = new InMemoryFilesystemImpl();
    this.fs.initialize();
  }

  @AfterEach
  public void after() {
    this.fs.finish();
  }

  @Test
  public void test() throws FileAccessException {
    // schuettec - 31.03.2017 : Create the logging folder
    fs.createFolder(LimbusFileService.LOGGING_DIRECTORY, false);

    // schuettec - 31.03.2017 : Assert an empty log folder.
    assertEquals(0, fs.getFolderEntries(LimbusFileService.LOGGING_DIRECTORY)
        .size());

    // schuettec - 31.03.2017 : Perform the first check with a non existent log file.
    {
      RolloverFileOutputStream out = new RolloverFileOutputStream(fs, "yyyy_mm_dd_test", true);
      String file = out.getCurrentFilename();
      PrintStream ps = new PrintStream(out);
      String[] strings = testStrings();
      logStrings(strings, ps);
      ps.close();
      byte[] fileContent = fs.getFileContent(file);
      assertStrings(strings, fileContent);
    }
    // schuettec - 31.03.2017 : Assert the current log file.
    assertEquals(1, fs.getFolderEntries(LimbusFileService.LOGGING_DIRECTORY)
        .size());

    // schuettec - 31.03.2017 : Perform a second logging with append=false. This causes the renaming to the backup file.
    {
      RolloverFileOutputStream out = new RolloverFileOutputStream(fs, "yyyy_mm_dd_test", false);
      String file = out.getCurrentFilename();
      PrintStream ps = new PrintStream(out);
      String[] strings = testStrings();
      logStrings(strings, ps);
      ps.close();
      byte[] fileContent = fs.getFileContent(file);
      assertStrings(strings, fileContent);
    }

    // schuettec - 31.03.2017 : Assert the current log file.
    assertEquals(2, fs.getFolderEntries(LimbusFileService.LOGGING_DIRECTORY)
        .size());
  }

  private static void assertStrings(String[] strings, byte[] fileContent) {
    ByteArrayInputStream bin = new ByteArrayInputStream(fileContent);
    try (Scanner scan = new Scanner(bin)) {
      for (String str : strings) {
        if (scan.hasNextLine()) {
          assertEquals(str, scan.nextLine());
        } else {
          fail("There were more strings expected than found in the log file content .");
        }
      }
    }
  }

  private static void logStrings(String[] strings, PrintStream ps) {
    for (String str : strings) {
      ps.println(str);
    }
  }

  private static String[] testStrings() {
    int uuids = 10;
    String[] testStrings = new String[uuids];
    for (int i = 0; i < uuids; i++) {
      testStrings[i] = UUID.randomUUID()
          .toString();
    }
    return testStrings;
  }

}
