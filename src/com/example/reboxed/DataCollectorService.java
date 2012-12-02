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
import java.util.Timer;
import java.util.TimerTask;

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

import android.app.PendingIntent;
import android.util.Log;

public class DataCollectorService extends Thread {
    
    public static final String TAG = DataCollector.class.getName();
    private String mEmail;
    private String mAuthToken;
    private IOIO ioio_; 
    private AnalogInput mADC0;
    private AnalogInput mADC1; 
    private SensorDataHistory mSensorDataHistory;
    
    private PendingIntent mAlarmSender;
    
    public DataCollectorService(String email, String authToken){
        this.mEmail = email;
        this.mAuthToken = authToken;
        this.mSensorDataHistory = new SensorDataHistory(20);
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
        
        int delay = 5000; // delay for 5 sec.

        int period = 5000; // repeat every 5 sec.

        Timer timer = new Timer();

        timer.scheduleAtFixedRate(new TimerTask() {

        public void run() {

            sendDataToServer();

        }

        }, delay, period);        
                
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        Log.d(TAG, "Begin mAcceptThread on: " + this);
        setName("CollectorThread");

        while(true) {    
            mSensorDataHistory.addSensorData(getSensorData());
            try{
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
        }
    }
    
    /* (non-Javadoc)
     * @see java.lang.Thread#destroy()
     */
    @Override
    public void destroy() {     
        mADC0.close();
        mADC1.close();
        try {
            ioio_.waitForDisconnect();
        } catch (InterruptedException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    }

    private SensorData getSensorData(){
        SensorData data = new SensorData();
        data.accel = 12315.41f;
                    
        try {                                        
            data.motion = mADC0.getVoltage();
            data.smoke = mADC1.getVoltage();
            Log.d(TAG, "data.motion "+data.motion+" data.smoke"+data.smoke);
            
        } catch (ConnectionLostException e) {
        } catch (Exception e) {
            Log.e("HelloIOIOPower", "Unexpected exception caught", e);
            ioio_.disconnect();
        }
        
        // TODO
        // Do something here to collect data;
        
        return data;
    }


    private void sendDataToServer(){
        SensorData data = mSensorDataHistory.getAverage();
        HttpPost post = new HttpPost("http://angelhack.jamesyong.net/processor/device/");
        List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>(2);                
        nameValuePairs.add(new BasicNameValuePair("cmd", "post"));
        nameValuePairs.add(new BasicNameValuePair("user", mEmail));
        nameValuePairs.add(new BasicNameValuePair("device", mAuthToken));
        
        //DATA type
        nameValuePairs.add(new BasicNameValuePair("smoke", Float.toString(data.smoke)));
        nameValuePairs.add(new BasicNameValuePair("motion", Float.toString(data.motion)));
        nameValuePairs.add(new BasicNameValuePair("accel", Float.toString(data.accel)));
        
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
