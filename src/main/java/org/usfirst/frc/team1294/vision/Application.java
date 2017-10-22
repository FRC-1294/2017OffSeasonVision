package org.usfirst.frc.team1294.vision;

import io.vertx.core.Vertx;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class Application {

  public static void main(String[] args) {
    loadNativeOpenCvLibraries();
    Vertx vertx = Vertx.vertx();
    vertx.deployVerticle(new ComputerVisionVerticle());
    vertx.deployVerticle(new MJpegStreamerVerticle());
  }

  private static void loadNativeOpenCvLibraries() {
    try {
      String osname = System.getProperty("os.name");
      String resname = "/opencv/";
      if (osname.startsWith("Windows"))
        resname += "Windows/" + System.getProperty("os.arch") + "/";
      else
        resname += osname + "/" + System.getProperty("os.arch") + "/";

      if (System.getProperty("os.name").startsWith("Linux") && new File("/usr/lib/arm-linux-gnueabihf").exists()) {
        resname += "hf/";
      }
      System.out.println(resname);
      if (osname.startsWith("Windows"))
        resname += "opencv_java310.dll";
      else if (osname.startsWith("Mac"))
        resname += "libopencv_java310.jnilib";
      else
        resname += "libopencv_java310.so";

      InputStream is = Application.class.getResourceAsStream(resname);
      if (is == null) {
        if (new File("./" + resname).exists()) {
          is = new FileInputStream("./" + resname);
        } else if (new File("./src/main/resources/" + resname).exists()) {
          is = new FileInputStream("./src/main/resources/" + resname);
        }
      }

      File jniLibrary;
      if (is != null) {
        // create temporary file
        if (System.getProperty("os.name").startsWith("Windows"))
          jniLibrary = File.createTempFile("opencv_java310", ".dll");
        else if (System.getProperty("os.name").startsWith("Mac"))
          jniLibrary = File.createTempFile("opencv_java310", ".dylib");
        else
          jniLibrary = File.createTempFile("opencv_java310", ".so");
        // flag for delete on exit
        jniLibrary.deleteOnExit();
        OutputStream os = new FileOutputStream(jniLibrary);

        byte[] buffer = new byte[1024];
        int readBytes;
        try {
          while ((readBytes = is.read(buffer)) != -1) {
            os.write(buffer, 0, readBytes);
          }
        } finally {
          os.close();
          is.close();
        }

        System.load(jniLibrary.getAbsolutePath());
      } else {
        System.loadLibrary("opencv_java310");
      }
      System.out.println("Successfully loaded opencv native libraries.");
    } catch (IOException ex) {
      ex.printStackTrace();
      System.exit(1);
    }
  }

}
