package com.nielsen.cloudapi.activity;

/*
 * 29.Mar.14    LFR    Initial design.  This module receives all the OnPause, OnResume OnDestroyed
 *                     for every current View in display
 * 31.Mar.14    LFR    Added flag "appInBackground" to handle OnDestroy, though not used
 * 04.Mar.14    LFR    Make an instance of MainAvtivity thruout the class as opposed to do it every time is needed
 *                     and to call pause video when it goes in background.
 */

import com.nielsen.cloudapi.fragment.VideosFragment;
import com.nielsen.cloudapi.model.CloudAPI;

import android.app.Activity;
import android.content.Intent;
import android.os.Handler;
import android.util.Log;

public class MasterActivity extends Activity {
	
	private static String TAG = "masterActivity";

	public static boolean mAppInBackground = true;
	private VideosFragment mMainActivity = null;

	private static String mCurrentActivityName = "";
	private static String mMainActivityName = "";

	@Override
	public void onResume() {
		super.onResume();
		mMainActivity = new VideosFragment();

		Intent i = getIntent();
		String activityName = i.getComponent().toString();
		String logMsg;

		mCurrentActivityName = activityName;
		mAppInBackground = false;

		if ((mMainActivityName == null) || mMainActivityName.equals("")) {
			mMainActivityName = activityName;
			logMsg = "Launched";
		} else if ((activityName != null)
				&& activityName.equals(mMainActivityName)) {

			logMsg = "MainActivity on Foreground";
		} else {
			logMsg = "Reinstated an Activity";
		}
		Log.i(TAG, "onResume Application: " + logMsg + " activityName="
				+ activityName);
	}

	@Override
	public void onPause() {
		super.onPause();

		Intent i = getIntent();
		final String activityName = i.getComponent().toString();
		String logMsg;

		if (activityName.equals(mCurrentActivityName)) {
			logMsg = "Suspend or not?";
			new Handler().postDelayed(new Runnable() {
				@Override
				public void run() {
					if (activityName.equals(mCurrentActivityName)) {
						mAppInBackground = true;

						Log.e(TAG, "onPause Application: GOING IN BACKGROUND");
					} else {
						mAppInBackground = false;
						Log.e(TAG, "onPause Application: STAYS IN FOREGROUNG");
					}
				}
			}, 350);

		} else {

			if (mMainActivity != null)
				mMainActivity.uiHoldOnVideo();

			logMsg = "Launching another view";
		}

		Log.i(TAG, "onPause Application: " + logMsg + " activityName="
				+ activityName);
	
	}

	//Upon app exit, run sessionEnd()
	@Override
	public void onStop() {
		super.onStop();

		Intent i = getIntent();
		final String activityName = i.getComponent().toString();
		Log.i(TAG, "onStop Application:  activityName=" + activityName);
		
		new Thread(new Runnable(){

			@Override
			public void run() {
				CloudAPI.sessionEnd();
			}
				
		}).start();
	}

	@Override
	public void onDestroy() {
		// if (mAppInBackground) {
		// //Do your app maintenance, because it was swipped-ou or terminated
		// if(mMainActivity != null)
		// mMainActivity.destroy();
		// }

		super.onDestroy();

		Log.i(TAG, "onDestroy Application: currentActivityName="
				+ mCurrentActivityName);
	}
	

}
