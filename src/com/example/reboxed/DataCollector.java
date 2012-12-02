package com.example.reboxed;

import ioio.lib.api.AnalogInput;
import ioio.lib.api.IOIO;
import ioio.lib.api.IOIOFactory;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.api.exception.IncompatibilityException;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import android.util.Log;


public class DataCollector {
    public static final String TAG = DataCollector.class.getName();
    private String mEmail;
    private String mAuthToken;
    private IOIO ioio_; 
    private AnalogInput mADC0;
    private AnalogInput mADC1;
    
    public DataCollector(String email, String authToken){
        this.mEmail = email;
        this.mAuthToken = authToken;
        ioio_ = IOIOFactory.create();
        Log.d(TAG, "Waiting for IOIO");
        try {
            ioio_.waitForConnect();
            Log.d(TAG, "Connected to IOIO");
            mADC0 = ioio_.openAnalogInput(36);
            mADC1 = ioio_.openAnalogInput(35);
            
        } catch (ConnectionLostException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IncompatibilityException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }
    
    private SensorData getSensorData(){
        SensorData data = new SensorData();
        data.accel = "1231541";
                    
        try {                                        
            data.motion = Float.toString(mADC0.getVoltage());
            data.smoke = Float.toString(mADC1.getVoltage());
            mADC0.close();
            mADC1.close();
            Log.d(TAG, "data.motion "+data.motion+" data.smoke"+data.smoke);
            
        } catch (ConnectionLostException e) {
        } catch (Exception e) {
            Log.e("HelloIOIOPower", "Unexpected exception caught", e);
            ioio_.disconnect();
        } finally {
            try {
                ioio_.waitForDisconnect();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        // TODO
        // Do something here to collect data;
        
        return data;
    }
    
    public void sendDataToServer(){
        SensorData data = getSensorData();
        HttpPost post = new HttpPost("http://angelhack.jamesyong.net/processor/device/");
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);                
        nameValuePairs.add(new BasicNameValuePair("cmd", "post"));
        nameValuePairs.add(new BasicNameValuePair("user", mEmail));
        nameValuePairs.add(new BasicNameValuePair("device", mAuthToken));
        
        //DATA type
        nameValuePairs.add(new BasicNameValuePair("smoke", data.smoke));
        nameValuePairs.add(new BasicNameValuePair("motion", data.motion));
        nameValuePairs.add(new BasicNameValuePair("accel", data.accel));
        
        try{
            post.setEntity(new UrlEncodedFormEntity(nameValuePairs));
    
            HttpClient client = new DefaultHttpClient();
            HttpResponse response = client.execute(post);
            HttpEntity entity = response.getEntity();
            String responseText = EntityUtils.toString(entity);
         
            Log.d(TAG, responseText);
            
        }
        catch (UnsupportedEncodingException e) {

            
        } catch (ClientProtocolException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        
    }

}
