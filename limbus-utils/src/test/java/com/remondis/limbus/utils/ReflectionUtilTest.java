package com.remondis.limbus.utils;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.util.List;

import org.junit.jupiter.api.Test;

public class ReflectionUtilTest {

  @Test
  public void shouldListCorrectlyForJars() throws Exception {
    List<String> classNamesFromPackage = ReflectionUtil.getClassNamesFromPackage("junit.runner");
    assertFalse(classNamesFromPackage.isEmpty());
    // TODO: If the JUnit version changes, the number of resources in junit.runner may change.
    // Version was junit-4.12 with 5 resources.
    assertEquals(5, classNamesFromPackage.size());
  }

}
