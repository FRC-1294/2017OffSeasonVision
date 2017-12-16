package org.usfirst.frc.team1294.vision;

import com.google.common.io.Resources;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.objdetect.Objdetect;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

public class HaarCascadeTargetDetector extends TargetDetector {

  private final CascadeClassifier cascadeClassifier;
  private final Scalar color_red;

  public HaarCascadeTargetDetector() {
    final String path;
    try {
      final File tempFile = File.createTempFile("cascade", ".xml");
      tempFile.deleteOnExit();
      path = tempFile.getAbsolutePath();
      try (FileOutputStream fileOutputStream = new FileOutputStream(tempFile)) {
        Resources.copy(Resources.getResource("haarcascade_frontalface_default.xml"), fileOutputStream);
      }
    } catch (IOException ex) {
      throw new RuntimeException(ex);
    }
    cascadeClassifier = new CascadeClassifier(path);
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
