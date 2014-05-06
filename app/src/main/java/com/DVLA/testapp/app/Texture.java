package com.DVLA.testapp.app;

import android.app.Activity;
import android.graphics.SurfaceTexture;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.TextureView;
import android.widget.FrameLayout;

import java.io.IOException;

/**
 * Created by breezed on 06/05/2014.
 */
public class Texture extends Activity implements TextureView.SurfaceTextureListener {
    private Camera mCamera;
    private TextureView texture;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public void run(TextureView myTexture){
        texture = myTexture;
        Log.d("A", "Loaded Run");
        texture.setSurfaceTextureListener(this);
        Log.d("B","Set Listener");
    }

    @Override
    public void onSurfaceTextureAvailable(SurfaceTexture surface, int width, int height) {
        mCamera = Camera.open();

        Log.d("C","Camera Open");
        Camera.Size previewSize = mCamera.getParameters().getPreviewSize();
        Log.d("D","Preview Size aquired: " + previewSize.width);
        /*
        myTexture.setLayoutParams(new FrameLayout.LayoutParams(
                previewSize.width, previewSize.height, Gravity.CENTER));
        */
        try {
            mCamera.setPreviewTexture(surface);
        } catch (IOException t) {
            Log.d("D1","Cannot set Preview Texture");
        }

        mCamera.startPreview();
        //myTexture.setAlpha(1.0f);
        texture.setRotation(90.0f);
    }

    @Override
    public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
        // Ignored, the Camera does all the work for us
    }

    @Override
    public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
        mCamera.stopPreview();
        mCamera.release();
        return true;
    }

    @Override
    public void onSurfaceTextureUpdated(SurfaceTexture surface) {

    }
}