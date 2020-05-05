package com.remondis.limbus.system.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.remondis.limbus.system.InfoRecord;
import com.remondis.limbus.system.LimbusSystem;
import com.remondis.limbus.system.LimbusSystemException;
import com.remondis.limbus.system.application.subpackage.SubPrivateComponentImpl;
import com.remondis.limbus.system.application.subpackage.SubPublicComponent;
import com.remondis.limbus.system.application.subpackage.SubPublicComponentImpl;
import com.remondis.limbus.system.applicationExtern.ExternalPrivateComponent;
import com.remondis.limbus.system.applicationExtern.PossibleLocalPublicComponentOverrideImpl;
import com.remondis.limbus.system.bundle.BundlePrivateComponent;
import com.remondis.limbus.system.bundle.BundlePublicComponent;
import com.remondis.limbus.system.bundle.BundlePublicComponentImpl;

@ExtendWith(MockitoExtension.class)
public class LimbusApplicationTest {

  @Test
  public void shouldImportBundleCorrectly() throws LimbusSystemException {
    LimbusSystem system = LimbusSystem.fromApplication(TestApplicationWithOverridesAndBundle.class);
    system.initialize();
    assertTrue(system.hasComponent(LocalPublicComponent.class));
    LocalPublicComponent localPublicComponent = system.getComponent(LocalPublicComponent.class);
    assertTrue(localPublicComponent instanceof PossibleLocalPublicComponentOverrideImpl);

    assertTrue(system.hasComponent(AnotherPublicComponent.class));
    AnotherPublicComponent anotherPublicComponent = system.getComponent(AnotherPublicComponent.class);
    assertTrue(anotherPublicComponent instanceof AnotherPublicComponentImpl);

    assertTrue(system.hasComponent(SubPublicComponent.class));
    SubPublicComponent subPublicComponent = system.getComponent(SubPublicComponent.class);
    assertTrue(subPublicComponent instanceof SubPublicComponentImpl);

    assertTrue(system.hasComponent(BundlePublicComponent.class));
    BundlePublicComponent bundlePublicComponent = system.getComponent(BundlePublicComponent.class);
    assertTrue(bundlePublicComponent instanceof BundlePublicComponentImpl);

    List<Class> privateComponents = getPrivateComponents(system);
    assertThat(privateComponents).containsExactlyInAnyOrder(BundlePrivateComponent.class,
        ExternalPrivateComponent.class, LocalPrivateComponent.class, SubPrivateComponentImpl.class);

  }

  @Test
  public void annotationsShouldOverrideComponentScan() throws LimbusSystemException {
    LimbusSystem system = LimbusSystem.fromApplication(TestApplicationWithOverrideAndExternalPrivateComponent.class);
    system.initialize();
    assertTrue(system.hasComponent(LocalPublicComponent.class));
    LocalPublicComponent localPublicComponent = system.getComponent(LocalPublicComponent.class);
    assertTrue(localPublicComponent instanceof PossibleLocalPublicComponentOverrideImpl);

    assertTrue(system.hasComponent(SubPublicComponent.class));
    SubPublicComponent subPublicComponent = system.getComponent(SubPublicComponent.class);
    assertTrue(subPublicComponent instanceof SubPublicComponentImpl);

    List<Class> privateComponents = getPrivateComponents(system);
    assertThat(privateComponents).containsExactlyInAnyOrder(ExternalPrivateComponent.class, LocalPrivateComponent.class,
        SubPrivateComponentImpl.class);

  }

  @Test
  public void shouldStartSystemCorrectly() throws LimbusSystemException {
    LimbusSystem system = LimbusSystem.fromApplication(TestApplication.class);
    system.initialize();
    assertTrue(system.hasComponent(LocalPublicComponent.class));
    LocalPublicComponent localPublicComponent = system.getComponent(LocalPublicComponent.class);
    assertTrue(localPublicComponent instanceof LocalPublicComponentImpl);

    assertTrue(system.hasComponent(SubPublicComponent.class));
    SubPublicComponent subPublicComponent = system.getComponent(SubPublicComponent.class);
    assertTrue(subPublicComponent instanceof SubPublicComponentImpl);

    List<Class> privateComponents = getPrivateComponents(system);
    assertThat(privateComponents).containsExactlyInAnyOrder(LocalPrivateComponent.class, SubPrivateComponentImpl.class);

  }

  private List<Class> getPrivateComponents(LimbusSystem system) {
    return system.getInfoRecords()
        .stream()
        .filter(not(InfoRecord::isPublicComponent))
        .map(info -> info.getComponent()
            .getConfiguration()
            .getComponentType())
        .collect(Collectors.toList());
  }

  public static <T> Predicate<T> not(Predicate<T> t) {
    return t.negate();
  }
}
