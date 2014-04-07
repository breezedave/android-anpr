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
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONObject;

import android.content.Intent;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import android.os.AsyncTask;

public class HttpRequest extends AsyncTask<Object, Void, vehRecord>
{
    TextView VRM;
    TextView Make;
    TextView Model;
    TextView FirstReg;
    TextView Tax;
    TextView MOT;
    TextView Insured;
    FrameLayout Loading;

    @Override
    protected vehRecord doInBackground(Object... params)
    {
        String param = (String) params[0];
        this.VRM = (TextView) params[1];
        this.Make = (TextView) params[2];
        this.Model = (TextView) params[3];
        this.FirstReg = (TextView) params[4];
        this.Tax = (TextView) params[5];
        this.MOT = (TextView) params[6];
        this.Insured = (TextView) params[7];
        this.Loading = (FrameLayout) params[8];

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
            Log.i("Test",json.toString());
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

    protected void onPostExecute(vehRecord vehicle)
    {
        VRM.setText(vehicle.getVRM());
        Make.setText(vehicle.getMake());
        Model.setText(vehicle.getModel());
        FirstReg.setText(DateTimeFormat.forPattern("YYYY MMM dd").print(vehicle.getFirstReg()));
        Tax.setText(DateTimeFormat.forPattern("YYYY MMM dd").print(vehicle.getTax()));
        MOT.setText(DateTimeFormat.forPattern("YYYY MMM dd").print(vehicle.getMOT()));
        Insured.setText(vehicle.getInsured().toString());
        Loading.setVisibility(View.GONE);

    }

    public void buildVehicle(JSONObject json,vehRecord vehicle) {
        try {
            vehicle.VRM = json.get("VRM").toString();
            vehicle.Make = json.get("Make").toString();
            vehicle.Model = json.get("Model").toString();
            vehicle.FirstReg = new DateTime(json.get("FirstReg"));
            vehicle.Tax = new DateTime(json.get("Tax").toString());
            vehicle.MOT = new DateTime(json.get("MOT").toString());
            vehicle.Insured = json.getBoolean("Insured");
        } catch (Exception e) {
            Log.i("Error",e.getMessage());
        }
    }
}