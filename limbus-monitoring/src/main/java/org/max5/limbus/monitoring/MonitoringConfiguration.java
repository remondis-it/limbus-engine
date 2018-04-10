package org.max5.limbus.monitoring;

import java.util.LinkedList;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamImplicit;

@XStreamAlias("configuration")
public class MonitoringConfiguration {

  /**
   * Holds the processing configuration like thread pool size etc.
   */
  private ProcessingConfig processing;

  @XStreamImplicit(itemFieldName = "publisher")
  private List<PublisherConfig> publisher;

  @XStreamImplicit
  private List<Pattern> patterns;

  public MonitoringConfiguration() {
    readResolve();
  }

  protected Object readResolve() {
    if (this.patterns == null) {
      this.patterns = new LinkedList<>();
    }
    if (this.publisher == null) {
      this.publisher = new LinkedList<>();
    }
    if (this.processing == null) {
      this.processing = new ProcessingConfig();
    }
    return this;
  }

  /**
   * Adds a {@link PublisherConfig} to this configuration.
   *
   * @param e
   *        The publisher to add
   */
  public void addPublisher(PublisherConfig e) {
    publisher.add(e);
  }

  /**
   * Removes a {@link PublisherConfig} from this configuration.
   *
   * @param o
   *        The publisher to remove.
   * @see java.util.Set#remove(java.lang.Object)
   */
  public void removePublisher(PublisherConfig o) {
    publisher.remove(o);
  }

  /**
   * Adds a {@link Pattern} to this configuration.
   *
   * @param e
   *        The pattern to add
   */
  public void addPattern(Pattern e) {
    patterns.add(e);
  }

  /**
   * Removes a {@link Pattern} from this configuration.
   *
   * @param o
   *        The pattern to remove.
   * @see java.util.Set#remove(java.lang.Object)
   */
  public void removePattern(Pattern o) {
    patterns.remove(o);
  }

  /**
   * @return the processing Returns the processing configuration
   */
  public ProcessingConfig getProcessing() {
    return processing;
  }

  /**
   * Sets the new processing configuration. If <code>null</code> the default processing configuration is used.
   *
   * @param processing
   *        the processing to set. If <code>null</code> the default processing configuration is used.
   */
  public void setProcessing(ProcessingConfig processing) {
    if (processing == null) {
      this.processing = new ProcessingConfig();
    } else {
      this.processing = processing;
    }
  }

  public List<PublisherConfig> getPublishers() {
    return new LinkedList<>(this.publisher);
  }

  public List<Pattern> getPatterns() {
    return new LinkedList<>(this.patterns);
  }

}
