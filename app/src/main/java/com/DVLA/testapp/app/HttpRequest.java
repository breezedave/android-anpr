package com.DVLA.testapp.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.DateTime;
import org.json.JSONObject;

import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.TextView;

import android.os.AsyncTask;

public class HttpRequest extends AsyncTask<Object, Void, vehRecord>
{
    TextView t;
    FrameLayout l;

    @Override
    protected vehRecord doInBackground(Object... params)
    {
        String param = (String) params[0];
        this.t = (TextView) params[1];
        this.l = (FrameLayout) params[2];
        BufferedReader inBuffer = null;
        String url = "http://breezedave.cloudapp.net/api/values/" + param;
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
            buildVehicle(json, vehicle);
            result = vehicle;

        } catch(Exception e) {
            // Do something about exceptions
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

    protected void onPostExecute(vehRecord vehRecord)
    {
        l.setVisibility(View.GONE);
        t.setText(vehRecord.getVRM());
        //t.setText(statusCode.toString());
    }

    public void buildVehicle(JSONObject json,vehRecord vehicle) {
        try {
            vehicle.VRM = json.get("VRM").toString();
            vehicle.Make = json.get("Make").toString();
            vehicle.Model = json.get("Model").toString();
            vehicle.FirstReg = new DateTime(json.get("FirstReg").toString());
            vehicle.Tax = new DateTime(json.get("Tax").toString());
            vehicle.MOT = new DateTime(json.get("MOT").toString());
        } catch (Exception e) {
            Log.i("Error",e.getMessage());
        }
    }
}