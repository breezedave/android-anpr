package com.DVLA.testapp.app;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.TextureView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.io.IOException;

/**
 * Created by breezed on 06/05/2014.
 */
public class Texture extends Activity implements TextureView.SurfaceTextureListener {
    private Camera mCamera;
    public TextureView texture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void run(TextureView myTexture){
        killHandler = false;
        texture = myTexture;
        texture.setSurfaceTextureListener(this);
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open();

        try {
            mCamera.setPreviewTexture(surface);
        } catch (IOException t) {
            Log.d("Err:","Cannot set Preview Texture");
        }

        mCamera.startPreview();
        texture.setRotation(90.0f);

        mHandler = new Handler();
        mHandler.postDelayed(getTextureBmap ,20);
    }

    Handler mHandler;
    Boolean killHandler=false;
    static public Boolean inUse = false;

    public Runnable getTextureBmap = new Runnable() {
        @Override
        public void run() {
            if(!inUse) {
                Bitmap bmp = texture.getBitmap();
                if(bmp==null){
                    killHandler=true;
                    MainActivity.killHandler=true;
                    return;
                }
                OpenCV opencv = new OpenCV();
                TessBaseAPI tess = new TessBaseAPI();

                Matrix matrix = new Matrix();
                matrix.postRotate(90);
                bmp = Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);

                Bitmap processedBitmap = opencv.imgConvert(bmp);
                if (bmp != null) {
                    new ImgProcess().execute(processedBitmap, bmp, tess);
                } else {
                    Log.d("runnable: ", "bitmap is null");
                }
            }
            if(killHandler == false) {
                mHandler.postDelayed(this, 20);
            }
        }

    };


    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, the Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        killHandler = true;
        MainActivity.killHandler=true;
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}