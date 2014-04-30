package com.DVLA.testapp.app;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by breezed on 29/04/14.
 */
public class ImgProcess extends AsyncTask<Object,String,String> {
    ImageView mImageView;
    TextView mTextView;

    @Override
    protected String doInBackground(Object... params)
    {
        MainActivity.killHandler = false;
        this.mImageView = (ImageView) params[0];
        this.mTextView = (TextView) params[1];

        List<String> found = new ArrayList<String>();
        Bitmap processedBitmap = MainActivity.currResultBmp;

        TessBaseAPI tess = new TessBaseAPI();
        tess.init(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Taxed/", "eng",TessBaseAPI.OEM_TESSERACT_ONLY);
        tess.setDebug(true);
        tess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_LINE);

        Integer fileWidth = processedBitmap.getWidth();
        Integer fileHeight = processedBitmap.getHeight();

        Integer x,y,w,h;
        String recognizedText = "";

        for(w=390;w<391;w+= 20){
            for(h=100;h<101;h+= 20){
                for(x=0; x < fileWidth - w; x += 5) {
                    for(y=0;y < fileHeight - h;y += 5) {
                        Bitmap bmp = OpenCV.getMini(processedBitmap,x,y,w,h);
                        tess.setImage(bmp);
                        recognizedText = tess.getUTF8Text();
                        Pattern pattern = Pattern.compile("^[0-Z]{4,7}$");
                        Matcher matcher = pattern.matcher(recognizedText);
                        if(matcher.matches()) {
                            Log.i("found: ", "" + found.size());
                            found.add(recognizedText);
                            MainActivity.currResultBmp = bmp;
                            MainActivity.currResultText = recognizedText;
                        } else {
                            MainActivity.currResultBmp = bmp;
                            MainActivity.currResultText = "";
                        }
                    }
                }
            }
        }
        for(String foundStr: found) {
            Log.d("Found: ",foundStr);
        }

        tess.end();
        return found.get(0).toString();
    }
    protected void onPostExecute(String firstResult)
    {
        MainActivity.killHandler = true;
        mTextView.setText(firstResult);
    }
}
