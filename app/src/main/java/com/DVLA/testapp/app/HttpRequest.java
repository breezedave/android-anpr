package com.DVLA.testapp.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.json.JSONObject;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import android.os.AsyncTask;

public class HttpRequest extends AsyncTask<Object, Void, vehRecord>
{
    vehRecView vehView;

    @Override
    protected vehRecord doInBackground(Object... params)
    {
        this.vehView = (vehRecView) params[0];

        if(this.vehView.Param.length()==0) {
            vehRecord result = new vehRecord();
            result.VRM = "Not Found";
            return result;
        }

        BufferedReader inBuffer = null;
        String url = "http://breezedave.co.uk/api/values/" + this.vehView.Param;
        vehRecord result;
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(request);
            inBuffer = new BufferedReader(
                    new InputStreamReader(
                            httpResponse.getEntity().getContent()));
            StringBuffer stringBuffer = new StringBuffer("");
            String line = "";
            String newLine = System.getProperty("line.separator");
            while ((line = inBuffer.readLine()) != null) {
                stringBuffer.append(line + newLine);
            }
            inBuffer.close();

            JSONObject json = new JSONObject(stringBuffer.toString());
            vehRecord vehicle = new vehRecord();
            Log.i("Test",json.toString());
            getVehicle(json, vehicle);
            result = vehicle;

        } catch(Exception e) {
            Log.i("Err",e.getMessage());
            result = new vehRecord();
        } finally {
            if (inBuffer != null) {
                try {
                    inBuffer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return result;
    }

    protected void onPostExecute(vehRecord vehicle)
    {
        vehView.VRM.setText(vehicle.getVRM());
        vehView.Make.setText(vehicle.getMake());
        vehView.Model.setText(vehicle.getModel());
        vehView.Tax.setText(DateTimeFormat.forPattern("dd MM YY").print(vehicle.getTax()));
        vehView.MOT.setText(vehicle.isMOTed());
        vehView.Insured.setText(vehicle.getInsured());
        vehView.LoadingFrame.setVisibility(View.GONE);

    }

    public void getVehicle(JSONObject json, vehRecord vehicle) {
        try {
            vehicle.VRM = json.get("VRM").toString();
            vehicle.Make = json.get("Make").toString();
            vehicle.Model = json.get("Model").toString();
            vehicle.Tax = new DateTime(json.get("Tax").toString());
            vehicle.MOT = new DateTime(json.get("MOT").toString());
            vehicle.Insured = json.getBoolean("Insured");
        } catch (Exception e) {
            Log.i("Error",e.getMessage());
        }
    }
}