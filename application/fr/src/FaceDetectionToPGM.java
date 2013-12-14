import java.io.*;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacpp.Loader;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;


public class FaceDetectionToPGM
{
  private static final int SCALE = 2;     
  // scaling factor to reduce size of input image

  // cascade definition for face detection
  private static final String CASCADE_FILE = "haarcascade_frontalface_alt.xml";

  private static final String OUT_FILE = "mf";


  private static void println(Object msg)throws Exception{
      System.out.println(msg);
  }


  public static void generatePGMFromPic(String srcPath, String file, String destPath)throws Exception{
    String srcFilePath = srcPath+"/"+file; 
    System.out.println("Loading image from " + srcFilePath);
    IplImage origImg = cvLoadImage(srcFilePath);

    // convert to grayscale
    IplImage grayImg = IplImage.create(origImg.width(),
                                         origImg.height(), IPL_DEPTH_8U, 1);
    cvCvtColor(origImg, grayImg, CV_BGR2GRAY);  

    // scale the grayscale (to speed up face detection)
    IplImage smallImg = IplImage.create(grayImg.width()/SCALE, 
                                        grayImg.height()/SCALE, IPL_DEPTH_8U, 1);
    cvResize(grayImg, smallImg, CV_INTER_LINEAR);

    // equalize the small grayscale
    IplImage equImg = IplImage.create(smallImg.width(), 
                                      smallImg.height(), IPL_DEPTH_8U, 1);
          cvEqualizeHist(smallImg, equImg);

    CvMemStorage storage = CvMemStorage.create();

    CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(
                                                     cvLoad(CASCADE_FILE));
    System.out.println("Detecting faces...");
    CvSeq faces = cvHaarDetectObjects(equImg, cascade, storage, 1.1, 3, 
                                        CV_HAAR_DO_CANNY_PRUNING);
    cvClearMemStorage(storage);

    int total = faces.total();
    System.out.println("Found " + total + " face(s)");
    for (int i = 0; i < total; i++) {
      CvRect r = new CvRect(cvGetSeqElem(faces, i));
     println("width->"+r.width());
     println("height->"+r.height());
     println("x->"+r.x());
     println("y->"+r.y());
      cvSetImageROI(origImg, cvRect(r.x()*SCALE,r.y()*SCALE,r.width()*SCALE,r.height()*SCALE) );
      IplImage origface=cvCreateImage( cvSize(r.width()*SCALE,r.height()*SCALE), 8, 3 );

      IplImage smallface=cvCreateImage( cvSize(120,120), 8, 3 );
      cvCopy(origImg,origface);
      cvResize(origface, smallface, CV_INTER_LINEAR);
      cvSaveImage(destPath+"/"+file+i+".pgm", smallface);
      cvResetImageROI(origImg);

    }

  }
  public static void main(String[] args)throws Exception
  {
    if (args.length < 2) {
      println("generate face pgm files from pictures");
      println("Usage: run FaceDetection <inputfolder> <outputfolder>");
      return;
    }

    System.out.println("Starting OpenCV...");

    // preload the opencv_objdetect module to work around a known bug
    Loader.load(opencv_objdetect.class); 
    String srcPath = args[0];
    String destPath = args[1];

    File folder = new File(srcPath);
    File[] listOfFiles = folder.listFiles(); 
 
    for (int i = 0; i < listOfFiles.length; i++) {
       if (listOfFiles[i].isFile()) {
           String files = listOfFiles[i].getName();
           println(srcPath+"/"+files);
           generatePGMFromPic(srcPath, files,destPath);
       }
    }

    return ;
  }  // end of main()

}  // end of FaceDetection class
