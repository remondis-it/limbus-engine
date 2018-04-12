package com.remondis.limbus.system.visualize;

import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import javax.swing.SwingUtilities;

import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.SingleGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.Viewer.CloseFramePolicy;
import com.remondis.limbus.IInitializable;
import com.remondis.limbus.Initializable;
import com.remondis.limbus.system.Component;
import com.remondis.limbus.system.ComponentConfiguration;
import com.remondis.limbus.system.InfoRecord;
import com.remondis.limbus.system.LimbusComponent;
import com.remondis.limbus.system.LimbusContainer;
import com.remondis.limbus.system.LimbusSystem;
import com.remondis.limbus.system.LimbusSystemListener;
import com.remondis.limbus.utils.ReflectionUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This is the visualizer component that shows the object graph that is maintained by a {@link LimbusSystem} instance.
 *
 * @author schuettec
 *
 */
public class LimbusSystemVisualizer extends Initializable<Exception> implements LimbusSystemListener {

  private static final Logger log = LoggerFactory.getLogger(LimbusSystemVisualizer.class);

  @LimbusContainer
  private LimbusSystem system;

  protected Viewer display;

  public LimbusSystemVisualizer() {
  }

  @Override
  protected void performInitialize() throws Exception {
  }

  @Override
  public void postInitialize() {
    System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
    Graph graph = new SingleGraph("id", true, false);
    try {
      String cssFormat = "url('%s')";
      String cssUrl = getClass().getResource("/graph.css")
          .toURI()
          .toURL()
          .toString();
      graph.addAttribute("ui.stylesheet", String.format(cssFormat, cssUrl));
    } catch (Exception e) {
      log.error("Could not find stylesheet for graph.", e);
    }
    this.display = graph.display();
    Layout layout = new SpringBox(false);
    graph.addSink(layout);
    layout.addAttributeSink(graph);

    this.display.setCloseFramePolicy(CloseFramePolicy.HIDE_ONLY);
    createSystemNode(graph);

    // schuettec - 28.03.2017 : This method relies on the order of the InfoRecords which is reflected by the
    // documentation of the Limbus System that this is a valid assumption.
    List<InfoRecord> infoRecords = system.getInfoRecords();
    for (InfoRecord info : infoRecords) {
      Component component = info.getComponent();

      // Create the current component node
      Node current = createComponentNode(graph, component);

      // Get the node ids of all dependencies
      Set<String> dependencies = getDependencies(component);

      // Connect the current component node with all of its dependencies
      // schuettec - 28.03.2017 : Due to the fact that Limbus System calculates a valid initialization order and
      // cirular dependencies are denied, we can assume that all dependencies will be known by the graph at this
      // point. (If not, the Limbus System would have detected an error and aborts initialization.)
      for (String dep : dependencies) {
        Node dependency = graph.getNode(dep);
        graph.addEdge(UUID.randomUUID()
            .toString(), current, dependency, true);
      }
    }
    // iterate the compute() method a number of times
    while (layout.getStabilization() < 0.9) {
      layout.compute();
    }
    display.disableAutoLayout();
  }

  @Override
  public void preDestroy() {

  }

  @Override
  protected void performFinish() {
    // Do this in Swing Thread!
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        display.close();
        display = null;
      }
    });
  }

  private Node createComponentNode(Graph graph, Component component) {
    ComponentConfiguration configuration = component.getConfiguration();
    Class<? extends IInitializable<?>> requestType = configuration.getRequestType();
    Class<? extends IInitializable<?>> implType = configuration.getComponentType();

    boolean isPublic = configuration.isPublicComponent();
    String implLabel = implType.getSimpleName();
    Node current = graph.addNode(isPublic ? requestType.getName() : UUID.randomUUID()
        .toString());
    current.addAttribute("ui.label", implLabel);
    if (!isPublic)
      current.addAttribute("ui.class", "private");

    return current;
  }

  private void createSystemNode(Graph graph) {
    Node systemNode = graph.addNode(LimbusSystem.class.getName());
    systemNode.addAttribute("ui.label", "Limbus System");

  }

  private Set<String> getDependencies(Component component) {
    IInitializable<?> instance = component.getInstance();
    List<Field> fields = ReflectionUtil.getAllAnnotatedFields(instance.getClass(), LimbusContainer.class,
        LimbusComponent.class);
    Set<String> deps = new HashSet<>();
    for (Field field : fields) {
      deps.add(field.getType()
          .getName());
    }
    return deps;
  }

}
