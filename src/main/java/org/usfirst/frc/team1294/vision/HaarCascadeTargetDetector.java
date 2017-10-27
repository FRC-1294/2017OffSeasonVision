package org.usfirst.frc.team1294.vision;

import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

public class HaarCascadeTargetDetector extends TargetDetector {

  private final CascadeClassifier cascadeClassifier;
  private final Scalar color_red;

  public HaarCascadeTargetDetector() {
    cascadeClassifier = new CascadeClassifier("data/haarcascade_frontalface_default.xml");
    color_red = new Scalar(0, 0, 255);
  }

  @Override
  protected Rect[] doDetection(final Mat image) {
    final MatOfRect faces = new MatOfRect();
    cascadeClassifier.detectMultiScale(image,
        faces,
        1.3,
        3,
        0| Objdetect.CASCADE_SCALE_IMAGE,
        new Size(30, 30),
        new Size());

    final Rect[] rects = faces.toArray();

    // draw rects around each target
    for (Rect rect : rects) {
      Imgproc.rectangle(image, rect.tl(), rect.br(), color_red);
    }

    return rects;
  }
}
