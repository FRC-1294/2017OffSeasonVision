package org.usfirst.frc.team1294.vision;

import com.google.common.io.Resources;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.DMatch;
import org.opencv.core.KeyPoint;
import org.opencv.core.Mat;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.List;
import java.util.Vector;

public class Features2DPlusHomographyTargetDetector extends TargetDetector {

  private final Mat objectImage;
  private final Mat sceneImage;

  public Features2DPlusHomographyTargetDetector() {
    final String path = Resources.getResource("PiBox.jpg").getPath();
    System.out.println(path);
    final Mat tmpObjectImage = Imgcodecs.imread(path);
    objectImage = new Mat();
    Imgproc.cvtColor(tmpObjectImage, objectImage, Imgproc.COLOR_BGRA2GRAY);

    sceneImage = new Mat();
  }

  @Override
  protected Rect[] doDetection(Mat image) {
    Imgproc.cvtColor(image, sceneImage, Imgproc.COLOR_BGRA2GRAY);

    FeatureDetector detector = FeatureDetector.create(FeatureDetector.SURF);
    DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.SURF);
    DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.FLANNBASED);


    // -- Step 1: Detect the keypoints using Detector
    MatOfKeyPoint keypoints_object = new MatOfKeyPoint();
    MatOfKeyPoint keypoints_scene = new MatOfKeyPoint();
    detector.detect(objectImage, keypoints_object);
    detector.detect(sceneImage, keypoints_scene);

    // -- Step 2: Calculate descriptors (feature vectors)
    Mat descriptors_object = new Mat();
    Mat descriptors_scene = new Mat();
    extractor.compute(objectImage, keypoints_object, descriptors_object);
    extractor.compute(sceneImage, keypoints_scene, descriptors_scene);

    // -- Step 3: Matching descriptor vectors using matcher
    MatOfDMatch matches = new MatOfDMatch();
    matcher.match(descriptors_object, descriptors_scene, matches);

    List<DMatch> matchesList = matches.toList();
    double max_dist = 0;
    double min_dist = 100;
    // -- Quick calculation of max and min distances between keypoints
    for (int i = 0; i < descriptors_object.rows(); i++) {
      double dist = matchesList.get(i).distance;
      if (dist < min_dist) {
        min_dist = dist;
      }
      if (dist > max_dist) {
        max_dist = dist;
      }
    }

    // -- Draw only "good" matches (i.e. whose distance is less than 3*min_dist )
    Vector<DMatch> good_matches = new Vector<DMatch>();
    for (int i = 0; i < descriptors_object.rows(); i++) {
      if (matchesList.get(i).distance < 3 * min_dist) {
        good_matches.add(matchesList.get(i));
      }
    }

    List<Point> objListGoodMatches = new ArrayList<Point>();
    List<Point> sceneListGoodMatches = new ArrayList<Point>();

    List<KeyPoint> keypoints_objectList = keypoints_object.toList();
    List<KeyPoint> keypoints_sceneList = keypoints_scene.toList();

    for (int i = 0; i < good_matches.size(); i++) {
      // -- Get the keypoints from the good matches
      objListGoodMatches.add(keypoints_objectList.get(good_matches.get(i).queryIdx).pt);
      sceneListGoodMatches.add(keypoints_sceneList.get(good_matches.get(i).trainIdx).pt);
      Imgproc.circle(image, new Point(sceneListGoodMatches.get(i).x, sceneListGoodMatches.get(i).y), 3, new Scalar( 255, 0, 0, 255));


    }
    String text = "Good Matches Count: " + good_matches.size();
    Imgproc.putText(image, text, new Point(0,60), Core.FONT_HERSHEY_COMPLEX_SMALL, 1, new Scalar(0, 0, 255, 255));


    MatOfPoint2f objListGoodMatchesMat = new MatOfPoint2f();
    objListGoodMatchesMat.fromList(objListGoodMatches);
    MatOfPoint2f sceneListGoodMatchesMat = new MatOfPoint2f();
    sceneListGoodMatchesMat.fromList(sceneListGoodMatches);

    // findHomography needs 4 corresponding points
    if(good_matches.size()>3){


      Mat H = Calib3d.findHomography(objListGoodMatchesMat, sceneListGoodMatchesMat, Calib3d.RANSAC, 5 /* RansacTreshold */);

      Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
      Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

      obj_corners.put(0, 0, new double[] { 0, 0 });
      obj_corners.put(1, 0, new double[] { sceneImage.cols(), 0 });
      obj_corners.put(2, 0, new double[] { sceneImage.cols(), sceneImage.rows() });
      obj_corners.put(3, 0, new double[] { 0, sceneImage.rows() });

      Core.perspectiveTransform(obj_corners, scene_corners, H);

      Imgproc.line(image, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(0, 255, 0), 2);
      Imgproc.line(image, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(0, 255, 0), 2);
      Imgproc.line(image, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(0, 255, 0), 2);
      Imgproc.line(image, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(0, 255, 0), 2);

    }

      return new Rect[0];
  }
}
