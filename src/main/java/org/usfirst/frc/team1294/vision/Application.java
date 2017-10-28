package org.usfirst.frc.team1294.vision;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import org.usfirst.frc.team1294.opencv.OpenCV;

public class Application {

  public static void main(String[] args) {
    OpenCV.loadNativeLibraries();

    final Vertx vertx = Vertx.vertx();
    final ComputerVisionConfig computerVisionConfig = new ComputerVisionConfig(15, 800, 600);
    final TargetDetector targetDetector = new Features2DPlusHomographyTargetDetector();
    final ComputerVisionVerticle computerVisionVerticle = new ComputerVisionVerticle(
        computerVisionConfig, targetDetector);
    vertx.deployVerticle(computerVisionVerticle, new DeploymentOptions().setWorker(true));
    vertx.deployVerticle(new MJpegStreamerVerticle());
  }

}
