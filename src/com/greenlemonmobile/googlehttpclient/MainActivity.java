package com.greenlemonmobile.googlehttpclient;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.support.v4.app.NavUtils;

import com.google.api.client.extensions.android2.AndroidHttp;
import com.google.api.client.http.GenericUrl;
import com.google.api.client.http.HttpRequest;
import com.google.api.client.http.HttpRequestFactory;
import com.google.api.client.http.HttpRequestInitializer;
import com.google.api.client.http.HttpResponse;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.http.HttpUnsuccessfulResponseHandler;
import com.google.api.client.http.LowLevelHttpRequest;
import com.google.api.client.http.LowLevelHttpResponse;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.http.json.JsonHttpParser;
import com.google.api.client.json.jackson.JacksonFactory;
import com.google.api.client.testing.http.MockHttpTransport;

import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

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
        
        HttpTransport httpTransport = AndroidHttp.newCompatibleTransport();
        HttpRequestFactory httpRequestFactory = httpTransport.createRequestFactory(new MyInitializer());
        try {
            HttpRequest httpRequest = httpRequestFactory.buildGetRequest(new GenericUrl("http://www.baidu.com/"));
            httpRequest.setThrowExceptionOnExecuteError(false);
            httpRequest.addParser(new JsonHttpParser(new JacksonFactory()));

            HttpResponse httpResponse = httpRequest.execute();
            if (!httpResponse.isSuccessStatusCode()) {
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        NetHttpTransport httpTransport2 = new NetHttpTransport();
        try {
            LowLevelHttpRequest httpRequest = httpTransport2.buildGetRequest("http://ibooksreader.googlecode.com/files/latest_version.json");

            LowLevelHttpResponse httpResponse = httpRequest.execute();
            if (httpResponse.getStatusCode() == 200) {
                httpResponse.getContentType();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        
        int callsBeforeSuccess = 3;
        MockHttpTransport fakeTransport = new MockHttpTransport();
        HttpRequest req;
        try {
            req = fakeTransport.createRequestFactory().buildGetRequest(new GenericUrl("http://www.baidu.com/"));
            req.setRetryOnExecuteIOException(true);
            req.setNumberOfRetries(callsBeforeSuccess + 1);
            HttpResponse resp = req.execute();
            if (!resp.isSuccessStatusCode()) {
            } else {
                
            }
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
