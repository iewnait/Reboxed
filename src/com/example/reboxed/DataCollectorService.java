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
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Handler;
import android.util.Log;

public class DataCollectorService extends Thread implements SensorEventListener {
    
    public static final String TAG = DataCollector.class.getName();
    private String mEmail;
    private String mAuthToken;
    private IOIO ioio_; 
    private AnalogInput mADC0;
    private AnalogInput mADC1; 
    private SensorDataHistory mSensorDataHistory;
    private SensorManager mSensorManager;
    private Context mActivityContext;
    
    private Handler mHandler;
    
    private float x = 0;
    private float y = 0;
    private float z = 0;
    private int mAccelerometerHistoryCount = 0;
    
    private PendingIntent mAlarmSender;
    
    public DataCollectorService(String email, String authToken, Handler handler, Context context){
        this.mEmail = email;
        this.mAuthToken = authToken;
        this.mHandler = handler;
        this.mActivityContext = context;
        
        mSensorManager = (SensorManager) mActivityContext.getSystemService(mActivityContext.SENSOR_SERVICE);
        
        mSensorManager.registerListener(this,
                mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);

        
        this.mSensorDataHistory = new SensorDataHistory(20);
        ioio_ = IOIOFactory.create();
                
    }

    /* (non-Javadoc)
     * @see java.lang.Thread#run()
     */
    @Override
    public void run() {
        Log.d(TAG, "Begin mAcceptThread on: " + this);
        setName("CollectorThread");
        
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
            Log.d(TAG, "sendData....");
            sendDataToServer();

        }

        }, delay, period);        

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
        
        mSensorManager.unregisterListener(this);

    }

    private SensorData getSensorData(){
        SensorData data = new SensorData();
        
        float avgX = x;       
        float avgY = y;
        float avgZ = z;
        float avgCount = mAccelerometerHistoryCount;
        
        x = 0;
        y = 0;
        z = 0;
        mAccelerometerHistoryCount = 0;
        
        avgX = avgX/avgCount;
        avgY = avgY/avgCount;
        avgZ = avgZ/avgCount;
        
        
        float accelerationSquareRoot = (float) Math.sqrt((avgX * avgX + avgY * avgY + avgZ * avgZ));
        
        Log.d(TAG, "avgX "+avgX+" avgY "+avgY + " avgZ "+avgZ+" count "+avgCount + " accel "+accelerationSquareRoot);
        
        data.accel = accelerationSquareRoot;
                    
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
            mHandler.obtainMessage(1, R.string.sensor_service_finished, -1).sendToTarget();

            
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // TODO Auto-generated method stub
        
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {
            getAccelerometer(event);
        }
    
    }
    
    void getAccelerometer(SensorEvent event){
        float[] values = event.values;
        // Movement
         x += values[0];
         y += values[1];
         z += values[2];
         mAccelerometerHistoryCount ++;

    }
    
}
