package org.usfirst.frc.team1294.vision;

public class ComputerVisionConfig {

  private int maxFps;
  private int width;
  private int height;

  public ComputerVisionConfig() {

  }

  public ComputerVisionConfig(int maxFps, int width, int height) {
    this.maxFps = maxFps;
    this.width = width;
    this.height = height;
  }

  public int getMaxFps() {
    return maxFps;
  }

  public void setMaxFps(int maxFps) {
    this.maxFps = maxFps;
  }

  public int getWidth() {
    return width;
  }

  public void setWidth(int width) {
    this.width = width;
  }

  public int getHeight() {
    return height;
  }

  public void setHeight(int height) {
    this.height = height;
  }
}
