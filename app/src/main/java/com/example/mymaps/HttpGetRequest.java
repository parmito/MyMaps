package com.example.mymaps;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.SupportMapFragment;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.UnknownHostException;
import java.security.AccessController;

public class HttpGetRequest extends AsyncTask<String,Void,String> {
    public static final String REQUEST_METHOD = "GET";
    public static final int READ_TIMEOUT = 15000;
    public static final int CONNECTION_TIMEOUT = 15000;
    private static final String TAG = "HttpGetRequest";

    String StringResult ="";
    private OnCompleteListener listener;

    public void setListener(OnCompleteListener listener) {
        this.listener = listener;
    }

    @Override
    protected String doInBackground(String... string) {
        String stringUrl = string[0];
        String inputLine;

        //Create a URL object holding our url
        URL myUrl = null;         //Create a connection
        try {
            myUrl = new URL(stringUrl);
            //Connect to our url
            Log.i("HttpGetRequest", String.valueOf(myUrl));
            HttpURLConnection connection = (HttpURLConnection) myUrl.openConnection();
            //connection.connect();
            if (connection.getResponseCode() != 200) {
                return StringResult;
            }

            //Create a new InputStreamReader
            InputStreamReader streamReader = new
                    InputStreamReader(connection.getInputStream());         //Create a new buffered reader and String Builder
            BufferedReader reader = new BufferedReader(streamReader);
            StringBuilder stringBuilder = new StringBuilder();         //Check if the line we are reading is not null
            while ((inputLine = reader.readLine()) != null) {
                stringBuilder.append(inputLine);
            }         //Close our InputStream and Buffered reader
            reader.close();
            streamReader.close();         //Set our result equal to our stringBuilder
            StringResult = stringBuilder.toString();
        }
        catch(IOException e){
            e.printStackTrace();
            StringResult = null;
        }
        return StringResult;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();

    }

    @Override
    protected void onPostExecute(String result) {
        super.onPostExecute(result);
        System.out.println("PostExecute");
        Log.i("HttpGetRequest", "PostExecute");

        listener.OnComplete(StringResult);

    }
}
