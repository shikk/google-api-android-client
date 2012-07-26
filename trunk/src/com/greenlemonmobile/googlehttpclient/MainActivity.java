package com.greenlemonmobile.googlehttpclient;

import android.app.Activity;
import android.os.Bundle;
import android.support.v4.app.NavUtils;
import android.view.Menu;
import android.view.MenuItem;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.http.ByteArrayContent;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.GZIPInputStream;

public class MainActivity extends Activity {
    
    public static class MyInitializer implements HttpRequestInitializer, HttpUnsuccessfulResponseHandler {

        @Override
        public boolean handleResponse(
                HttpRequest request, HttpResponse response, boolean retrySupported)
                throws IOException {
            System.out.println(response.getStatusCode() + " " + response.getStatusMessage());
            return false;
        }

        @Override
        public void initialize(HttpRequest request) throws IOException {
            request.setUnsuccessfulResponseHandler(this);
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        
        Logger.getLogger(HttpTransport.class.getName()).setLevel(Level.CONFIG);
        
        // GET
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        HttpRequestFactory httpRequestFactory = httpTransport.createRequestFactory(new MyInitializer());
        try {
            HttpRequest httpRequest = httpRequestFactory.buildGetRequest(new GenericUrl("http://api.ubox.cn/ubox/getVmInfo.json?version=v1&deviceid=2&clientversion=3.1.0&channelId=1device_no=3c:43:8e:ea:79:3a"));
            httpRequest.setThrowExceptionOnExecuteError(false);
            httpRequest.setEnableGZipContent(true);
            httpRequest.addParser(new JsonHttpParser(new JacksonFactory()));

            HttpResponse httpResponse = httpRequest.execute();
            if (!httpResponse.isSuccessStatusCode()) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        // POST
        try {
            ByteArrayContent byteContent = ByteArrayContent.fromString("sign=DE3749931B8E9F40B46D8685DBDE90A7&timestamp=1343282682368&uid=28280&vmid=146", "application/x-www-form-urlencoded");
            HttpRequest httpRequest = httpRequestFactory.buildPostRequest(new GenericUrl("http://api.ubox.cn/ubox/getVmInfo.json?version=v1&deviceid=2&clientversion=3.1.0&channelId=1device_no=3c:43:8e:ea:79:3a"), byteContent);
            httpRequest.setEnableGZipContent(false);
            HttpResponse httpResponse = httpRequest.execute();
            if (!httpResponse.isSuccessStatusCode()) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        URL url;
        try {
            url = new URL("http://api.ubox.cn/ubox/getVmInfo.json?version=v1&deviceid=2&clientversion=3.1.0&channelId=1device_no=3c:43:8e:ea:79:3a");

            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            
            conn.setConnectTimeout(5000);
            conn.setReadTimeout(5000);
            conn.setUseCaches(false);
            conn.setRequestProperty("Charset", "UTF-8");
            conn.setRequestProperty("Connection", "Keep-Alive");
            conn.setRequestProperty("Accept-Encoding", "gzip,deflate");
            conn.setRequestMethod("POST");
            conn.setDoOutput(true);
            String formData = "sign=DE3749931B8E9F40B46D8685DBDE90A7&timestamp=1343282682368&uid=28280&vmid=146";
            conn.setRequestProperty("Content-Length", String.valueOf(formData.getBytes().length));
            conn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            
            DataOutputStream outStream = new DataOutputStream(conn.getOutputStream());
            outStream.write(formData.getBytes());
            outStream.flush();
            
            conn.connect();
            
            conn.getResponseCode();
            conn.getContentLength();            
            conn.getContentType();
            
            InputStream is = null;
            String encoding = conn.getHeaderField("Content-Encoding");
            if ("gzip".equals(encoding)) {
                is = new GZIPInputStream(conn.getInputStream());
            } else {
                is = conn.getInputStream();
            }
            
            byte[] data = null;
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            byte[] buf = new byte[1024];
            int len = 0;
            while ((len = is.read(buf)) != -1) {
                os.write(buf, 0, len);
            }
            data = os.toByteArray();
            String jsonContent = new String(data, "UTF-8");
            
            try {
                JSONObject jsonObject = new JSONObject(jsonContent);
            } catch (JSONException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
            
        } catch (MalformedURLException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } 
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.activity_main, menu);
        return true;
    }

    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                NavUtils.navigateUpFromSameTask(this);
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

}
