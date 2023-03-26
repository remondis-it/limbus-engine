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
import com.remondis.limbus.system.applicationExtern.ExternalPrivateComponent;
import com.remondis.limbus.system.applicationExtern.MyLocalPublicComponentImpl;
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
    assertTrue(localPublicComponent instanceof MyLocalPublicComponentImpl);

    assertTrue(system.hasComponent(AnotherPublicComponent.class));
    AnotherPublicComponent anotherPublicComponent = system.getComponent(AnotherPublicComponent.class);
    assertTrue(anotherPublicComponent instanceof AnotherPublicComponentImpl);

    assertTrue(system.hasComponent(BundlePublicComponent.class));
    BundlePublicComponent bundlePublicComponent = system.getComponent(BundlePublicComponent.class);
    assertTrue(bundlePublicComponent instanceof BundlePublicComponentImpl);

    List<Class> privateComponents = getPrivateComponents(system);
    assertThat(privateComponents).containsExactlyInAnyOrder(BundlePrivateComponent.class,
        ExternalPrivateComponent.class);

  }

  @Test
  public void shouldStartEmptySystem() throws LimbusSystemException {
    LimbusSystem system = LimbusSystem.fromApplication(TestApplication.class);
    system.initialize();
    assertTrue(system.getAllComponents()
        .isEmpty());
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
