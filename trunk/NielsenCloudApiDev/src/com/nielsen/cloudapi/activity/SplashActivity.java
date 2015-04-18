package com.nielsen.cloudapi.activity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

/**
 * Splash activity for the application
 * 
 */
public class SplashActivity extends Activity
{
    // Splash screen timer
    private static int SPLASH_TIME_OUT = 1000;
    private Handler mHandler = new Handler(); 
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() 
        {
            /** Start your app main activity */
            Intent i = new Intent(SplashActivity.this, MainActivity.class);
            startActivity(i);

            /** close this activity */
            finish();
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);
        mHandler.postDelayed(mRunnable, SPLASH_TIME_OUT);
    }

    @Override
    public void onDestroy()
    {
        mHandler.removeCallbacks(mRunnable);
        super.onDestroy();
    }
}