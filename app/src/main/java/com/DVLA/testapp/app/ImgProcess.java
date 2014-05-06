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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by breezed on 29/04/14.
 */
public class ImgProcess extends AsyncTask<Object,String,String> {
    Bitmap bmp;
    Bitmap processedBitmap;
    TessBaseAPI tess;

    @Override
    protected String doInBackground(Object... params)
    {
        Texture.inUse = true;
        MainActivity.killHandler = false;
        this.processedBitmap = (Bitmap) params[0];
        this.bmp = (Bitmap) params[1];
        this.tess = (TessBaseAPI) params[2];
        TessBaseAPI tess = this.tess;

        tess.init(Environment.getExternalStorageDirectory().getAbsolutePath() + "/Taxed/", "eng",TessBaseAPI.OEM_TESSERACT_ONLY);
        tess.setDebug(true);
        tess.setPageSegMode(TessBaseAPI.PageSegMode.PSM_AUTO_ONLY);

        String currTxt = "";

        Bitmap bmp = this.bmp;
        Bitmap chosen = this.processedBitmap;
        tess.setImage(chosen);
        String letters = tess.getBoxText(0);
        histCountBox boxCoord = letterCleanse(letters);

        if(boxCoord.volume == 0) {
            MainActivity.currResultBmp = bmp;
            Log.d("imgP:","Not Found");
            return "Not Found";
        }
        Integer thisX = 0;
        Integer thisY = Math.max(0,boxCoord.y1 -5);
        //Integer thisW = (boxCoord.xMax - boxCoord.xMin);
        Integer thisW = chosen.getWidth();
        Integer thisH =  Math.min(chosen.getHeight()-thisY,(boxCoord.y2 - boxCoord.y1) +10);

        Bitmap histBox = OpenCV.getMini(chosen,thisX, thisY, thisW,thisH);
        //histBox = OpenCV.clearFlatColors(histBox, boxCoord.xMin, boxCoord.xMax);

        tess.setImage(histBox);
        letters = tess.getBoxText(0);
        histCountBox boxCoord2 = letterCleanse(letters);
        histBox = OpenCV.drawRect(bmp,boxCoord2.boxLetterList,thisX,thisY);
        String result = "";
        for(boxLetter b:boxCoord2.boxLetterList){
            result += b.letter;
        }
        currTxt = result;

        if(currTxt.length()>0){
            Pattern pattern = Pattern.compile("([A-H]|[J-Z]|[0-9])([A-Z]|[0-9]){0,5}([A-H]|[J-Z]|[0-9])");
            Matcher matcher = pattern.matcher(currTxt);
            if (matcher.find())
            {
                currTxt = matcher.group();
            }
            MainActivity.currResultText = currTxt;
            MainActivity.currResultBmp = histBox;
            Log.d("imgP:",currTxt);
            return currTxt;
        }
        return "Not Fnd";
    }

    protected void onPostExecute(String firstResult)
    {
        Texture.inUse = false;
        //MainActivity.killHandler = true;
    }

    public histCountBox letterCleanse(String letters) {
        List<boxLetter> boxLetters = new ArrayList<boxLetter>();
        for(String ltr:letters.split("\\r?\\n")){
            try {
                boxLetter box = new boxLetter();
                String[] lst = ltr.split(" ");
                if(lst.length>=5) {
                    box.letter = lst[0];
                    box.x1 = Integer.parseInt(lst[1]);
                    box.y1 = Integer.parseInt(lst[2]);
                    box.x2 = Integer.parseInt(lst[3]);
                    box.y2 = Integer.parseInt(lst[4]);
                    if (box.y2 - box.y1 > 25) {
                        boxLetters.add(box);
                    }
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
                if((box.y2 >= hist.y2 - 10 && box.y2 <= hist.y2 +10)&&(box.y1 >= hist.y1 - 10 && box.y1 <= hist.y1 +10)) {
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

        return histCountList.get(0);
    }


}
