package com.remondis.limbus.tasks;

import java.util.function.Function;

/**
 * A linear interpolator between two points.
 *
 * @author schuettec
 *
 */
public class LinearInterpolator implements Function<Double, Double> {

  private double m;
  private double b;

  public LinearInterpolator(double x1, double y1, double x2, double y2) {
    this.m = (y2 - y1) / (x2 - x1);
    this.b = y1 - m * x1;
  }

  public double getYFor(double x) {
    return m * x + b;
  }

  @Override
  public Double apply(Double t) {
    return getYFor(t);
  }

}
