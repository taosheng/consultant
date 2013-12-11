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


  public static void generatePGMFromPic(String file)throws Exception{
    // load an image
    System.out.println("Loading image from " + file);
    IplImage origImg = cvLoadImage(file);

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

    // create temp storage, used during object detection
    CvMemStorage storage = CvMemStorage.create();

    // instantiate a classifier cascade for face detection
    CvHaarClassifierCascade cascade = new CvHaarClassifierCascade(
                                                     cvLoad(CASCADE_FILE));
    System.out.println("Detecting faces...");
    CvSeq faces = cvHaarDetectObjects(equImg, cascade, storage, 1.1, 3, 
                                        CV_HAAR_DO_CANNY_PRUNING);
    cvClearMemStorage(storage);

    // iterate over the faces and draw yellow rectangles around them
    int total = faces.total();
    System.out.println("Found " + total + " face(s)");
    for (int i = 0; i < total; i++) {
      CvRect r = new CvRect(cvGetSeqElem(faces, i));
     // cvRectangle(origImg, cvPoint( r.x()*SCALE, r.y()*SCALE ),    // undo the scaling
     // cvPoint( (r.x() + r.width())*SCALE, (r.y() + r.height())*SCALE ), 
     //                   CvScalar.YELLOW, 6, CV_AA, 0);
     println("width->"+r.width());
     println("height->"+r.height());
     println("x->"+r.x());
     println("y->"+r.y());
      cvSetImageROI(origImg, cvRect(r.x()*SCALE,r.y()*SCALE,r.width()*SCALE,r.height()*SCALE) );
      IplImage smallface=cvCreateImage( cvSize(r.width()*SCALE,r.height()*SCALE), 8, 3 );

      cvCopy(origImg,smallface);
      cvSaveImage(file+"_"+i+".pgm", smallface);
      cvResetImageROI(origImg);

    }

  }
  public static void main(String[] args)throws Exception
  {
    if (args.length != 1) {
      println("generate face pgm files from pictures");
      println("Usage: run FaceDetection <inputfolder>");
      return;
    }

    System.out.println("Starting OpenCV...");

    // preload the opencv_objdetect module to work around a known bug
    Loader.load(opencv_objdetect.class); 
    String path = args[0];

    File folder = new File(path);
    File[] listOfFiles = folder.listFiles(); 
 
    for (int i = 0; i < listOfFiles.length; i++) {
       if (listOfFiles[i].isFile()) {
           String files = listOfFiles[i].getName();
           println(path+"/"+files);
           generatePGMFromPic(path+"/"+files);
       }
    }

    return ;
  }  // end of main()

}  // end of FaceDetection class
