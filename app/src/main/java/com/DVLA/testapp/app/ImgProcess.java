package com.DVLA.testapp.app;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;

import com.googlecode.tesseract.android.TessBaseAPI;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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


        Bitmap processedBitmap = null;
        processedBitmap = MainActivity.origBmp;

        TessBaseAPI tess = new TessBaseAPI();
        tess.init(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Taxed/", "eng",TessBaseAPI.OEM_TESSERACT_ONLY);
        tess.setDebug(true);
//        tess.setVariable("tessedit_char_whitelist", "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789");
        tess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_OSD);

        String currTxt = "";

        Bitmap chosen = processedBitmap;
        tess.setImage(chosen);
        Log.i("BoxText: ",tess.getBoxText(0));
        String letters = tess.getBoxText(0);

        histCountBox boxCoord = letterCleanse(letters);

        if(boxCoord.volume == 0) {
            Log.i("First Pass","No Letters Found");
            return "Not Found";
        }
        Integer thisX = 0;
        Integer thisY = Math.max(0,boxCoord.y1 -((boxCoord.y2 - boxCoord.y1)/2));
        //Integer thisW = (boxCoord.xMax - boxCoord.xMin);
        Integer thisW = chosen.getWidth();
        Integer thisH =  Math.min(chosen.getHeight()-thisY,(boxCoord.y2 - boxCoord.y1)*2);

        Bitmap histBox = OpenCV.getMini(chosen,thisX, thisY, thisW,thisH);
        MainActivity.currResultBmp = histBox;
        MainActivity.currResultText= "First Sweep";
        histBox = OpenCV.clearFlatColors(histBox, boxCoord.xMin, boxCoord.xMax);

        tess.setImage(histBox);
        letters = tess.getBoxText(0);
        Log.i("Second Sweep: ",letters);
        histCountBox boxCoord2 = letterCleanse(letters);
        histBox = OpenCV.drawRect(histBox,boxCoord2.boxLetterList);
        String result = "";
        for(boxLetter b:boxCoord2.boxLetterList){
            result += b.letter;
        }
        currTxt = result;

        if(currTxt.length()>0){
            MainActivity.currResultBmp = histBox;
            MainActivity.currResultText= currTxt;
            return currTxt;
        }
        return "Not Fnd";
    }

    protected void onPostExecute(String firstResult)
    {
        if(firstResult != null){
            mTextView.setText(firstResult);
            Log.i("Txt: ",firstResult);
        }
        MainActivity.killHandler = true;
    }

    public histCountBox letterCleanse(String letters) {
        List<boxLetter> boxLetters = new ArrayList<boxLetter>();
        for(String ltr:letters.split("\\r?\\n")){
            try {
            boxLetter box = new boxLetter();
            String[] lst = ltr.split(" ");
            box.letter = lst[0];
            box.x1 = Integer.parseInt(lst[1]);
            box.y1 = Integer.parseInt(lst[2]);
            box.x2 = Integer.parseInt(lst[3]);
            box.y2 = Integer.parseInt(lst[4]);
            if(box.y2-box.y1>25){
                boxLetters.add(box);
            }
            } catch (Exception e) {
                Log.i("Err: ",e.getMessage().toString());
            }
        }
        List<histCountBox> histCountList = new ArrayList<histCountBox>();
        histCountList.add(new histCountBox());
        for(boxLetter box:boxLetters){
            Boolean found=false;
            for(histCountBox hist:histCountList) {
                if((box.y2-box.y1) >= (hist.y2-hist.y1) -5 && (box.y2-box.y1) <= (hist.y2-hist.y1) +5) {
                        found = true;
                        hist.volume++;
                        hist.boxLetterList.add(box);
                        if(box.x1<hist.xMin){hist.xMin = box.x1;}
                        if(box.x2>hist.xMax){hist.xMax = box.x2;}
                }
            }
            if(!found){
                histCountBox a = new histCountBox();
                a.y1 = box.y1;
                a.y2 = box.y2;
                a.boxLetterList.add(box);
                histCountList.add(a);
            }
        }
        Collections.sort(histCountList,new boxLetterComapre());

        Log.i("Number of Hist Boxes: ", histCountList.size() + "");
        Log.i("Letters in top Box: ",histCountList.get(0).volume+"");

        return histCountList.get(0);
    }


}
