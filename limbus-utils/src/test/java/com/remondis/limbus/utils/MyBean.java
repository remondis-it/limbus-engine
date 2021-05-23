package com.remondis.limbus.utils;

public class MyBean {

  private String string;
  private int integer;

  public MyBean(String string, int integer) {
    super();
    this.string = string;
    this.integer = integer;
  }

  public MyBean() {
    super();
  }

  public String getString() {
    return string;
  }

  public void setString(String string) {
    this.string = string;
  }

  public int getInteger() {
    return integer;
  }

  public void setInteger(int integer) {
    this.integer = integer;
  }

}
