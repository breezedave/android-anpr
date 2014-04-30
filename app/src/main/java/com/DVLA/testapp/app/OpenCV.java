package com.DVLA.testapp.app;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.core.MatOfFloat;
import org.opencv.core.MatOfInt;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Core;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.features2d.KeyPoint;
import org.opencv.highgui.Highgui;
import org.opencv.imgproc.Imgproc;


import org.opencv.features2d.Features2d;
import org.opencv.objdetect.*;



import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;
import android.util.Log;
import android.view.MenuItem;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;

public class OpenCV extends Activity {
    private static final String  TAG                 = "OCVSample::Activity";

    public static final int      VIEW_MODE_RGBA      = 0;
    public static final int      VIEW_MODE_HIST      = 1;
    public static final int      VIEW_MODE_CANNY     = 2;
    public static final int      VIEW_MODE_SEPIA     = 3;
    public static final int      VIEW_MODE_SOBEL     = 4;
    public static final int      VIEW_MODE_ZOOM      = 5;
    public static final int      VIEW_MODE_PIXELIZE  = 6;
    public static final int      VIEW_MODE_POSTERIZE = 7;

    private MenuItem             mItemPreviewRGBA;
    private MenuItem             mItemPreviewHist;
    private MenuItem             mItemPreviewCanny;
    private MenuItem             mItemPreviewSepia;
    private MenuItem             mItemPreviewSobel;
    private MenuItem             mItemPreviewZoom;
    private MenuItem             mItemPreviewPixelize;
    private MenuItem             mItemPreviewPosterize;
    private CameraBridgeViewBase mOpenCvCameraView;

    private Size                 mSize0;

    private Mat                  mIntermediateMat;
    private Mat                  mMat0;
    private MatOfInt             mChannels[];
    private MatOfInt             mHistSize;
    private int                  mHistSizeNum = 25;
    private MatOfFloat           mRanges;
    private Scalar               mColorsRGB[];
    private Scalar               mColorsHue[];
    private Scalar               mWhilte;
    private Point                mP1;
    private Point                mP2;
    private float                mBuff[];
    private Mat                  mSepiaKernel;

    public static int           viewMode = VIEW_MODE_RGBA;

    //protected void imgConvert(String imgLoc,CascadeClassifier cascade) {
    protected Bitmap imgConvert(String imgLoc) {
        //Scalar min = new Scalar(0, 0, 0, 140);//BGR-A
        //Scalar max= new Scalar(0, 0, 0, 200);//BGR-A

        Mat image;
        List<String> listReg = new ArrayList<String>();
        Mat processedImg = new Mat();
        image = Highgui.imread(imgLoc);
        Mat gray_image = new Mat();
        Size shrankSize = new Size();
        shrankSize.height = 1500;
        shrankSize.width = (float) shrankSize.height / image.height() * image.width();
        if(image.height() > 1500) { Imgproc.resize(image, image, shrankSize);}

        Imgproc.cvtColor(image,gray_image,Imgproc.COLOR_RGB2GRAY);
        //Imgproc.GaussianBlur(image,image,new Size(7,7),3);
        //Imgproc.equalizeHist(image,image);
        Imgproc.threshold(gray_image,gray_image,127,255,0);
        Bitmap bmp = Bitmap.createBitmap(gray_image.width(),gray_image.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(gray_image,bmp);
        return bmp;
     }

    static public Bitmap getMini(Bitmap bmp,Integer x,Integer y,Integer w,Integer h) {
        Mat mat = new Mat();
        Bitmap bmp2 = Bitmap.createBitmap(w,h, Bitmap.Config.ARGB_8888);
        Utils.bitmapToMat(bmp,mat);
        Mat mat2 = mat.submat(y,y+h,x,x+w);
        Utils.matToBitmap(mat2,bmp2);
        return bmp2;
    }

    // Not currently in use. Saved for later.
    public void faceDetection(Mat srcGray, Mat src,String imgLoc,CascadeClassifier cascade) {
       MatOfRect storage = new MatOfRect();
        Log.i("Starting Detection",new DateTime().toString());
        cascade.detectMultiScale(srcGray,storage);
        Log.i("Finished Detection",new DateTime().toString());
        int total_Faces = storage.toList().size();
        Log.i("Total potential matches",storage.toList().toString());

        List<Rect> x = new ArrayList<Rect>();
        for(int i = 0; i < total_Faces; i++){
            Rect r = storage.toList().get(i);
            x.add(0,r);
        }
        Collections.sort(x,new customCompare());
        Integer i = 0;
        for(i=0;i<x.size();i++){
            Rect r = x.get(i);
            Core.rectangle(src,new Point(r.x,r.y),new Point(r.x+r.width,r.y+r.height),new Scalar(255,0,0),2);
        }

        //removed obj only code for time being
        //src = src.submat(r);

        Highgui.imwrite(imgLoc, src);
    }
}
