package com.remondis.limbus.maintenance;

import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Comparator;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import com.googlecode.lanterna.gui2.ActionListBox;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.Container;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox;
import com.remondis.limbus.engine.LimbusMaintenanceConsole;
import com.remondis.limbus.utils.Lang;

public class LogFileViewer extends AbstractLimbusItem {
  public LogFileViewer() {
    super("Log File Viewer");
  }

  @Override
  public Container getComponent(LimbusMaintenanceConsole gui) {
    Panel p = new Panel(new BorderLayout());

    // Log File Viewer
    final TextBox textBox = new TextBox();

    // File Chooser
    ActionListBox fileChooser = new ActionListBox();
    Path logPath = Paths.get("./logs/")
        .toAbsolutePath()
        .normalize();

    // try (DirectoryStream<Path> stream = Files.newDirectoryStream(logPath)) {
    try (Stream<Path> stream = StreamSupport.stream(Files.newDirectoryStream(logPath)
        .spliterator(), false)
        .sorted(new Comparator<Path>() {

          @Override
          public int compare(Path o1, Path o2) {
            return o1.getFileName()
                .toString()
                .compareTo(o2.getFileName()
                    .toString())
                * -1;
          }
        })) {
      stream.iterator()
          .forEachRemaining((Path path) -> {
            String fileName = path.getFileName()
                .toString();
            fileChooser.addItem(fileName, () -> {
              try (FileInputStream fin = new FileInputStream(path.toFile());
                  ByteArrayOutputStream bout = new ByteArrayOutputStream()) {
                Lang.copy(fin, bout);
                byte[] byteArray = bout.toByteArray();
                String logFile = new String(byteArray);
                textBox.setText(logFile);
              } catch (Exception e) {
                textBox.addLine("Cannot read log file: " + path.toString());
                String exceptionAsString = Lang.exceptionAsString(e);
                textBox.addLine("Cannot read log file: " + path.toString());
                textBox.addLine(exceptionAsString);
              }

            });
          });
    } catch (IOException e) {
      fileChooser.addItem("I/O error", () -> {
        String exceptionAsString = Lang.exceptionAsString(e);
        textBox.setText(exceptionAsString);
      });
    }

    // Add File Chooser
    p.addComponent(fileChooser, BorderLayout.Location.LEFT);

    // Log File Viewer
    p.addComponent(textBox, BorderLayout.Location.CENTER);

    return p;
  }

}
