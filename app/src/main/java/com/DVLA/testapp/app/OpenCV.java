package com.DVLA.testapp.app;

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
import android.util.Log;
import android.view.MenuItem;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

    protected void hsvConvert(String imgLoc,CascadeClassifier cascade) {
        Mat image;
        image = Highgui.imread(imgLoc);
        faceDetection(image,imgLoc,cascade);
        //Mat gray_image = new Mat();
        //Imgproc.cvtColor(image, gray_image, Imgproc.COLOR_RGB2GRAY);
        //Log.i("Gray","It's Gray Now");
        //Highgui.imwrite(imgLoc, gray_image);
     }

    public void faceDetection(Mat src,String imgLoc,CascadeClassifier cascade) {
       MatOfRect storage = new MatOfRect();

        cascade.detectMultiScale(src,storage); 

        int total_Faces = storage.toList().size();
        Log.i("Total Face",storage.toList().toString());

        List<Rect> x = new ArrayList<Rect>();
        for(int i = 0; i < total_Faces; i++){
            Rect r = storage.toList().get(i);
            x.add(0,r);
        }
        Collections.sort(x,new customCompare());
        Rect r = x.get(0);
        Core.rectangle(src,new Point(r.x,r.y),new Point(r.x+r.width,r.y+r.height),new Scalar(255,0,0),10);
        Highgui.imwrite(imgLoc, src);
    }
}
