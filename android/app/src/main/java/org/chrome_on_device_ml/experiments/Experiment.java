package org.chrome_on_device_ml.experiments;

public interface Experiment {
  public void initialize();
  public void close();
  public void evaluate(int numberOfContents);
  public double getTime();
}
