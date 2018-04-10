package org.max5.limbus.monitoring;

import java.util.LinkedList;
import java.util.List;

import org.max5.limbus.utils.Lang;
import org.max5.limbus.utils.SerializeException;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias(value = "monitor")
public final class Pattern implements Comparable<Pattern> {

  @XStreamAsAttribute
  protected String pattern;

  @XStreamImplicit(itemFieldName = "publisherRef")
  protected List<String> publishers;

  public static void main(String[] args) throws SerializeException {
    Pattern p = new Pattern("com.some.package");
    p.publishers.add("PublisherImplA");
    p.publishers.add("PublisherImplB");

    MonitoringFactory.getDefaultXStream()
        .writeObject(p, System.out);
  }

  Pattern(String pattern) {
    super();
    Lang.denyNull("pattern", pattern);
    this.pattern = pattern;
    readResolve();
  }

  protected Object readResolve() {
    if (this.publishers == null) {
      this.publishers = new LinkedList<>();
    }
    return this;
  }

  /**
   * @return the pattern
   */
  public String getPattern() {
    return pattern;
  }

  /**
   * @param pattern
   *        the pattern to set
   */
  protected void setPattern(String pattern) {
    this.pattern = pattern;
  }

  /**
   * @return the publishers
   */
  protected List<String> getPublishers() {
    return new LinkedList<>(publishers);
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#hashCode()
   */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((pattern == null) ? 0 : pattern.hashCode());
    return result;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#equals(java.lang.Object)
   */
  @Override
  public boolean equals(Object obj) {
    if (this == obj)
      return true;
    if (obj == null)
      return false;
    if (getClass() != obj.getClass())
      return false;
    Pattern other = (Pattern) obj;
    if (pattern == null) {
      if (other.pattern != null)
        return false;
    } else if (!pattern.equals(other.pattern))
      return false;
    return true;
  }

  /*
   * (non-Javadoc)
   *
   * @see java.lang.Object#toString()
   */
  @Override
  public String toString() {
    return "Pattern [pattern=" + pattern + ", publishers=" + publishers + "]";
  }

  @Override
  public int compareTo(Pattern o) {
    return PatternSpecifityComparator.compareSpecifity(this, o);
  }

}
