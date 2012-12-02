package com.example.reboxed;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends Activity{
    // For Debugging
    public static final String TAG = MainActivity.class.getName();
    
    //Shared Preferences
    private SharedPreferences mAuthCaches;
    public static final String PREF_AUTHTOKEN = "ReboxedAuthToken";
    public static final String PREF_EMAIL = "ReboxedEmail";
    
    private String mAuthToken = null;
    private String mEmail = null;
    
    private PendingIntent mAlarmSender;
    
    public static final int ACTIVITY_LOGIN = 0;
    
    private Handler mHandler = new Handler(){

        /* (non-Javadoc)
         * @see android.os.Handler#handleMessage(android.os.Message)
         */
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == 1){
                Toast.makeText(getApplicationContext(), msg.arg1, Toast.LENGTH_SHORT).show();
            }
        }
        
    };

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                
        setContentView(R.layout.main);
        
        mAuthCaches = getSharedPreferences("ReboxedPreferences", MODE_PRIVATE);
        
        if(isAuthenicated()){
//            TextView textView = (TextView) findViewById(R.id.textview);
//            textView.setText(mAuthCaches.getString(PREF_AUTHTOKEN, "AuthTokenNULL"));
//            textView.setText(mAuthCaches.getString(PREF_EMAIL, "EmailNULL"));
            GifWebView view = new GifWebView(this, "file:///android_asset/sauron.gif"); 
            setContentView(view);
            
            //set alarm
            setAlarm();
            
        }
        else {
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, ACTIVITY_LOGIN);
        }

    }      
  
    
    /* (non-Javadoc)
     * @see android.app.Activity#onActivityResult(int, int, android.content.Intent)
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        
        if(requestCode == MainActivity.ACTIVITY_LOGIN){
            if(resultCode == LoginActivity.LOGIN_SUCCESS_RESULT_CODE){
                mAuthToken = data.getExtras().getString(PREF_AUTHTOKEN);
                mEmail = data.getExtras().getString(PREF_EMAIL);
                SharedPreferences.Editor prefEditor = mAuthCaches.edit();
                prefEditor.putString(PREF_AUTHTOKEN, mAuthToken);
                prefEditor.putString(PREF_EMAIL, mEmail);
                prefEditor.commit();                
            }
            else {
                // Do nothing?                
            }
        }
    }



    /* (non-Javadoc)
     * @see android.app.Activity#onPause()
     */
    @Override
    protected void onPause() {
        // TODO Auto-generated method stub
        super.onPause();
    }

    /* (non-Javadoc)
     * @see android.app.Activity#onResume()
     */
    @Override
    protected void onResume() {
        super.onResume();

        if(isAuthenicated()){
//            TextView textView = (TextView) findViewById(R.id.textview);
//            textView.setText(mAuthCaches.getString(PREF_AUTHTOKEN, "AuthTokenNULL"));
//            textView.setText(mAuthCaches.getString(PREF_EMAIL, "EmailNULL"));

            GifWebView view = new GifWebView(this, "file:///android_asset/index.html"); 
            setContentView(view);
            
            //set alarm
            setAlarm();

        }
    }
    
    private void setAlarm(){
        
//        Intent serviceIntent = new Intent(MainActivity.this, SensorService.class);
//        
//        serviceIntent.putExtra(PREF_AUTHTOKEN, mAuthToken);
//        serviceIntent.putExtra(PREF_EMAIL, mEmail);
//        
//        mAlarmSender = PendingIntent.getService(MainActivity.this,
//                0, serviceIntent, 0);
//
//        // We want the alarm to go off 5 seconds from now.
//        long firstTime = SystemClock.elapsedRealtime();
//
//        // Schedule the alarm!
//        AlarmManager am = (AlarmManager)getSystemService(ALARM_SERVICE);
//        am.setRepeating(AlarmManager.ELAPSED_REALTIME_WAKEUP,
//                        firstTime, 5*1000, mAlarmSender);
//
////        // Tell the user about what we did.
////        Toast.makeText(AlarmService.this, R.string.repeating_scheduled,
////                Toast.LENGTH_LONG).show();
        
        DataCollectorService collectorService = new DataCollectorService(mEmail, mAuthToken, mHandler, getBaseContext());
        collectorService.start();

    }
    
    public boolean isAuthenicated(){
        return mAuthCaches.contains(PREF_AUTHTOKEN) && mAuthCaches.contains(PREF_EMAIL);
    }            
}
