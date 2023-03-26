package com.remondis.limbus.engine.logging;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.Map;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class RoutedOutputStreamTest {

  @Mock
  TargetSelector<UUID, String> selectorMock;

  @Mock
  TargetWriter<String> writerMock;

  String defaultTarget = UUID.randomUUID()
      .toString();

  @SuppressWarnings("unchecked")
  @Test
  public void test_with_targets() throws IOException {
    String target1 = UUID.randomUUID()
        .toString();
    String target2 = UUID.randomUUID()
        .toString();
    String target3 = UUID.randomUUID()
        .toString();

    UUID id1 = UUID.randomUUID();
    UUID id2 = UUID.randomUUID();
    UUID id3 = UUID.randomUUID();

    {
      // This test only set a default target and nothing more
      RoutedOutputStream<UUID, String> output = new RoutedOutputStream<UUID, String>(selectorMock, writerMock,
          defaultTarget);
      output.addTarget(id1, target1);
      output.addTarget(id2, target2);
      output.addTarget(id3, target3);

      doReturn(target2).when(selectorMock)
          .selectTarget((Map<UUID, String>) any());
      assertWriteTo(output, writerMock, target2);

      doReturn(target1).when(selectorMock)
          .selectTarget((Map<UUID, String>) any());
      assertWriteTo(output, writerMock, target1);

      doReturn(target3).when(selectorMock)
          .selectTarget((Map<UUID, String>) any());
      assertWriteTo(output, writerMock, target3);

      doReturn(null).when(selectorMock)
          .selectTarget((Map<UUID, String>) any());
      assertWriteTo(output, writerMock, defaultTarget);

    }
  }

  @SuppressWarnings("unchecked")
  @Test
  public void test_default_target() throws IOException {
    // This test only set a default target and nothing more
    RoutedOutputStream<UUID, String> output = new RoutedOutputStream<UUID, String>(selectorMock, writerMock,
        defaultTarget);
    doReturn(null).when(selectorMock)
        .selectTarget((Map<UUID, String>) any());

    assertWriteTo(output, writerMock, defaultTarget);

    output.flush();
    verify(writerMock, times(1)).flush(defaultTarget);

    output.close();
    verify(writerMock, times(1)).flush(defaultTarget);

  }

  private void assertWriteTo(RoutedOutputStream<UUID, String> output, TargetWriter<String> targetWriter, String target)
      throws IOException, TargetWriteException {
    byte[] bytes = UUID.randomUUID()
        .toString()
        .getBytes();

    int expectedSingleWrite = -3457654;
    output.write(expectedSingleWrite);
    output.write(bytes);
    output.write(bytes, 0, bytes.length);

    verify(targetWriter, times(1)).writeTo(expectedSingleWrite, target);

    verify(targetWriter, times(2)).writeTo(bytes, 0, bytes.length, target);
  }

}
