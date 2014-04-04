package com.DVLA.testapp.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;

import android.util.Log;
import android.widget.TextView;

import android.os.AsyncTask;
import android.widget.TextView;

public class HttpRequest extends AsyncTask<Object, Void, String>
{
    TextView t;
    Integer statusCode = 0;
    @Override
    protected String doInBackground(Object... params)
    {
        String param = (String) params[0];
        this.t = (TextView) params[1];
        BufferedReader inBuffer = null;
        String url = "http://www.google.com?vrm=" + param;

        String result = "fail";
        try {
            HttpClient httpClient = new DefaultHttpClient();
            HttpGet request = new HttpGet(url);
            HttpResponse httpResponse = httpClient.execute(request);
            statusCode = httpResponse.getStatusLine().getStatusCode();
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

            result = stringBuffer.toString();

        } catch(Exception e) {
            // Do something about exceptions
            result = e.getMessage();
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

    protected void onPostExecute(String page)
    {

        //t.setText(page);
        t.setText(statusCode.toString());
    }
}