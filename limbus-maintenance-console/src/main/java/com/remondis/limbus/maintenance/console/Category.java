package com.remondis.limbus.maintenance.console;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

/**
 * This class defines an action that opens a child category. A categorie can contain further categories or actions that
 * result in opening a panel.
 *
 * 
 *
 */
public class Category extends Item {

  protected List<Item> children;

  public Category(String title) {
    this(title, (Item[]) null);
  }

  public Category(String title, Item... children) {
    super(title);
    this.children = new LinkedList<>();

    add(children);
  }

  public void add(Item... children) {
    if (children != null) {
      this.children.addAll(Arrays.asList(children));
    }
  }

  public Item select(int index) {
    return children.get(index);
  }

  public Item select(Object object) {
    return children.get(children.indexOf(object));
  }

  public List<Item> getItems() {
    return children;
  }

}
