package com.remondis.limbus.maven;

import java.io.PrintStream;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.aether.artifact.Artifact;
import org.eclipse.aether.graph.Dependency;
import org.eclipse.aether.graph.DependencyNode;
import org.eclipse.aether.graph.DependencyVisitor;
import org.eclipse.aether.util.artifact.ArtifactIdUtils;
import org.eclipse.aether.util.graph.manager.DependencyManagerUtils;
import org.eclipse.aether.util.graph.transformer.ConflictResolver;

public class ConsoleDependencyGraphDumper implements DependencyVisitor {

  private PrintStream out;

  private List<ChildInfo> childInfos = new LinkedList<ChildInfo>();

  public ConsoleDependencyGraphDumper() {
    this(null);
  }

  public ConsoleDependencyGraphDumper(PrintStream out) {
    this.out = (out != null) ? out : System.out;
  }

  @Override
  public boolean visitEnter(DependencyNode node) {
    out.println(formatIndentation() + formatNode(node));
    childInfos.add(new ChildInfo(node.getChildren()
        .size()));
    return true;
  }

  private String formatIndentation() {
    StringBuilder buffer = new StringBuilder(128);
    for (Iterator<ChildInfo> it = childInfos.iterator(); it.hasNext();) {
      buffer.append(it.next()
          .formatIndentation(!it.hasNext()));
    }
    return buffer.toString();
  }

  private String formatNode(DependencyNode node) {
    StringBuilder buffer = new StringBuilder(128);
    Artifact a = node.getArtifact();
    Dependency d = node.getDependency();
    buffer.append(a);
    if (d != null && d.getScope()
        .length() > 0) {
      buffer.append(" [")
          .append(d.getScope());
      if (d.isOptional()) {
        buffer.append(", optional");
      }
      buffer.append("]");
    }
    {
      String premanaged = DependencyManagerUtils.getPremanagedVersion(node);
      if (premanaged != null && !premanaged.equals(a.getBaseVersion())) {
        buffer.append(" (version managed from ")
            .append(premanaged)
            .append(")");
      }
    }
    {
      String premanaged = DependencyManagerUtils.getPremanagedScope(node);
      if (premanaged != null && !premanaged.equals(d.getScope())) {
        buffer.append(" (scope managed from ")
            .append(premanaged)
            .append(")");
      }
    }
    DependencyNode winner = (DependencyNode) node.getData()
        .get(ConflictResolver.NODE_DATA_WINNER);
    if (winner != null && !ArtifactIdUtils.equalsId(a, winner.getArtifact())) {
      Artifact w = winner.getArtifact();
      buffer.append(" (conflicts with ");
      if (ArtifactIdUtils.toVersionlessId(a)
          .equals(ArtifactIdUtils.toVersionlessId(w))) {
        buffer.append(w.getVersion());
      } else {
        buffer.append(w);
      }
      buffer.append(")");
    }
    return buffer.toString();
  }

  @Override
  public boolean visitLeave(DependencyNode node) {
    if (!childInfos.isEmpty()) {
      childInfos.remove(childInfos.size() - 1);
    }
    if (!childInfos.isEmpty()) {
      childInfos.get(childInfos.size() - 1).index++;
    }
    return true;
  }

  private static class ChildInfo {
    int count;
    int index;

    public ChildInfo(int count) {
      this.count = count;
    }

    public String formatIndentation(boolean end) {
      boolean last = index + 1 >= count;
      if (end) {
        return last ? "\\- " : "+- ";
      }
      return last ? "   " : "|  ";
    }

  }

}
