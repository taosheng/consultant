import java.io.*;
import java.util.* ;
import java.util.logging.Logger;

import com.googlecode.javacv.*;
import com.googlecode.javacv.cpp.*;
import com.googlecode.javacpp.Loader;
import com.googlecode.javacpp.FloatPointer;
import com.googlecode.javacpp.Pointer;

import static com.googlecode.javacv.cpp.opencv_core.*;
import static com.googlecode.javacv.cpp.opencv_imgproc.*;
import static com.googlecode.javacv.cpp.opencv_highgui.*;
import static com.googlecode.javacv.cpp.opencv_objdetect.*;
import static com.googlecode.javacv.cpp.opencv_legacy.*;

/**
 * Tool library
 * Lear faces and store to opencv standard xml file
 * @author taosheng.chen@gmail.com
 */
public class FaceLearner {

  private static final int SCALE = 2;
  private static final Logger LOGGER = Logger.getLogger(FaceLearner.class.getName());
  private static final String CASCADE_FILE = "haarcascade_frontalface_alt.xml";


  /** the number of training faces */
  private int nTrainFaces = 0;
  /** the training face image array */
  IplImage[] trainingFaceImgArr;
  ArrayList<IplImage> trainingFaces = new ArrayList<IplImage>();
  /** the test face image array */
  IplImage[] testFaceImgArr;
  /** the person number array */
  CvMat personNumTruthMat;
  /** the number of persons */
  int nPersons;
  /** the person names */
  final List<String> personNames = new ArrayList<String>();
  /** the number of eigenvalues */
  int nEigens = 0;
  /** eigenvectors */
  IplImage[] eigenVectArr;
  ArrayList<IplImage> eigens = new ArrayList<IplImage>();
  /** eigenvalues */
  CvMat eigenValMat;
  /** the average image */
  IplImage pAvgTrainImg;
  /** the projected training faces */
  CvMat projectedTrainFaceMat;

  public static void main(final String[] args) throws Exception{

    String personName = args[0] ;
    String sampleImagePath = args[1] ;
    String trainedDataOutput = args[2] ;
    String tmpImageDataOutput = args[3] ;

    final FaceLearner faceLearner = new FaceLearner();
    
    faceLearner.normalize(sampleImagePath,tmpImageDataOutput);
    faceLearner.learn(tmpImageDataOutput);
//    faceLearner.storeTrainedData(trainedDataOutput);
    //faceRecognition.recognizeFileList("data/some-test-faces.txt");
    //faceRecognition.recognizeFileList(args[1]);
  }

  /**
  * 
  */
  public void learn(String tmpImageDataOutput){

    File folder = new File(tmpImageDataOutput);
    File[] listOfFiles = folder.listFiles();
    // load image to trainingFaces
    for (int i = 0; i < listOfFiles.length; i++) {
       if (listOfFiles[i].isFile()) {
           String file = listOfFiles[i].getName();
           String filepath = tmpImageDataOutput+"/"+file;
           IplImage tmpImage= cvLoadImage( filepath, CV_LOAD_IMAGE_GRAYSCALE);
           if(tmpImage != null){
             trainingFaces.add(tmpImage) ;
           }
       }
    }
    int nfaces = trainingFaces.size() ;
    System.out.println("total: "+ nfaces );
    // Does the Principal Component Analysis, finding the average image and the eigenfaces that represent any image in the given dataset.....so call PCA
      // set the number of eigenvalues to use
    nTrainFaces = nfaces;
    nEigens = nTrainFaces - 1;
    CvTermCriteria calcLimit;
    CvSize faceImgSize = new CvSize();
    faceImgSize.width(trainingFaces.get(0).width());
    faceImgSize.height(trainingFaces.get(0).height());


    for (int i = 0; i < nEigens; i++) {
      eigens.add( cvCreateImage(
              faceImgSize, // size
              IPL_DEPTH_32F, // depth
              1)); // channels)
    }
 
    eigenValMat = cvCreateMat( 1, nEigens, CV_32FC1); // type, 32-bit float, 1 channel
    // allocate the averaged image
    pAvgTrainImg = cvCreateImage(
            faceImgSize, // size
            IPL_DEPTH_32F, // depth
            1); // channels

    // set the PCA termination criterion
    calcLimit = cvTermCriteria(
            CV_TERMCRIT_ITER, // type
            nEigens, // max_iter
            1); // epsilon

     LOGGER.info("computing average image, eigenvalues and eigenvectors");
    // compute average image, eigenvalues, and eigenvectors
    eigenVectArr = eigens.toArray(new IplImage[eigens.size()]);
    cvCalcEigenObjects(
            nTrainFaces, // nObjects
            trainingFaces.toArray(new IplImage[trainingFaces.size()]), // input
            eigenVectArr, // the output is array... need to transfer to arrayList latter
            CV_EIGOBJ_NO_CALLBACK, // ioFlags
            0, // ioBufSize
            null, // userData
            calcLimit,
            pAvgTrainImg, // avg
            eigenValMat.data_fl()); // eigVals
    
    // eigens = (ArrayList) Arrays.asList(eigenVectArr)  ;

    LOGGER.info("normalizing the eigenvectors");
    cvNormalize(
            eigenValMat, // src (CvArr)
            eigenValMat, // dst (CvArr)
            1, // a
            0, // b
            CV_L1, // norm_type
            null); // mask



    LOGGER.info("projecting the training images onto the PCA subspace");
    // project the training images onto the PCA subspace
    projectedTrainFaceMat = cvCreateMat(
            nTrainFaces, // rows
            nEigens, // cols
            CV_32FC1); // type, 32-bit float, 1 channel

    // initialize the training face matrix - for ease of debugging
    for (int i1 = 0; i1 < nTrainFaces; i1++) {
      for (int j1 = 0; j1 < nEigens; j1++) {
        projectedTrainFaceMat.put(i1, j1, 0.0);
      }
    }


    LOGGER.info("created projectedTrainFaceMat with " + nTrainFaces + " (nTrainFaces) rows and " + nEigens + " (nEigens) columns");
/** nTrainFace should always > 5 !!!!!!!!!!!!!!!!
    if (nTrainFaces < 5) {
      LOGGER.info("projectedTrainFaceMat contents:\n" + oneChannelCvMatToString(projectedTrainFaceMat));
    }
*/
   final FloatPointer floatPointer = new FloatPointer(nEigens);
    for (int i = 0; i < nTrainFaces; i++) {
      cvEigenDecomposite(
              trainingFaces.get(i), // obj
              nEigens, // nEigObjs
              eigenVectArr, // eigInput (Pointer)
              0, // ioFlags
              null, // userData (Pointer)
              pAvgTrainImg, // avg
              floatPointer); // coeffs (FloatPointer)

/*nTrainFace should always > 5 !!!!!!!!!!!!!!!!
      if (nTrainFaces < 5) {
        LOGGER.info("floatPointer: " + floatPointerToString(floatPointer));
      }*/
      for (int j1 = 0; j1 < nEigens; j1++) {
        projectedTrainFaceMat.put(i, j1, floatPointer.get(j1));
      }
    }
/*nTrainFace should always > 5 !!!!!!!!!!!!!!!!
    if (nTrainFaces < 5) {
      LOGGER.info("projectedTrainFaceMat after cvEigenDecomposite:\n" + projectedTrainFaceMat);
    }
*/
    // store the recognition data as an xml file
//    storeTrainingData();

    // Save all the eigenvectors as images, so that they can be checked.
  //  storeEigenfaceImages();



  }

  /** normalized to 120*120 PGM files..*/
  public void normalize(String sampleImagePath, String trainedDataOutput)throws Exception{
    Loader.load(opencv_objdetect.class);
    String srcPath = sampleImagePath;
    String destPath = trainedDataOutput;

    File folder = new File(srcPath);
    File[] listOfFiles = folder.listFiles();

    for (int i = 0; i < listOfFiles.length; i++) {
       if (listOfFiles[i].isFile()) {
           String files = listOfFiles[i].getName();
           generatePGMFromPic(srcPath, files,destPath);
       }
    }
  }
  public void generatePGMFromPic(String srcPath, String file, String destPath)throws Exception{

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
      cvSetImageROI(origImg, cvRect(r.x()*SCALE,r.y()*SCALE,r.width()*SCALE,r.height()*SCALE) );
      IplImage origface=cvCreateImage( cvSize(r.width()*SCALE,r.height()*SCALE), 8, 3 );

      IplImage smallface=cvCreateImage( cvSize(120,120), 8, 3 );
      cvCopy(origImg,origface);
      cvResize(origface, smallface, CV_INTER_LINEAR);
      cvSaveImage(destPath+"/"+file+i+".pgm", smallface);
      cvResetImageROI(origImg);

    }



  }

}
