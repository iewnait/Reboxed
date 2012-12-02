package com.example.reboxed;

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
    
    public DataCollector(String email, String authToken){
        this.mEmail = email;
        this.mAuthToken = authToken;        
    }
    
    private SensorData getSensorData(){
        SensorData data = new SensorData();
        
        data.motion = "12421232";
        data.accel = "1231541";
        data.smoke = "25123";
        
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
