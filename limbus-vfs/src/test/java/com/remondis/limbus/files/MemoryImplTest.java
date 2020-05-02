package com.remondis.limbus.files;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.remondis.limbus.utils.Lang;

public class MemoryImplTest {

  private static final String SOME_FOLDER = "SOME_FOLDER";

  private static final String ROOT_FILE = "rootFile";

  private LimbusFileService fs;

  @BeforeEach
  public void before() throws Exception {
    Lang.initializeJDKLogging();
    fs = new InMemoryFilesystemImpl();
    fs.initialize();
  }

  @AfterEach
  public void after() {
    fs.finish();
  }

  @Test
  public void test_no_append_to_file() throws IOException, FileAccessException {

    String rootContent = UUID.randomUUID()
        .toString();
    try (OutputStream output = fs.createFile(ROOT_FILE)) {
      // Assert that the file does not exist. It may only exist after closing the stream
      output.write(rootContent.getBytes());
    }

    String appendContent = UUID.randomUUID()
        .toString();
    // assert append
    try (OutputStream output = fs.createFile(ROOT_FILE)) {
      // Assert that the file does not exist. It may only exist after closing the stream
      output.write(appendContent.getBytes());
    }

    byte[] expectedContent = appendContent.getBytes();
    byte[] actualContent = fs.getFileContent(ROOT_FILE);

    assertArrayEquals(expectedContent, actualContent);
  }

  @Test
  public void test_append_to_file() throws IOException, FileAccessException {

    ByteArrayOutputStream appendOut = new ByteArrayOutputStream();

    String rootContent = UUID.randomUUID()
        .toString();
    try (OutputStream output = fs.createFile(ROOT_FILE)) {
      // Assert that the file does not exist. It may only exist after closing the stream
      output.write(rootContent.getBytes());
      appendOut.write(rootContent.getBytes());
    }

    String appendContent = UUID.randomUUID()
        .toString();
    // assert append
    try (OutputStream output = fs.createFile(ROOT_FILE, true)) {
      // Assert that the file does not exist. It may only exist after closing the stream
      output.write(appendContent.getBytes());
      appendOut.write(appendContent.getBytes());
    }

    byte[] expectedContent = appendOut.toByteArray();
    appendOut.close();
    byte[] actualContent = fs.getFileContent(ROOT_FILE);

    assertArrayEquals(expectedContent, actualContent);
  }

  @Test
  public void test_create_folder() throws FileAccessException, IOException {
    fs.createFolder(SOME_FOLDER, false);
    assertTrue(fs.hasFolder(SOME_FOLDER));
    assertFalse(fs.hasFile(SOME_FOLDER));
    try {
      fs.getFile(SOME_FOLDER);
      fail("FileNotFoundException expected!");
    } catch (FileNotFoundException e) {
      // Expected
    }
    try {
      fs.getFileAsStream(SOME_FOLDER);
      fail("FileNotFoundException expected!");
    } catch (FileNotFoundException e) {
      // Expected
    }
    try {
      fs.getFileContent(SOME_FOLDER);
      fail("FileNotFoundException expected!");
    } catch (FileNotFoundException e) {
      // Expected
    }
    assertEmptyFolder(SOME_FOLDER);

    try {
      fs.createFolder(fs.toPath(SOME_FOLDER, "A", "B", "C"), false);
      fail("FileNotFoundException was expected.");
    } catch (FileNotFoundException e) {
      // Expected
    }

    fs.createFolder(fs.toPath(SOME_FOLDER, "A", "B", "C"), true);
    assertFalse(fs.isFolderEmpty(fs.toPath(SOME_FOLDER)));
    assertFalse(fs.isFolderEmpty(fs.toPath(SOME_FOLDER, "A")));
    assertFalse(fs.isFolderEmpty(fs.toPath(SOME_FOLDER, "A", "B")));
    assertTrue(fs.isFolderEmpty(fs.toPath(SOME_FOLDER, "A", "B", "C")));

    String expectedContent = UUID.randomUUID()
        .toString();
    String testFilePath = fs.toPath(SOME_FOLDER, "A", "B", "testFile");
    try (OutputStream testFile = fs.createFile(testFilePath)) {
      testFile.write(expectedContent.getBytes());
    }
    String actualContent = new String(fs.getFileContent(testFilePath));
    assertEquals(expectedContent, actualContent);

    fs.deleteFolder(SOME_FOLDER);
    assertFalse(fs.hasFolder(fs.toPath(SOME_FOLDER)));
    assertFalse(fs.hasFolder(fs.toPath(SOME_FOLDER, "A")));
    assertFalse(fs.hasFolder(fs.toPath(SOME_FOLDER, "A", "B")));
    assertFalse(fs.hasFolder(fs.toPath(SOME_FOLDER, "A", "B", "C")));
    assertFalse(fs.hasFile(testFilePath));
  }

  private void assertEmptyFolder(String someFolder) {
    assertTrue(fs.hasFolder(someFolder));
    List<String> entries = fs.getFolderEntries(someFolder);
    assertTrue(entries.isEmpty());
    assertTrue(fs.isFolderEmpty(someFolder));
  }

  @Test
  public void test_create_root_file_and_rename() throws FileAccessException, IOException {
    String rootContent = UUID.randomUUID()
        .toString();
    try (OutputStream output = fs.createFile(ROOT_FILE)) {
      // Assert that the file does not exist. It may only exist after closing the stream
      assertFalse(fs.hasFile(ROOT_FILE));
      output.write(rootContent.getBytes());
    }
    // The file exists after close was called on the output stream
    assertTrue(fs.hasFile(ROOT_FILE));

    URL file = fs.getFile(ROOT_FILE);
    InputStream input = file.openStream();
    byte[] bytesByUrl = Lang.toByteArray(input);

    input = fs.getFileAsStream(ROOT_FILE);
    byte[] bytesByStream = Lang.toByteArray(input);

    byte[] bytesAsBytes = fs.getFileContent(ROOT_FILE);

    assertEquals(rootContent, new String(bytesByUrl));
    assertEquals(rootContent, new String(bytesByStream));
    assertEquals(rootContent, new String(bytesAsBytes));

    String newFilename1 = fs.toPath("a", "b", "c", "newFile");
    {
      try {
        fs.renameFile(ROOT_FILE, newFilename1);
        fail("Moving a file to another folder using rename is not allowed!");
      } catch (Exception e) {
        // Expected!
      }
    }
    String newFilename2 = "newFile";
    {
      fs.renameFile(ROOT_FILE, newFilename2);
      assertFalse(fs.hasFile(ROOT_FILE));
      assertTrue(fs.hasFile(newFilename2));
    }
  }

}
