package org.usfirst.frc.team1294.vision;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.buffer.Buffer;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;
import org.opencv.videoio.VideoCapture;

public class ComputerVisionVerticle extends AbstractVerticle {

  private static final int MAX_FPS = 30;

  private final VideoCapture videoCapture;
  private final Mat originalImage;
  private final Mat grayImage;
  private final CascadeClassifier cascadeClassifier;

  public ComputerVisionVerticle() {
    videoCapture = new VideoCapture(0);
    originalImage = new Mat();
    grayImage = new Mat();
    cascadeClassifier = new CascadeClassifier("data/haarcascade_frontalface_default.xml");
  }

  @Override
  public void start() throws Exception {
    scheduleWork(0);
  }

  private void handleTimer(Long aLong) {
    final long startTime = System.currentTimeMillis();
    try {
      captureFrameAndDetectFaces();
      publishImageToEventBus();
    } finally {
      scheduleWork(startTime);
    }
  }

  private void scheduleWork(long startTime) {
    long delay = (long) (1 / (double)MAX_FPS * 1000) - (System.currentTimeMillis() - startTime);
    if (delay < 1) {
      delay = 1;
    }
    vertx.setTimer(delay, this::handleTimer);
  }

  private void captureFrameAndDetectFaces() {
    videoCapture.read(originalImage);

    Imgproc.cvtColor(originalImage, grayImage, Imgproc.COLOR_BGR2GRAY);

    final MatOfRect faces = new MatOfRect();
    cascadeClassifier.detectMultiScale(grayImage,
        faces,
        1.3,
        3,
        0| Objdetect.CASCADE_SCALE_IMAGE,
        new Size(30, 30),
        new Size());

    for (Rect rect : faces.toArray()) {
      Scalar color_red = new Scalar(0, 0, 255);
      Imgproc.rectangle(originalImage, rect.tl(), rect.br(), color_red);
    }
  }

  private void publishImageToEventBus() {
    MatOfByte m = new MatOfByte();
    MatOfInt parameters = new MatOfInt(Imgcodecs.IMWRITE_JPEG_QUALITY, 100);
    Imgcodecs.imencode(".jpg", originalImage, m, parameters);
    final Buffer buffer = Buffer.buffer(m.toArray());

    vertx.eventBus().publish("images", buffer);
  }
}
