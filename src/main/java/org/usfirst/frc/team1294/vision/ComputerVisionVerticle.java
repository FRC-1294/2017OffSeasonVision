package org.usfirst.frc.team1294.vision;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.videoio.VideoCapture;

import static org.opencv.videoio.Videoio.CV_CAP_PROP_BUFFERSIZE;
import static org.opencv.videoio.Videoio.CV_CAP_PROP_FRAME_HEIGHT;
import static org.opencv.videoio.Videoio.CV_CAP_PROP_FRAME_WIDTH;

public class ComputerVisionVerticle extends AbstractVerticle {

  private final ComputerVisionConfig computerVisionConfig;

  private final VideoCapture videoCapture;
  private final Mat originalImage;
  private final TargetDetector targetDetector;

  public ComputerVisionVerticle(final ComputerVisionConfig computerVisionConfig,
      final TargetDetector targetDetector) {
    this.computerVisionConfig = computerVisionConfig;
    this.targetDetector = targetDetector;

    videoCapture = new VideoCapture(0);
    videoCapture.set(CV_CAP_PROP_BUFFERSIZE, 3);
    videoCapture.set(CV_CAP_PROP_FRAME_WIDTH, computerVisionConfig.getWidth());
    videoCapture.set(CV_CAP_PROP_FRAME_HEIGHT, computerVisionConfig.getHeight());

    originalImage = new Mat();
  }

  @Override
  public void start() throws Exception {
    vertx.setTimer(1, this::doWork);
  }

  private void doWork(Long aLong) {
    final long startTime = System.currentTimeMillis();
    try {
      // read the next frame from video
      videoCapture.read(originalImage);

      //
      if (computerVisionConfig.getWidth() < originalImage.width() || computerVisionConfig.getHeight() < originalImage.height()) {
        // resize image since it came form the camera too big despite us telling it not to
        Imgproc.resize(originalImage, originalImage, new Size(computerVisionConfig.getWidth(), computerVisionConfig.getHeight()));
      }

      // run the detection algorithm
      final Rect[] targets = targetDetector.doDetection(originalImage);

      // convert the image to jpeg
      MatOfByte m = new MatOfByte();
      MatOfInt parameters = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 100);
      Imgcodecs.imencode(".jpg", originalImage, m, parameters);
      final Buffer buffer = Buffer.buffer(m.toArray());

      // publish the image on the eventbus
      vertx.eventBus().publish("images", buffer);
    } catch (Exception ex) {
      ex.printStackTrace();
    } finally {
      // set a timer to do more work
      long delay = Math.max(1, (long) (1 / (double) this.computerVisionConfig.getMaxFps() * 1000) - (System.currentTimeMillis() - startTime));
      vertx.setTimer(delay, this::doWork);
    }
  }
}
