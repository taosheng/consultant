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
  ArrayList<IplImage> testFaces = new ArrayList<IplImage>();
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
    String trainedOutput = args[2] ;
    String tmpImageOutput = args[3] ;
    String detectImagePath = args[4] ;
    String tmpDetectImageOutput = args[5] ;

    final FaceLearner faceLearner = new FaceLearner();
    
    faceLearner.normalize(sampleImagePath,tmpImageOutput);
    faceLearner.learn(tmpImageOutput);
    faceLearner.storeTrainedData(personName,trainedOutput);
    faceLearner.normalize(detectImagePath, tmpDetectImageOutput);
    faceLearner.recognize(tmpDetectImageOutput, trainedOutput, personName);
  }
  /* From a normalized image file to recognized if match current trained data. 
  */
  public void recognize(String tmpDetectImageOutput, String trainedOutput, String personName){

    File folder = new File(tmpDetectImageOutput);
    File[] listOfFiles = folder.listFiles();
    ArrayList<String> testFaceFileNames = new ArrayList<String>();

    String answer ="";
    // load image to testFaces array list
    for (int i = 0; i < listOfFiles.length; i++) {
       if (listOfFiles[i].isFile()) {
           String file = listOfFiles[i].getName();
           String filepath = tmpDetectImageOutput+"/"+file;
           IplImage tmpImage= cvLoadImage( filepath, CV_LOAD_IMAGE_GRAYSCALE);
           if(tmpImage != null){
             testFaces.add(tmpImage) ;
             testFaceFileNames.add(filepath) ;
           }
       }
    }
  
    CvMat trainPersonNumMat = loadTrainingData(trainedOutput, personName);
    
//    int ntestfaces = testFaces.size() ;
    int nTestFaces = testFaces.size() ;
    LOGGER.info(trainedOutput+"/"+personName+".xml");
    System.out.println("total: "+ nTestFaces+" to be tested latter..." );

    personNumTruthMat = cvCreateMat( 1, nTestFaces, CV_32SC1); // type, 32-

    float[] projectedTestFace = new float[nEigens] ;
    float confidence = 0.0f;
    int nCorrect = 0;
    int nWrong = 0;
    double timeFaceRecognizeStart = (double) cvGetTickCount(); // supposedly to record the timing??
        for (int i = 0; i < nTestFaces; i++) {
          int iNearest;
          int nearest;
          int truth;

          // project the test image onto the PCA subspace
          LOGGER.info("before find decomposite..");
          cvEigenDecomposite(
              testFaces.get(i),
              nEigens, // nEigObjs
              eigenVectArr, // eigInput (Pointer)
              0, // ioFlags
              null, // userData
              pAvgTrainImg, // avg
              projectedTestFace);  // coeffs

      //LOGGER.info("projectedTestFace\n" + floatArrayToString(projectedTestFace));

          final FloatPointer pConfidence = new FloatPointer(confidence);
          LOGGER.info("before find nearest...");
          iNearest = findNearestNeighbor(projectedTestFace, new FloatPointer(pConfidence));
          confidence = pConfidence.get();
          truth = personNumTruthMat.data_i().get(i);
          nearest = trainPersonNumMat.data_i().get(iNearest);


         if (nearest == truth) {
            answer = "Correct";
            nCorrect++;
          } else {
            answer = "WRONG!";
            nWrong++;
          }
         LOGGER.info(testFaceFileNames.get(i));
         LOGGER.info("nearest = " + nearest + ", Truth = " + truth + " (" + answer + "). Confidence = " + confidence);
      }
 

  }
  /**
  * store training data by person name.
  */
  public void storeTrainedData(String personName, String trainedOutput)
    throws Exception{

    CvFileStorage fileStorage;
    String outputXmlFile = trainedOutput+"/"+personName+".xml" ;
    LOGGER.info("writing " + outputXmlFile);
    fileStorage = cvOpenFileStorage(outputXmlFile, null, CV_STORAGE_WRITE,null);
                                 //(filename, memstorage, flags, encoding)...
    cvWriteInt( fileStorage, personName, 1);
             // fileStorage, person name, number of person 
    // store all existing data
 
    cvWriteInt( fileStorage,  "nPersons", 1); 

    cvWriteInt(fileStorage, "nEigens", nEigens);
    LOGGER.info("writing nTrainFaces...");

    cvWriteInt(fileStorage,"nTrainFaces", nTrainFaces);

    LOGGER.info("writing personNumTruthMat...");
    cvWrite( fileStorage, "trainPersonNumMat", personNumTruthMat); 

    cvWrite( fileStorage, "eigenValMat", eigenValMat); 

    cvWrite( fileStorage, "projectedTrainFaceMat", projectedTrainFaceMat);

    cvWrite(fileStorage, "avgTrainImg", pAvgTrainImg); 


    for (int i = 0; i < nEigens; i++) {
      String varname = "eigenVect_" + i;
      cvWrite(
              fileStorage, // fs
              varname, // name
              eigenVectArr[i]); // value
    }

    // release the file-storage interface
    cvReleaseFileStorage(fileStorage);
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

    personNumTruthMat = cvCreateMat(1, nfaces, CV_32SC1);
               // rows ,nFaces,  type, 32-bit unsigned, one channel

      // initialize the person number matrix - for ease of debugging
      for (int j1 = 0; j1 < nfaces; j1++) {
        personNumTruthMat.put(0, j1, 0);
      }

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
  public void normalize(String sampleImagePath, String trainedOutput)throws Exception{
    Loader.load(opencv_objdetect.class);
    String srcPath = sampleImagePath;
    String destPath = trainedOutput;

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




  /** Find the most likely person based on a detection. Returns the index, and stores the confidence value into pConfidence.
   *
   * @param projectedTestFace the projected test face
   * @param pConfidencePointer a pointer containing the confidence value
   * @param iTestFace the test face index
   * @return the index
   */
  private int findNearestNeighbor(float projectedTestFace[], FloatPointer pConfidencePointer) {
    double leastDistSq = Double.MAX_VALUE;
    int i = 0;
    int iTrain = 0;
    int iNearest = 0;

    LOGGER.info("................");
    LOGGER.info("find nearest neighbor from " + nTrainFaces + " training faces");
    for (iTrain = 0; iTrain < nTrainFaces; iTrain++) {
      //LOGGER.info("considering training face " + (iTrain + 1));
      double distSq = 0;

   for (i = 0; i < nEigens; i++) {
        //LOGGER.debug("  projected test face distance from eigenface " + (i + 1) + " is " + projectedTestFace[i]);

        float projectedTrainFaceDistance = (float) projectedTrainFaceMat.get(iTrain, i);
        float d_i = projectedTestFace[i] - projectedTrainFaceDistance;
        distSq += d_i * d_i; // / eigenValMat.data_fl().get(i);  // Mahalanobis distance (might give better results than Eucalidean distance)
//          if (iTrain < 5) {
//            LOGGER.info("    ** projected training face " + (iTrain + 1) + " distance from eigenface " + (i + 1) + " is " + projectedTrainFaceDistance);
//            LOGGER.info("    distance between them " + d_i);
//            LOGGER.info("    distance squared " + distSq);
//          }
      }

      if (distSq < leastDistSq) {
        leastDistSq = distSq;
        iNearest = iTrain;
        LOGGER.info("  training face " + (iTrain + 1) + " is the new best match, least squared distance: " + leastDistSq);
      }
    }

   // Return the confidence level based on the Euclidean distance,
    // so that similar images should give a confidence between 0.5 to 1.0,
    // and very different images should give a confidence between 0.0 to 0.5.
    float pConfidence = (float) (1.0f - Math.sqrt(leastDistSq / (float) (nTrainFaces * nEigens)) / 255.0f);
    pConfidencePointer.put(pConfidence);

    LOGGER.info("training face " + (iNearest + 1) + " is the final best match, confidence " + pConfidence);
    return iNearest;
  }


  /** Opens the training data from the file 'data/facedata.xml'.
   *
   * @param pTrainPersonNumMat
   * @return the person numbers during training, or null if not successful
   */
  private CvMat loadTrainingData(String dataPath, String personName) {
    LOGGER.info("loading training data");
    CvMat pTrainPersonNumMat = null; // the person numbers during training
    CvFileStorage fileStorage;
    int i;

    String dataFilePath = dataPath+"/"+personName+".xml";
    LOGGER.info("loading training data from "+dataFilePath);
    // create a file-storage interface
    fileStorage = cvOpenFileStorage(
            dataFilePath, // filename
            null, // memstorage
            CV_STORAGE_READ, // flags
            null); // encoding
    if (fileStorage == null) {
      LOGGER.severe("Can't open training database file '"+dataFilePath+"'.");
      return null;
    }

    // Load the person names.
    personNames.clear();        // Make sure it starts as empty.
    nPersons = cvReadIntByName( fileStorage, null, personName, 0);
    if (nPersons == 0) {
      LOGGER.severe("No people found in the training database 'data/facedata.xml'.");
      return null;
    } else {
      LOGGER.info(nPersons + " persons read from the training database");
    }

    // Load each person's name.
    for (i = 0; i < nPersons; i++) {
      String sPersonName;
      String varname = "personName_" + (i + 1);
      sPersonName = cvReadStringByName(
              fileStorage, // fs
              null, // map
              varname,
              "");
      personNames.add(sPersonName);
    }
    LOGGER.info("person names: " + personNames);

   // Load the data
    nEigens = cvReadIntByName(
            fileStorage, // fs
            null, // map
            "nEigens",
            0); // default_value
    nTrainFaces = cvReadIntByName(
            fileStorage,
            null, // map
            "nTrainFaces",
            0); // default_value
    Pointer pointer = cvReadByName(
            fileStorage, // fs
            null, // map
            "trainPersonNumMat"); // name
    pTrainPersonNumMat = new CvMat(pointer);

    pointer = cvReadByName(
            fileStorage, // fs
            null, // map
            "eigenValMat"); // name
    eigenValMat = new CvMat(pointer);

    pointer = cvReadByName(
            fileStorage, // fs
            null, // map
            "projectedTrainFaceMat"); // name
    projectedTrainFaceMat = new CvMat(pointer);
   pointer = cvReadByName(
            fileStorage,
            null, // map
            "avgTrainImg");
    pAvgTrainImg = new IplImage(pointer);

    eigenVectArr = new IplImage[nTrainFaces];
    for (i = 0; i <= nEigens; i++) {
      String varname = "eigenVect_" + i;
      pointer = cvReadByName(
              fileStorage,
              null, // map
              varname);
      eigenVectArr[i] = new IplImage(pointer);
    }

    // release the file-storage interface
    cvReleaseFileStorage(fileStorage);

    LOGGER.info("Training data loaded (" + nTrainFaces + " training images of " + nPersons + " people)");
    final StringBuilder stringBuilder = new StringBuilder();
    stringBuilder.append("People: ");
    if (nPersons > 0) {
      stringBuilder.append("<").append(personNames.get(0)).append(">");
    }
    for (i = 1; i < nPersons; i++) {
      stringBuilder.append(", <").append(personNames.get(i)).append(">");
    }
    LOGGER.info(stringBuilder.toString());

    return pTrainPersonNumMat;
  }
}

