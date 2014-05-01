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
        processedBitmap = MainActivity.currResultBmp;

        TessBaseAPI tess = new TessBaseAPI();
        tess.init(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Taxed/", "eng",TessBaseAPI.OEM_TESSERACT_ONLY);
        tess.setDebug(true);
        tess.setVariable("tessedit_char_whitelist", "ABCDEFGHJKLMNPQRSTUVWXYZ0123456789");
        tess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_SINGLE_COLUMN);

        Integer fileWidth = processedBitmap.getWidth();
        Integer fileHeight = processedBitmap.getHeight();

        Integer x,y,w,h,i;
        String recognizedText = "";

        //Split method
        Integer chosenBit =0;
        Integer currMaxLen = 0;
        String currTxt = "";
        /*
        for(i=0;i<3;i++){
            Bitmap third = OpenCV.getMini(processedBitmap,0,fileHeight/4 * i,fileWidth,fileHeight/2);
            tess.setImage(third);
            String txt = tess.getUTF8Text();
            Integer txtLen = txt.length();
            Log.i("Third"+i,txt);
            MainActivity.currResultBmp = third;
            MainActivity.currResultText= "Text: "+txt;
            if(txtLen >currMaxLen){
                currMaxLen = txtLen;
                chosenBit = i;
            }
        }
        Bitmap chosen = OpenCV.getMini(processedBitmap,0,fileHeight/4 * chosenBit,fileWidth,fileHeight/2);
        */

        Bitmap chosen = processedBitmap;
        tess.setImage(chosen);
        currTxt = tess.getUTF8Text();
        Log.i("Txt: ",currTxt);
        String letters = tess.getBoxText(0);

        histCountBox boxCoord = letterCleanse(letters);

        MainActivity.currResultBmp = chosen;
        MainActivity.currResultText= currTxt;

        //Integer thisX = Math.max(0,boxCoord.xMin*2/3);
        Integer thisX = 0;
        Integer thisY = Math.max(0,boxCoord.y1);
        //Integer thisW = (boxCoord.xMax - boxCoord.xMin);
        Integer thisW = processedBitmap.getWidth();
        Integer thisH =  (boxCoord.y2 - boxCoord.y1)*2;
        Log.i("histBox: ",thisX + " " + thisY + " " + thisW + " " + thisH);

        Bitmap histBox = OpenCV.getMini(processedBitmap,thisX, thisY, thisW,thisH);
        histBox = OpenCV.clearFlatColors(histBox, boxCoord.xMin, boxCoord.xMax);
        MainActivity.currResultBmp = histBox;
        MainActivity.currResultText= currTxt;

        tess.setImage(histBox);
        letters = tess.getBoxText(0);

        histCountBox boxCoord2 = letterCleanse(letters);
        histBox = OpenCV.drawRect(histBox,boxCoord2.boxLetterList);
        String result = "";
        for(boxLetter b:boxCoord2.boxLetterList){
            result += b.letter;
        }
        currTxt = result;

        Log.i("Txt: ",currTxt);
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
        }
        MainActivity.killHandler = true;
    }

    public histCountBox letterCleanse(String letters) {
        List<boxLetter> boxLetters = new ArrayList<boxLetter>();
        for(String ltr:letters.split("\\r?\\n")){
            //Log.i("boxLetters: ",ltr);
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
        }
        List<histCountBox> histCountList = new ArrayList<histCountBox>();
        histCountList.add(new histCountBox());
        for(boxLetter box:boxLetters){
            Boolean found=false;
            for(histCountBox hist:histCountList) {
                if(box.y1 * 1.0 >= hist.y1 * -1.02 && box.y1 * 1.0 <= hist.y1 * 1.02) {
                    if(box.y2 * 1.0 >= hist.y2 * -1.02 && box.y2 * 1.0 <= hist.y2 * 1.02) {
                        found = true;
                        hist.volume++;
                        hist.boxLetterList.add(box);
                        if(box.x1<hist.xMin){hist.xMin = box.x1;}
                        if(box.x2>hist.xMax){hist.xMax = box.x2;}
                    }
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

        Log.i("histBOxCOunt: ", histCountList.size() + "");
        Log.i("histBox: ",histCountList.get(0).volume+"");

        return histCountList.get(0);
    }


}
