package com.nielsen.cloudapi.fragment;

/*
 * 27.Mar.14    LFR    Un-comment the code for suspend @ onPause
 *                     Copy the tested code of ACTIVITY METHODS from the QA version
 *                     Implementation of Scrub Bar Gauge
 * 28.Mar.14    LFR    Implementation of a new ID3 Decoder
 *                     Display ID3 Decoder Version
 * 29.Mar.14    LFR    Re-instated the code in appMonitorPlayHead to send to SDK the position every 2Secs.
 * 30.Mar.14    LFR    Implementation of Background detection by creating "masterActivity".
 *                     All extended Activity were changed to masterActivity
 *                     var mAppSdk was move to "masterActivity
 *                     Added ocrMetaData in replacement to MetaData
 * 01.Apr.14    LFR    Fixed where a pause player was reloading upon tapping play after pause
 * 03.Apr.14    LFR    Reverse the order to StartMetering.  Now is Play, then Load Metadata
 * 08.Apr.14    LFR    Force to release the Progress Dialog on dismiss, because going away in background
 * 10.Apr.14    LFR    None of Dialog or Progress are displayed if the app is in Background
 * 28.Apr.14    LFR    Added AdModel & DataSrc
 * 01.May.14    LFR    In the metaData set AssetId with the name of the channel
 *                     and don't send AdModel & DataSrc if either is empty
 * 05.May.14    LFR    Added AdModel & DataSrc
 * 14.May.14    LFR    Removed dprflag
 * 16.May.14    LFR    Changed calc UTC for headpos in Live Stream
 * 16.May.14    LFR    toggleSmooth button added as well as the code for it

 */

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ImageButton;
import android.widget.SeekBar;
import android.widget.SeekBar.OnSeekBarChangeListener;
import android.widget.TextView;
import android.widget.Toast;

import com.nielsen.cloudapi.activity.MainActivity;
import com.nielsen.cloudapi.activity.MasterActivity;
import com.nielsen.cloudapi.activity.R;
import com.nielsen.cloudapi.model.AdvertisingIdClient;
import com.nielsen.cloudapi.model.AdvertisingIdClient.AdInfo;
import com.nielsen.cloudapi.model.CloudAPI;
import com.nielsen.cloudapi.model.Global;
import com.nielsen.cloudapi.model.MovieItem;
import com.nielsen.cloudapi.model.MovieList;
import com.nielsen.cloudapi.model.Player;

@SuppressLint("SimpleDateFormat")
public class VideosFragment extends Fragment implements
		OnItemClickListener, OnClickListener, OnSeekBarChangeListener {

	private final String TAG = VideosFragment.class.getSimpleName();
	
	public static final String APP_ID = "FHG163HR-BH45-JKY6-BKH7-67GJKY68GJK8";

	private static Context mContext;
	private static Activity appActivity;
	private static String appPkgName;
	private static Intent launchIntent;
	private static String advertisingId;

	private static String appName, appId, appVersion, appBuiltNo, appVerBuilt,
			videoCensusId, appClientId;
	private static boolean appDisabled = false;

	private static String ocrMetaData;
	private static boolean sendOCR = false;
	private static boolean smoothing = false;

	private static String nuid = "unknown";
	private static String idfa = "unknown";
	private static String sdkVersion = "0.0";
	private static String optOutUrl = "http://secure-uat-cert.imrworldwide.com/nielsen_app_optout.html";
	private static String sfCode = "uat-cert";// "uat-cert"; //"qatdpr"; // "us"
	private static String sdkCfgUrl = ""; // http://www.nielseninternet.com/id3sdk/config-hyu-drm-stnId.txt

	private static Timer monitorHeadTimer = null;
	private static TimerTask monitorHeadPos;

	//private static TextView  channelName;
	private static ImageButton ibtnPlay, ibtnSettings,  ibtnReplay,
	ibtnInfo,ibtnUp, ibtnDown;

	private static SurfaceView mPlayerView;

	private static SeekBar seekBar;
	private static TextView tvBarGauge;

	private static int overridePlayState = 0;

	private static AppProgressDialog mProgressDialog;
	private static AlertDialog alert;

	private static Bundle activityPars;

	public static Player mPlayer;
	public static MovieList mMovies;
	public static MovieItem mCurrentMovie;
	private static boolean newMovie2Play = true;

	private final String LOG_FILENAME = "logActivity.txt";
	public static String appLogFileLocation;
	public static int appLogSize = 0;
	public static String finalId3;
	private static VideosFragment mMainActivity = null;
	View rootView = null;
	private static String sessionId;
	private Handler durationHandler = new Handler();


	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		mMainActivity = this;		
		rootView = inflater.inflate(R.layout.activity_main_player, container, false);

		mContext = rootView.getContext();
		appPkgName = mContext.getPackageName();
		appActivity = getActivity();

		PackageManager manager = getActivity().getPackageManager();
		try {
			PackageInfo appInfo = manager.getPackageInfo(getActivity().getPackageName(),
					0);
			appVersion = appInfo.versionName;
			appBuiltNo = Integer.toString(appInfo.versionCode);
			appVerBuilt = appVersion + "." + appBuiltNo;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		//Insert your own name here
		appName = "NielsenCloudApi";
		//Insert your own AppID here
		appId = "FHG163HR-BH45-JKY6-BKH7-67GJKY68GJK8";
		videoCensusId = "AppSampleVcId";
		appClientId = "Sujay Anjankar";

		ocrMetaData = "{\"type\":\"ad\","
				+ "\"nol_ocrtag\":\"http://secure-aws.imrworldwide.com/cgi-bin/m?ci=ENT29825&am=3&ep=1&at=view&rt=banner&st=image&ca=1234&cr=crv194436&pc=plc1234320\" }";

		appLoadAppSettings();

		try {
			mMovies = new MovieList(mContext);
			mCurrentMovie = mMovies.getCurrentMovie();
		} catch (Exception e) {
			mMovies = null;
			uiSetPopMessage("Movie list empty");
		}

		uiInitialize();

		uiResetLog();

		try {
			mPlayer = Player.getInstance(mContext, this);
		} catch (Exception e) {
			mPlayer = null;
		}

		uiResetPlayPause();

		uiSetPopMessage("Please hit the play button to start playing");
		new LongOperation().execute("");
		
		return rootView;
		
	}

	/********************************************************************************************* UI METHODS ***/
	private void uiInitialize() {

		//channelName = (TextView) rootView.findViewById(R.id.channelName);

		mPlayerView = (SurfaceView) rootView.findViewById(R.id.playerView);
		seekBar = (SeekBar) rootView.findViewById(R.id.seekBar);
		seekBar.setVisibility(View.VISIBLE);
		seekBar.setOnSeekBarChangeListener(this);

		tvBarGauge = (TextView) rootView.findViewById(R.id.lblBarGauge);
		tvBarGauge.setVisibility(View.VISIBLE);

		//ibtnSettings = uiBindImageButton(R.id.btnSettings);
		ibtnPlay = uiBindImageButton(R.id.btnPlay);
		//ibtnPause = uiBindImageButton(R.id.btnPause);
		 //ibtnInfo     = uiBindImageButton(R.id.btnInfo);
		ibtnUp = uiBindImageButton(R.id.btnUp);
		ibtnDown = uiBindImageButton(R.id.btnDown);
		ibtnReplay = uiBindImageButton(R.id.btnReplay);

		final Handler handler = new Handler();
		handler.postDelayed(new Runnable() {
			@Override
			public void run() {
				uiDisplayMovieName();
			}
		}, 2000);
		
		mPlayerView.setOnTouchListener(new OnTouchListener() {
			
			@Override
			public boolean onTouch(View v, MotionEvent event) {
				uiManagePlayPause(overridePlayState);
				ibtnPlay.setVisibility(View.VISIBLE);
				return true;
			}
		});
	}

	public SurfaceView uiGetPlayerView() {
		return mPlayerView;
	}

	public void uiResetPlayPause() {
		ibtnPlay.setSelected(false);
		//ibtnPause.setSelected(false);
		appStopMeteringVideo();
	}

	public void uiSetPopMessage(String msg) {
		Toast.makeText(getActivity(), msg, Toast.LENGTH_LONG).show();
	}

	public void uiSetProgressDialog(String message) {
		final String msg = message;

		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				if (MasterActivity.mAppInBackground)
					return;

				mProgressDialogLock.lock();

				if (mProgressDialog == null) {
					Log.d("ProgressDialog", "create");
					mProgressDialog = new AppProgressDialog(appActivity);
					mProgressDialog.setIndeterminate(true);
				}

				mProgressDialog.setMessage(msg);
				mProgressDialog.show();

				mProgressDialogLock.unlock();
			}
		});
	}

	public void uiResetProgressDialog() {
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				mProgressDialogLock.lock();

				if ((mProgressDialog != null) && mProgressDialog.isShowing())
					mProgressDialog.dismiss();

				mProgressDialog = null;
				Log.d("ProgressDialog", "SET 2 NULL");

				mProgressDialogLock.unlock();
			}
		});
	}

	private Lock mProgressDialogLock = new ReentrantLock();

	public void uiSetAlertDialog(String title, String message) {
		final String finalTitle;
		if (title != null)
			finalTitle = title;
		else
			finalTitle = "";

		final String finalMsg;
		if (message != null)
			finalMsg = message;
		else
			finalMsg = "";

		uiResetProgressDialog();

		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				if (MasterActivity.mAppInBackground)
					return;

				mAlertDialogLock.lock();
				if (alert != null) {
					if (alert.isShowing()) {
						alert.dismiss();
					}
					alert = null;
				}

				final AlertDialog.Builder builder = new AlertDialog.Builder(
						appActivity);
				builder.setMessage(finalMsg)
						.setTitle(finalTitle)
						.setCancelable(false)
						.setPositiveButton("Ok",
								new DialogInterface.OnClickListener() {
									public void onClick(DialogInterface dialog,
											int which) {
										alert.dismiss();
									}
								});

				alert = builder.create();
				alert.show();
				mAlertDialogLock.unlock();
			}
		});
	}

	private Lock mAlertDialogLock = new ReentrantLock();

	synchronized public void uiAppendLog(String logEntry) {
		SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss.SSS");
		final String buf = String.format("%s %s%n", sdf.format(new Date()),
				logEntry);

		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				if (MasterActivity.mAppInBackground)
					return;
				appAppendLogFile(buf);
			}
		});
	}
	
	//TODO comment
	synchronized public void uiUpdateSeekbar(final boolean isPlaying) {
		
		getActivity().runOnUiThread(new Runnable() {
			public void run() {
				if (MasterActivity.mAppInBackground)
					return;
					updateSeekBar();

			}
		});
	}
	
	private Runnable updateSeekBarTime = new Runnable() {

	    public void run() {
	        updateSeekBar();
	        //repeat yourself that again in 100 miliseconds
	        durationHandler.postDelayed(this, 1000);

	    }

	};

	private void uiResetLog() {

		appResetLogFile();
	}

	/*------------------------------------------------------------------------------- INTERNAL UI METHODS --*/
	@Override
	public void onProgressChanged(SeekBar seekBar, int progressPos,
			boolean fromUser) {
		// Log.e(TAG, "The progressPos: " + progressPos + " fromUser: " +
		// fromUser);
		if (fromUser && mPlayer != null)
			mPlayer.setPlayhead(progressPos);
	}

	@Override
	public void onStartTrackingTouch(SeekBar seekBar) {
		// Log.e(TAG, "Start track touch");
	}

	@Override
	public void onStopTrackingTouch(SeekBar seekBar) {
		// Log.e(TAG, "Stop track touch");
	}

	private void uiDisplayMovieName() {
		if (MasterActivity.mAppInBackground || 
				 (mCurrentMovie == null))
			return;

		//String CurName = channelName.getText().toString();
		String NewName = mCurrentMovie.getName();
		//channelName.setText(NewName);
		uiAppendLog("Channel: " + NewName);
		//newMovie2Play = !NewName.equals(CurName);
	}

	private void uiManagePlayPause(int tappedBtnId) {
		
		
		boolean toPlay = (tappedBtnId == R.id.btnPlay);

		if (mPlayer == null) {
			try {
				mPlayer = Player.getInstance(mContext, this);
				newMovie2Play = true;
			} catch (Exception e) {
				mPlayer = null;
			}
		}

		if (mPlayer != null && toPlay != mPlayer.isPlaying()) {
			ibtnPlay.setSelected(toPlay);
			//ibtnPause.setSelected(!toPlay);

			if (newMovie2Play) {
				if (toPlay) {
					if (mPlayer.changeChannel(mCurrentMovie.getUrl())) {
						appStartMeteringVideo();
						newMovie2Play = false;
					}
				}
			} else {
				if (toPlay) {
					if (mPlayer.continueVideo())
						appStartMeteringVideo();
				} else {
					if (mPlayer != null)
						mPlayer.pauseVideo();
					appStopMeteringVideo();
				}
			}
		}
		ibtnPlay.setVisibility(View.GONE);
		overridePlayState = 0;
		if (mPlayer != null)
			Log.d(TAG, (mPlayer.isPlaying() ? "PLAYING" : "PAUSE"));
	}

	public void uiHoldOnVideo() {
		if (mPlayer != null && mPlayer.isPlaying()) {
			//uiManagePlayPause(R.id.btnPause);
			overridePlayState = R.id.btnPlay;
		}
	}

	private void uiReleaseHoldOnVideo() {
		if (overridePlayState != 0) {
			uiManagePlayPause(overridePlayState);
		}
	}

	public void onClick(View v) {

		int pressedBtn = v.getId();
		MovieItem tmpMovie = null;

		switch (pressedBtn) {
		case R.id.btnPlay:
		//case R.id.btnPause:
			uiManagePlayPause(pressedBtn);
			break;

		

		/*case R.id.btnSettings:
			uiHoldOnVideo();
			launchIntent = new Intent(getActivity(), SettingsFragment.class);
			activityPars = new Bundle();

			appendAppStrBundle(Global.keySdkCfgUrl);
			appendAppStrBundle(Global.keyAppId);
			appendAppStrBundle(Global.keyAppName);
			appendAppStrBundle(Global.keyAppVer);
			appendAppStrBundle(Global.keyAppClientId);
			appendAppStrBundle(Global.keyVideoCensusId);
			appendAppStrBundle(Global.keySFcode);
			appendAppStrBundle(Global.keyAdModel);
			appendAppStrBundle(Global.keyDataSrc);

			launchIntent.putExtras(activityPars);
			startActivityForResult(launchIntent, Global.SETTINGS_REQUEST);
			break;
			
			
		   case R.id.btnInfo:
               uiHoldOnVideo();
               launchIntent = new Intent(getActivity(), InfoActivity.class);
               activityPars = new Bundle();             
               appendAppStrBundle(Global.keyIdfa); 
               appendAppStrBundle(Global.keyWebUrl);
               launchIntent.putExtras(activityPars);
               VideosFragment.this.startActivityForResult(launchIntent, Global.INFO_REQUEST);
               break;*/

		case R.id.btnUp:
			if (mMovies != null)
				tmpMovie = mMovies.getNextMovie();

			if (tmpMovie == null) {
				uiSetPopMessage("You are at the begining of the playlist");
			} else if (mPlayer == null) {
				uiSetPopMessage("Can't PLAY video");
			} else {
				uiHoldOnVideo();
				mCurrentMovie = tmpMovie;
				uiDisplayMovieName();
				mPlayer.resetMediaPlayer();
				newMovie2Play = true;
				uiManagePlayPause(R.id.btnPlay);
			}

			break;

		case R.id.btnDown:
			if (mMovies != null)
				tmpMovie = mMovies.getPreviousMovie();

			if (tmpMovie == null) {
				uiSetPopMessage("You are at the end of the playlist");
			} else if (mPlayer == null) {
				uiSetPopMessage("Can't PLAY video");
			} else {
				uiHoldOnVideo();
				mCurrentMovie = tmpMovie;
				uiDisplayMovieName();
				mPlayer.resetMediaPlayer();
				newMovie2Play = true;
				uiManagePlayPause(R.id.btnPlay);
			}
			break;

		case R.id.btnReplay:
			uiResetLog();
			if (mPlayer != null)
				mPlayer.resetMediaPlayer();
			else
				uiSetPopMessage("Can't PLAY video");
			newMovie2Play = true;
			uiManagePlayPause(R.id.btnPlay);
			break;

		

		default:
			break;
		}
	}

	private ImageButton uiBindImageButton(int buttonId) {
		ImageButton ib = (ImageButton) rootView.findViewById(buttonId);
		ib.setOnClickListener(this);
		return ib;
	}

	private void appendAppStrBundle(String key) {
		activityPars.putString(key, appGetStringValue(key));
	}

	@Override
	public void onItemClick(AdapterView<?> arg0, View arg1, int arg2, long arg3) {
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if (resultCode != MainActivity.RESULT_OK)
			return;

		String temp;
		
		   

		if (requestCode == Global.INFO_REQUEST) {
			temp = data.getStringExtra(Global.keyOptResult);

		} else if (requestCode == Global.SETTINGS_REQUEST) {
			temp = data.getStringExtra(Global.keySdkCfgUrl);
			if (temp != null) {
				sdkCfgUrl = temp;
				Log.e(TAG, "sdkCfgUrl changed: " + temp);

			}

			temp = data.getStringExtra(Global.keyAppId);
			if (temp != null) {
				appId = temp;
				Log.e(TAG, "appId changed: " + temp);
			}

			temp = data.getStringExtra(Global.keyAppName);
			if (temp != null) {
				appName = temp;
				Log.e(TAG, "appName changed: " + temp);
			}

			temp = data.getStringExtra(Global.keyAppVer);
			if (temp != null) {
				appVersion = temp;
				Log.e(TAG, "appVersion changed: " + temp);
			}

			temp = data.getStringExtra(Global.keyAppClientId);
			if (temp != null) {
				appClientId = temp;
				Log.e(TAG, "appClientId changed: " + temp);
			}

			temp = data.getStringExtra(Global.keyVideoCensusId);
			if (temp != null) {
				videoCensusId = temp;
				Log.e(TAG, "videoCensusId changed: " + temp);
			}

			temp = data.getStringExtra(Global.keySFcode);
			if (temp != null) {
				sfCode = temp;
				Log.e(TAG, "sfCode changed: " + temp);

			}

			temp = data.getStringExtra(Global.keyMovChanged);
			if (temp != null) {
				Log.e(TAG, "Movies Changed changed: " + temp);
				try {
					mMovies = new MovieList(mContext);
				} catch (Exception e) {
					mMovies = null;
				}
			}
			appSaveAppSettings();
		} else if (requestCode == Global.METATAG_REQUEST) {
			if (data != null) {
				temp = data.getStringExtra(Global.keyMetaTag);
				if (temp != null) {
					ocrMetaData = temp;
					sendOCR = true;
					Log.e(TAG, "OCR changed: " + temp);
					appSaveAppSettings();
				}
			}
		}
	}

	private void updateSeekBar() {
		boolean showIt = ((mPlayer != null) && (mPlayer.isPlaying()) && mPlayer.videoPosition() > 0);
		if (showIt) {
			System.out.println(">>>>>>>>>>> "+mPlayer.videoPosition());
			seekBar.setVisibility(View.VISIBLE);
			seekBar.setMax(mPlayer.videoDuration());
			tvBarGauge.setVisibility(View.VISIBLE);
			BitmapFactory.Options options=new BitmapFactory.Options();
			options.inSampleSize = 1;
			seekBar.setProgress(mPlayer.videoPosition());
			long second = (mPlayer.videoPosition()) % 60;
			long minute = (mPlayer.videoPosition() / 60) % 60;
			long hour = (mPlayer.videoPosition() / (60 * 60)) % 24;
			
			String timeStr;
			if(hour != 0)
				timeStr = String.format(Locale.getDefault(),"%02d:%02d:%02d", hour, minute,
						second);
			else
				timeStr = String.format(Locale.getDefault(),"%02d:%02d",  minute,
						second);
			tvBarGauge.setText(timeStr);
		}
	}

	/******************************************************************************************** APP METHODS ***/
	/*
	 * NielsenUrl: www.nielsen.com PC Content ID: X100zdCIGeIlgZnkYj6UvQ==
	 * [9010, -1554509648] FD Content ID: X100zdCIGeIlgZnkYj6UvQ== [0, 0] Tag
	 * TimeStamp: PC:10/16/2013 03:31:00, FD:01/01/1995 00:00:00 Watermark: Seq:
	 * 2 DATA TAG: NW: 9010, U, 10/16/2013 02:30:47, PC NW: 9010, U, 10/16/2013
	 * 02:30:52, PC N2: 9010, U, 10/16/2013 02:30:52, PC N2: 9010, U, 10/16/2013
	 * 02:30:56, PC NW: 9010, U, 10/16/2013 02:30:57, PC N2: 9010, U, 10/16/2013
	 * 02:31:00, PC PC Offset: 0 FD Offset:0 Stream Type: 00
	 */
	public void appProcessID3tag(final String id3String) {
		uiAppendLog(id3String);
		new Thread(new Runnable(){

			@Override
			public void run() {
				CloudAPI.sendId3(id3String);
			}
			
		}).start();
		
		
	}

	private void appLoadAppSettings() {
		SharedPreferences appData = mContext.getSharedPreferences(
				Global.keyAppData, 0);

		appId = appData.getString(Global.keyAppId, appId);
		appName = appData.getString(Global.keyAppName, appName);
		appVersion = appData.getString(Global.keyAppVer, appVersion);
		appClientId = appData.getString(Global.keyAppClientId, appClientId);
		videoCensusId = appData.getString(Global.keyVideoCensusId,
				videoCensusId);
		sfCode = appData.getString(Global.keySFcode, sfCode);
		sdkCfgUrl = appData.getString(Global.keySdkCfgUrl, sdkCfgUrl);
		ocrMetaData = appData.getString(Global.keyMetaTag, ocrMetaData);
		appDisabled = appData.getBoolean(Global.keyAppDisabled, appDisabled);
	}

	private void appSaveAppSettings() {
		SharedPreferences appData = mContext.getSharedPreferences(
				Global.keyAppData, 0);
		SharedPreferences.Editor editor = appData.edit();

		editor.putString(Global.keyAppId, appId);
		editor.putString(Global.keyAppName, appName);
		editor.putString(Global.keyAppVer, appVersion);
		editor.putString(Global.keyAppClientId, appClientId);
		editor.putString(Global.keyVideoCensusId, videoCensusId);
		editor.putString(Global.keySFcode, sfCode);
		editor.putString(Global.keySdkCfgUrl, sdkCfgUrl);
		editor.putString(Global.keyMetaTag, ocrMetaData);
		editor.putBoolean(Global.keyAppDisabled, appDisabled);

		editor.commit();
	}

	private String appGetStringValue(String key) {
		if (key.equals(Global.keyAppId))
			return appId;

		else if (key.equals(Global.keyAppName))
			return appName;

		else if (key.equals(Global.keyAppVer))
			return appVersion;

		else if (key.equals(Global.keyAppBuiltNo))
			return appBuiltNo;

		else if (key.equals(Global.keyAppClientId))
			return appClientId;

		else if (key.equals(Global.keySFcode))
			return sfCode;

		else if (key.equals(Global.keySdkCfgUrl))
			return sdkCfgUrl;

		else if (key.equals(Global.keyVideoCensusId))
			return videoCensusId;

		else if (key.equals(Global.keyMetaTag))
			return ocrMetaData;

		else if (key.equals(Global.keyWebUrl))
			return optOutUrl;

		else if (key.equals(Global.keyIdfa))
			return advertisingId;
		
		else if (key.equals(Global.keySdkVer))
			return sdkVersion;

		else if (key.equals(Global.keyMpxVersion) && (mPlayer != null))
			return mPlayer.mpxVersion;

		else
			return null;
	}

	private boolean appGetBooleanValue(String key) {
		if (key.equals(Global.keyAppDisabled))
			return appDisabled;
		else
			return false;
	}

	private void appSetStringValue(String key, String value) {
		boolean reInitSDK = false;

		if (key.equals(Global.keyAppId))
			appId = value;

		else if (key.equals(Global.keyAppName))
			appName = value;

		else if (key.equals(Global.keyAppVer))
			appVersion = value;

		else if (key.equals(Global.keyAppBuiltNo))
			appBuiltNo = value;

		else if (key.equals(Global.keyAppClientId))
			appClientId = value;

		else if (key.equals(Global.keySFcode)) {
			sfCode = value;
			reInitSDK = true;
		}

		else if (key.equals(Global.keySdkCfgUrl)) {
			sdkCfgUrl = value;
			reInitSDK = true;
		}

		else if (key.equals(Global.keyVideoCensusId))
			videoCensusId = value;

		else if (key.equals(Global.keyNuid))
			nuid = value;

		else if (key.equals(Global.keyIdfa))
			idfa = advertisingId;

		else if (key.equals(Global.keyWebUrl))
			optOutUrl = value;

		else if (key.equals(Global.keyOptResult)) {

		}

		else if (key.equals(Global.keyMovChanged)) {
			try {
				mMovies = new MovieList(mContext);
			} catch (Exception e) {
				mMovies = null;
			}
		}

	}

	private void appSetBooleanValue(String key, boolean value) {
		if (key.equals(Global.keyAppDisabled)) {
			appDisabled = value;

		}
	}

	private void appStartMeteringVideo() {
		if (mMovies == null)
			return;

		mCurrentMovie = mMovies.getCurrentMovie();

		if (mCurrentMovie == null)
			return;

		String metaData = "{\"type\" : \"content\"," + "\"assetId\":\""
				+ mCurrentMovie.getName() + "\", "
				+ "\"program\":\"MyProgram\"," + "\"tv\":\""
				+ mCurrentMovie.getTvParam() + "\", "
				+ "\"title\":\"MyEpisode\"," + "\"category\":\"testCategory\",";

		String tmpDataSrc = mCurrentMovie.getDataSrc();
		String tmpAdModel = mCurrentMovie.getAdModel();

		if (!tmpDataSrc.equals("") && !tmpAdModel.equals(""))
			metaData += "\"dataSrc\":\"" + tmpDataSrc + "\","
					+ "\"adModel\":\"" + tmpAdModel + "\",";

		metaData += "\"length\":\"6000\"" + "}";

		Log.i(TAG, metaData);

		String tag = "{ \"channelName\" : \"" + mCurrentMovie.getName() + " - "
				+ mCurrentMovie.getUrl() + "\" }";

		new Thread(new Runnable(){

			@Override
			public void run() {
				 CloudAPI.loadMetadata();
				 uiAppendLog("loadMetadata...");
			}
			
		}).start();
		durationHandler.postDelayed(updateSeekBarTime, 100);
		
		getActivity().getActionBar().setTitle(mCurrentMovie.getName());
	}

	private void appStopMeteringVideo() {
		Log.d(TAG, "StopMeteringVideo");
		
		if(sessionId != null){
			new Thread(new Runnable(){
	
				@Override
				public void run() {
					CloudAPI.stopId3();
					uiAppendLog("ID3 stopped!");
				}
				
			}).start();
		}
		durationHandler.removeCallbacks(updateSeekBarTime);
	}

	private void appEmailLogs() {
		StringBuilder sdkContent;
		StringBuilder logContent = null;

		SimpleDateFormat sdf = new SimpleDateFormat("EEEE");
		Date d = new Date();
		String dayOfTheWeek = sdf.format(d);

		String sdkFiName = "ErrorReport-" + dayOfTheWeek.substring(0, 3)
				+ ".txt";
		File file = new File("/data/data/" + appPkgName + "/files/log/"
				+ sdkFiName);
		sdkContent = new StringBuilder();

		try {
			BufferedReader br = new BufferedReader(new FileReader(file));
			String line;

			while ((line = br.readLine()) != null) {
				sdkContent.append(line);
				sdkContent.append('\n');
			}
			br.close();
		} catch (IOException e) {
			// You'll need to add proper error handling here
		}

		if (appLogSize > 0) {
			file = new File(appLogFileLocation);
			if (file.exists() && file.canRead()) {
				logContent = new StringBuilder();
				logContent
						.append("* * * * * * * * * LOG CONTENT * * * * * * * * *\n");

				try {
					BufferedReader br = new BufferedReader(new FileReader(file));
					String line;

					while ((line = br.readLine()) != null) {
						logContent.append(line);
						logContent.append('\n');
					}
					br.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		File root = getAppRoot();
		File logFile = new File(root, sdkFiName);
		FileWriter writer;
		try {
			writer = new FileWriter(logFile);
			writer.write(sdkContent.toString());

			if (logContent != null)
				writer.write(logContent.toString());

			writer.flush();
			writer.close();
		} catch (IOException e3) {
			e3.printStackTrace();
		}

		Intent launchIntent = new Intent(Intent.ACTION_SEND);
		launchIntent.setType("text/plain");
		launchIntent.putExtra(Intent.EXTRA_EMAIL,
				new String[] { "email@example.com" });
		launchIntent.putExtra(Intent.EXTRA_SUBJECT, "Log Report");
		launchIntent.putExtra(Intent.EXTRA_TEXT, "Please open attached file.");
		launchIntent.putExtra(Intent.EXTRA_STREAM, Uri.fromFile(logFile));

		Log.d(TAG, "Finished creating attachment");

		startActivity(Intent.createChooser(launchIntent, "Send email..."));
	}

	private void appAppendLogFile(String logEntry) {
		appLogSize += logEntry.length();

		try {
			File root = getAppRoot();
			File logFile = new File(root, LOG_FILENAME);
			appLogFileLocation = logFile.getAbsolutePath();
			FileWriter writer = new FileWriter(logFile, true);
			writer.append(logEntry);
			writer.flush();
			writer.close();
			Log.i(TAG, "Logs saved on --> " + logFile.getAbsolutePath());
		} catch (IOException e) {
			e.printStackTrace();
			Log.i(TAG, "Exception in saving LogFile");
		}
	}

	private void appResetLogFile() {
		File root = getAppRoot();
		File logFile = new File(root, LOG_FILENAME);
		if (logFile != null && logFile.exists()) {
			logFile.delete();
			Log.i(TAG, "Remove Log File --> " + logFile.getAbsolutePath());
		}
		appLogSize = 0;
	}

	private File getAppRoot() {
		File appRoot = new File(Environment.getExternalStorageDirectory(),
				appActivity.getPackageName());
		if (!appRoot.exists())
			if (!appRoot.mkdirs())
				return null;

		return appRoot;
	}

	//
	public void setPlayerActive(boolean playerActive) {
		if (!playerActive) {
			newMovie2Play = true;
			mPlayer = null;
		}
	}

	public static void onNetworkUpdated(boolean networkActive) {
		if (!networkActive) {
			if (mMainActivity != null && mPlayer != null
					&& (mPlayer.isPlaying() || mPlayer.isActivated())) {
				mMainActivity.getActivity().runOnUiThread(new Runnable() {
					public void run() {
						try {
							mMainActivity.uiResetPlayPause();
						} catch (Exception e) {
							// TODO: more logging?
						}
					}
				});
			}
		}
	}

	class AppProgressDialog extends ProgressDialog {
		public AppProgressDialog(Context context) {
			super(context);
		}

		@Override
		public void onStop() {
			mProgressDialogLock.lock();
			mProgressDialog = null;
			mProgressDialogLock.unlock();
		}
	}

	/*************************************************************************************** ACTIVITY METHODS ***/

	public void destroy() {
		mMainActivity = null;
		Log.i(TAG, "onDestroy");
		mProgressDialogLock.lock();
		if (mProgressDialog != null) {
			if (mProgressDialog.isShowing())
				mProgressDialog.dismiss();
			mProgressDialog = null;
		}
		mProgressDialogLock.unlock();

		mAlertDialogLock.lock();
		if (alert != null) {
			if (alert.isShowing())
				alert.dismiss();
			alert = null;
		}
		mAlertDialogLock.unlock();

		if (null != mPlayer) {
			try {
				mPlayer.releaseMediaPlayer();
				mPlayer = null;
			} catch (Exception e) {
				// TODO: more logging
			}
		}
	}

	@Override
	public void onDestroy() {

		Log.i(TAG, "onDestroy");

		destroy();

		super.onDestroy();
	}
	
	
	
	 private class LongOperation extends AsyncTask<String, Void, String> {

	        @Override
	        protected String doInBackground(String... params) {
	        	try {
	                AdInfo adInfo = AdvertisingIdClient.getAdvertisingIdInfo(mContext);
	                 advertisingId = adInfo.getId();                
	                 Log.i(TAG, "advertisingId  "+advertisingId);

	                 //Once adID is found, create session
	                 uiAppendLog("advertising ID: " + advertisingId);
	                 
	                 sessionId = CloudAPI.sessionInit(appId, advertisingId, appName);
	                 uiAppendLog("sessionID: " + sessionId);

	                 
	            } catch (Exception e) {
	                e.printStackTrace();                            
	            }  
	            return advertisingId;
	        }
	        
	        
	      
	 }
	 
	 
}
