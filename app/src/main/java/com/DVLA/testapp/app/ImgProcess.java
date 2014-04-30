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
        List<boxLetter> boxLetters = new ArrayList<boxLetter>();
        for(String ltr:letters.split("\\r?\\n")){
            //Log.i("boxLetters: ",ltr);
            boxLetter box = new boxLetter();
            String[] lst = ltr.split(" ");
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
                if(box.y1 * 1.0 >= hist.y1 * -1.5 && box.y1 * 1.0 <= hist.y1 * 1.5) {
                    if(box.y2 * 1.0 >= hist.y2 * -1.5 && box.y2 * 1.0 <= hist.y2 * 1.5) {
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
        histCountBox boxCoord = histCountList.get(0);
        Log.i("histBOxCOunt: ",histCountList.size()+"");
        Log.i("histBox: ",histCountList.get(0).volume+"");

        //Integer thisX = Math.max(0,boxCoord.xMin*2/3);
        Integer thisX = 0;
        Integer thisY = Math.max(0,boxCoord.y1);
        //Integer thisW = (boxCoord.xMax - boxCoord.xMin);
        Integer thisW = processedBitmap.getWidth();
        Integer thisH =  (boxCoord.y2 - boxCoord.y1)*2;
        Log.i("histBox: ",thisX + " " + thisY + " " + thisW + " " + thisH);

        Bitmap histBox = OpenCV.getMini(processedBitmap,thisX, thisY, thisW,thisH);

        tess.setImage(histBox);
        currTxt = tess.getUTF8Text();
        Log.i("Txt: ",currTxt);
        if(currTxt.length()>0){
            MainActivity.currResultBmp = histBox;
            MainActivity.currResultText= currTxt;
            return currTxt;
        }
        return "notfound";
        /*
        Log.i("Post third: ",""+currTxt);
        if(currTxt==""){return "";}
        Bitmap chosen = OpenCV.getMini(processedBitmap,0,fileHeight/4 * chosenBit,fileWidth,fileHeight/2);
        i = 0;
        while(currMaxLen>0 && i< chosen.getHeight()-10 ) {
            i+= 3;
            Bitmap yShrink = OpenCV.getMini(chosen,0, i, chosen.getWidth(),chosen.getHeight()-i);
            tess.setImage(yShrink);
            currTxt = tess.getUTF8Text();
            currMaxLen=currTxt.length();
            MainActivity.currResultBmp = yShrink;
            MainActivity.currResultText= "Text: "+currTxt;
        }
        chosen = OpenCV.getMini(chosen,0, i-3, chosen.getWidth(),chosen.getHeight()-(i-3));
        tess.setImage(chosen);
        currTxt = tess.getUTF8Text();
        currMaxLen = currTxt.length();
        Log.i("Post top shrink",""+currTxt);
        MainActivity.currResultBmp = chosen;
        MainActivity.currResultText = currTxt;
        String lastText = currTxt;
        i = 0;
        while(lastText == currTxt && i< chosen.getHeight()-10 ) {
            i+= 3;
            Bitmap yShrink = OpenCV.getMini(chosen,0, 0, chosen.getWidth(),chosen.getHeight()-i);
            tess.setImage(yShrink);
            currTxt = tess.getUTF8Text();
            currMaxLen=currTxt.length();
            MainActivity.currResultBmp = yShrink;
            MainActivity.currResultText= "Text: "+currTxt;
        }
        chosen = OpenCV.getMini(chosen,0, 0, chosen.getWidth(),chosen.getHeight()-(i-3));
        tess.setImage(chosen);
        currTxt = tess.getUTF8Text();
        currMaxLen = currTxt.length();
        Log.i("Post base shrink",""+currTxt);
        MainActivity.currResultBmp = chosen;
        MainActivity.currResultText = currTxt;


        Log.i("currTxt: ",currTxt);
        return currTxt;
        */

        // Loop method
        /*
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
        String result = "";
        if(found.size()>0) {
            result = found.get(0).toString();
        } else {
            result = "";
        }
        return result;
        */
    }
    protected void onPostExecute(String firstResult)
    {
        if(firstResult != null){
            mTextView.setText(firstResult);
        }
        MainActivity.killHandler = true;
    }
}
