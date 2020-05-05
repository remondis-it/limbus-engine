package com.remondis.limbus.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ReflectionUtilTest {

  @Test
  public void shouldListCorrectlyForJars() throws Exception {
    List<String> classNamesFromPackage = ReflectionUtil.getClassNamesFromPackage("org.slf4j.spi");
    assertFalse(classNamesFromPackage.isEmpty());
    // TODO: If the SLF4J version changes, the number of resources in org.slf4j.spi may change!
    // Version wasslf4j-api-2.0.0-alpha1 with 10 resources.
    assertEquals(10, classNamesFromPackage.size());
  }

}
