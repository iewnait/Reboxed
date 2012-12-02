package com.example.reboxed;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.TextView;

public class MainActivity extends Activity{
    // For Debugging
    public static final String TAG = MainActivity.class.getName();
    
    //Shared Preferences
    private SharedPreferences mAuthCaches;
    public static final String PREF_AUTHTOKEN = "ReboxedAuthToken";
    
    private String mAuthToken = null;
    
    public static final int ACTIVITY_LOGIN = 0;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);                
        setContentView(R.layout.main);        
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
                SharedPreferences.Editor prefEditor = mAuthCaches.edit();
                prefEditor.putString(PREF_AUTHTOKEN, mAuthToken);
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
        
        mAuthCaches = getSharedPreferences("ReboxedPreferences", MODE_PRIVATE);
        
        if(isAuthenicated()){
            TextView textView = (TextView) findViewById(R.id.textview);
            textView.setText(mAuthCaches.getString(PREF_AUTHTOKEN, "AuthTokenNULL"));
            
            
            
        }
        else {
            Intent i = new Intent(this, LoginActivity.class);
            startActivityForResult(i, ACTIVITY_LOGIN);
        }

    }
    
    public boolean isAuthenicated(){
        return mAuthCaches.contains(PREF_AUTHTOKEN);
    }

}
