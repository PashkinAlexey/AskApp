package com.example.askapp;

import android.util.Log;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.StatusLine;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by Алексей on 12.09.2016.
 */

public abstract class JSONParser {

    public static JSONObject getJSONFromUrl(String url) throws JSONException, IOException {
        JSONObject jobj = null;
        HttpURLConnection urlConnection = null;
        BufferedReader reader = null;
        String str="";

        URL targetUrl = new URL(url);

        urlConnection = (HttpURLConnection) targetUrl.openConnection();
        urlConnection.setRequestMethod("GET");
        urlConnection.connect();

        InputStream inputStream = urlConnection.getInputStream();
        StringBuffer buffer = new StringBuffer();

        reader = new BufferedReader(new InputStreamReader(inputStream));

        String line;
        while ((line = reader.readLine()) != null) {
            buffer.append(line);
        }

        str = buffer.toString();
        jobj = new JSONObject(str);
        return jobj;
    }
}
