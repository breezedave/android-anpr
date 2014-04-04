package com.DVLA.testapp.app;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.graphics.drawable.BitmapDrawable;
import android.media.ExifInterface;
import android.provider.MediaStore;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.ImageView;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.util.Log;
import android.os.Environment;
import android.net.Uri;
import android.widget.Toast;

import org.apache.http.protocol.HTTP;


public class MainActivity extends ActionBarActivity {

    static final int REQUEST_IMAGE_CAPTURE = 1;
    Integer orientation = 6;
    File output = getFile();
    String mCurrentPhotoPath="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        gotoStart();
    }

    protected void gotoStart() {
        setContentView(R.layout.start);
        final Button photoButton = (Button) findViewById(R.id.photoButton);
        photoButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                dispatchTakePictureIntent();
            }
        });

        final Button regButton = (Button) findViewById(R.id.regButton);
        regButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                gotoManualVrm();
            }
        });
    }

    protected void gotoManualVrm() {
        setContentView(R.layout.manual_vrm);
        final Button searchVrm = (Button) findViewById(R.id.vrmButton);
        searchVrm.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                TextView textView = (TextView)findViewById(R.id.resultHolder);
                new HttpRequest().execute("A1",textView);
            }
        });
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if (output != null) {
            Uri uri = Uri.fromFile(output);
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,uri);
            takePictureIntent.putExtra(MediaStore.EXTRA_VIDEO_QUALITY, 1);
            startActivityForResult(takePictureIntent, REQUEST_IMAGE_CAPTURE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_IMAGE_CAPTURE && resultCode == RESULT_OK) {
            setContentView(R.layout.view_photo);

            Bitmap imageBitmap = BitmapFactory.decodeFile(output.getAbsolutePath());

            try{
                ExifInterface exif = new ExifInterface(output.getAbsolutePath());
                orientation = exif.getAttributeInt(ExifInterface.TAG_ORIENTATION, 1);
                Log.d(" Orientation",orientation.toString());
            }catch(IOException e) {
                e.printStackTrace();
            }

            TextView screenText = (TextView)findViewById(R.id.atextView);
            if(screenText != null){
                screenText.setText(output.getAbsolutePath() + " - " + orientation);
            }
            ImageView mImageView = (ImageView)findViewById(R.id.mImageView);
            if(mImageView != null){
                scaleImageToView(mImageView,imageBitmap);
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private File createImageFile() throws IOException {
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = new File (Environment.getExternalStorageDirectory().getAbsolutePath() + "/Taxed");
        storageDir.mkdirs();

        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mCurrentPhotoPath = "file:" + image.getAbsolutePath();
        return image;
    }

    private void scaleImageToView(ImageView view, Bitmap bitmap)
    {
        float maxWidth = 2000;
        float maxHeight = 2000;
        float bWidth = maxWidth;
        float bHeight = maxHeight;
        try {
            bWidth = bitmap.getWidth();
            bHeight = bitmap.getHeight();
        }catch(Exception e) {
            e.printStackTrace();
        }
        float scaling = 1;
        if(bWidth > maxWidth) {
            scaling = scaling * (maxWidth/bWidth);
        }
        if(bHeight > maxHeight) {
            scaling = scaling * (maxHeight/bHeight);
        }
        Integer intWidth = (int)Math.floor(bWidth);
        Integer intHeight = (int)Math.floor(bHeight);
        Matrix matrix = new Matrix();
        matrix.postScale(scaling,scaling);
        if(orientation==6){matrix.postRotate(90);}
        if(orientation==3){matrix.postRotate(180);}
        Bitmap scaledBitmap = null;
        scaledBitmap = Bitmap.createBitmap(bitmap, 0, 0, intWidth, intHeight, matrix, true);
        BitmapDrawable result = new BitmapDrawable(scaledBitmap);

        // Apply the scaled bitmap
        view.setImageDrawable(result);
    }

    private int dpToPx(int dp)
    {
        float density = getApplicationContext().getResources().getDisplayMetrics().density;
        return Math.round((float)dp * density);
    }

    File getFile() {
        File photoFile = null;
        try {
            photoFile = createImageFile();
            return photoFile;
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
