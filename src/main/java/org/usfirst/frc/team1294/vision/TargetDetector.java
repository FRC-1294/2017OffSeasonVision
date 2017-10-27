package org.usfirst.frc.team1294.vision;

import org.opencv.core.Mat;
import org.opencv.core.Rect;

public abstract class TargetDetector {
  protected abstract Rect[] doDetection(final Mat image);
}
