package org.max5.limbus.monitoring;

import org.max5.limbus.utils.Lang;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamAsAttribute;

@XStreamAlias(value = "publisher")
public final class PublisherConfig {

  @XStreamAsAttribute
  protected String id;

  protected Object instance;

  PublisherConfig(String id, Object instance) {
    super();
    Lang.denyNull("id", id);
    Lang.denyNull("instance", instance);
    this.id = id;
    this.instance = instance;
    readResolve();
  }

  protected Object readResolve() {
    return this;
  }

  /**
   * @return the id
   */
  public String getId() {
    return id;
  }

  /**
   * @param id
   *        the id to set
   */
  protected void setId(String id) {
    this.id = id;
  }

  /**
   * @return the instance
   */
  public Object getInstance() {
    return instance;
  }

  /**
   * @param instance
   *        the instance to set
   */
  public void setInstance(Object instance) {
    this.instance = instance;
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
    result = prime * result + ((id == null) ? 0 : id.hashCode());
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
    PublisherConfig other = (PublisherConfig) obj;
    if (id == null) {
      if (other.id != null)
        return false;
    } else if (!id.equals(other.id))
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
    return "Publisher [id=" + id + ", instance=" + instance.toString() + "]";
  }

}
