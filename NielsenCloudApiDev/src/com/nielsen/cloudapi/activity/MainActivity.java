package com.nielsen.cloudapi.activity;

import java.util.ArrayList;

import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.nielsen.cloudapi.adapter.NavDrawerItem;
import com.nielsen.cloudapi.adapter.NavDrawerListAdapter;
import com.nielsen.cloudapi.fragment.HistoryFragment;
import com.nielsen.cloudapi.fragment.MyMoviesFragment;
import com.nielsen.cloudapi.fragment.OptOutFragment;
import com.nielsen.cloudapi.fragment.SettingsFragment;
import com.nielsen.cloudapi.fragment.VideosFragment;
import com.nielsen.cloudapi.model.DatabaseHelper;
import com.nielsen.cloudapi.model.Global;
import com.nielsen.cloudapi.model.MovieList;
import com.nielsen.cloudapi.model.Player;

@SuppressWarnings("deprecation")
public class MainActivity extends MasterActivity {
	private DrawerLayout mDrawerLayout;
	private ListView mDrawerList;
	private static boolean sendOCR = false;
	private ActionBarDrawerToggle mDrawerToggle;
	private final String TAG = MainActivity.class.getSimpleName();
	
	// nav drawer title
	private CharSequence mDrawerTitle;

	// used to store app title
	private CharSequence mTitle;
	private static boolean appDisabled = false;
	private static String appName, appId, appVersion, appBuiltNo, videoCensusId, appClientId;
	private static String ocrMetaData;
	private static String advertisingId;
	public static Player mPlayer;
	private static String sdkVersion = "0.0";
	private static String optOutUrl = "http://secure-uat-cert.imrworldwide.com/nielsen_app_optout.html";
	private static String sfCode = "uat-cert";// "uat-cert"; //"qatdpr"; // "us"
	private static String sdkCfgUrl = ""; // http://www.nielseninternet.com/id3sdk/config-hyu-drm-stnId.txt
	
	// slide menu items
	private String[] navMenuTitles;
	private TypedArray navMenuIcons;

	private ArrayList<NavDrawerItem> navDrawerItems;
	private NavDrawerListAdapter adapter;
	
	//Shared preferences to be used across the app.
	SharedPreferences prefs;
	SharedPreferences.Editor edit;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		prefs = this.getSharedPreferences(getResources().getString(R.string.SHARED_PREFERENCES_KEY), Context.MODE_APPEND);
		if (!prefs.contains(getResources().getString(R.string.DB_INITIALIZED_FLAG))) {
			DatabaseHelper dbHelper = new DatabaseHelper(this, getResources().getString(R.string.DB_TABLE_NAME), null, 1);
			dbHelper.init(this);
			dbHelper.getDbStatus();
		}
		setContentView(R.layout.activity_main);

		mTitle = mDrawerTitle = getTitle();

		// load slide menu items
		navMenuTitles = getResources().getStringArray(R.array.nav_drawer_items);

		// nav drawer icons from resources
		navMenuIcons = getResources()
				.obtainTypedArray(R.array.nav_drawer_icons);

		mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
		mDrawerList = (ListView) findViewById(R.id.list_slidermenu);

		navDrawerItems = new ArrayList<NavDrawerItem>();
		
		// adding nav drawer items to array
		int i =0;
		for(String name : navMenuTitles){
			System.out.println(name);
			if(name.equalsIgnoreCase("Videos")){
				try {
					navDrawerItems.add(new NavDrawerItem(name, navMenuIcons.getResourceId(i, -1),true,""+new MovieList(this).mList.size()));
				} catch (Exception e) {
					navDrawerItems.add(new NavDrawerItem(name, navMenuIcons.getResourceId(i, -1)));
					e.printStackTrace();
				}
			}else{
				navDrawerItems.add(new NavDrawerItem(name, navMenuIcons.getResourceId(i, -1)));
			}
			i++;
		}
		// Recycle the typed array
		navMenuIcons.recycle();
		
		
		PackageManager manager = getPackageManager();
		try {
			PackageInfo appInfo = manager.getPackageInfo(getPackageName(),
					0);
			appVersion = appInfo.versionName;
			appBuiltNo = Integer.toString(appInfo.versionCode);
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		}

		//Insert your own name here
		appName = getString(R.string.app_name);
		//Insert your own AppID here
		appId = getString(R.string.app_id);
		videoCensusId = "AppSampleVcId";
		appClientId = "Sujay Anjankar";

		ocrMetaData = "{\"type\":\"ad\","
				+ "\"nol_ocrtag\":\"http://secure-aws.imrworldwide.com/cgi-bin/m?ci=ENT29825&am=3&ep=1&at=view&rt=banner&st=image&ca=1234&cr=crv194436&pc=plc1234320\" }";
		appLoadAppSettings();

		mDrawerList.setOnItemClickListener(new SlideMenuClickListener());

		// setting the nav drawer list adapter
		adapter = new NavDrawerListAdapter(getApplicationContext(),
				navDrawerItems);
		mDrawerList.setAdapter(adapter);

		// enabling action bar app icon and behaving it as toggle button
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);

		mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
				R.drawable.ic_drawer, //nav menu toggle icon
				R.string.app_name, // nav drawer open - description for accessibility
				R.string.app_name // nav drawer close - description for accessibility
		) {
			public void onDrawerClosed(View view) {
				getActionBar().setTitle(mTitle);
				// calling onPrepareOptionsMenu() to show action bar icons
				invalidateOptionsMenu();
			}

			public void onDrawerOpened(View drawerView) {
				getActionBar().setTitle(mDrawerTitle);
				// calling onPrepareOptionsMenu() to hide action bar icons
				invalidateOptionsMenu();
			}
		};
		mDrawerLayout.setDrawerListener(mDrawerToggle);

		if (savedInstanceState == null) {
			// on first time display view for first nav item
			displayView(0);
		}
	}
	
	/**
	 * Slide menu item click listener
	 * */
	private class SlideMenuClickListener implements
			ListView.OnItemClickListener {
		@Override
		public void onItemClick(AdapterView<?> parent, View view, int position,
				long id) {
			// display view for selected nav drawer item
			displayView(position);
		}
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// toggle nav drawer on selecting action bar app icon/title
		if (mDrawerToggle.onOptionsItemSelected(item)) {
			return true;
		}
		// Handle action bar actions click
		switch (item.getItemId()) {
		case R.id.action_settings:
			displayView(1);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	/* *
	 * Called when invalidateOptionsMenu() is triggered
	 */
	@Override
	public boolean onPrepareOptionsMenu(Menu menu) {
		// if nav drawer is opened, hide the action items
		boolean drawerOpen = mDrawerLayout.isDrawerOpen(mDrawerList);
		menu.findItem(R.id.action_settings).setVisible(!drawerOpen);
		return super.onPrepareOptionsMenu(menu);
	}

	/**
	 * Diplaying fragment view for selected nav drawer list item
	 * */
	private void displayView(int position) {
		// update the main content by replacing fragments
		Fragment fragment = null;
		switch (position) {
		case 0:
			Bundle videoFragmentBundle = new Bundle();
			videoFragmentBundle.putInt(Global.videoId, -1);
			
			fragment = new VideosFragment();
			fragment.setArguments(videoFragmentBundle);
			break;
		case 1:
			fragment = new MyMoviesFragment();
			break;
		case 2:
			fragment = new HistoryFragment();
			break;
		case 3:
			Bundle fragOptoutFragment = new Bundle();
			fragment = new OptOutFragment();
			appendAppStrBundle(fragOptoutFragment, Global.keyWebUrl);
			fragment.setArguments(fragOptoutFragment);
			break;
		case 4:
			Bundle fragSettingsBundle = new Bundle();
			fragment = new SettingsFragment();
			appendAppStrBundle(fragSettingsBundle, Global.keySdkCfgUrl);
			appendAppStrBundle(fragSettingsBundle, Global.keyAppId);
			appendAppStrBundle(fragSettingsBundle, Global.keyAppName);
			appendAppStrBundle(fragSettingsBundle, Global.keyAppVer);
			appendAppStrBundle(fragSettingsBundle, Global.keyAppClientId);
			appendAppStrBundle(fragSettingsBundle, Global.keyVideoCensusId);
			appendAppStrBundle(fragSettingsBundle, Global.keySFcode);
			appendAppStrBundle(fragSettingsBundle, Global.keyAdModel);
			appendAppStrBundle(fragSettingsBundle, Global.keyDataSrc);
			fragment.setArguments(fragSettingsBundle);
			break;
		
			
		

		default:
			break;
		}

		if (fragment != null) {
			FragmentManager fragmentManager = getFragmentManager();
			fragmentManager.beginTransaction()
					.replace(R.id.frame_container, fragment).commit();

			// update selected item and title, then close the drawer
			mDrawerList.setItemChecked(position, true);
			mDrawerList.setSelection(position);
			setTitle(navMenuTitles[position]);
			mDrawerLayout.closeDrawer(mDrawerList);
		} else {
			// error in creating fragment
			Log.e("MainActivity", "Error in creating fragment");
		}
	}

	@Override
	public void setTitle(CharSequence title) {
		mTitle = title;
		getActionBar().setTitle(mTitle);
	}

	/**
	 * When using the ActionBarDrawerToggle, you must call it during
	 * onPostCreate() and onConfigurationChanged()...
	 */

	@Override
	protected void onPostCreate(Bundle savedInstanceState) {
		super.onPostCreate(savedInstanceState);
		// Sync the toggle state after onRestoreInstanceState has occurred.
		mDrawerToggle.syncState();
	}

	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
		// Pass any configuration change to the drawer toggls
		mDrawerToggle.onConfigurationChanged(newConfig);
	}
	
	private void appendAppStrBundle(Bundle bundle, String key) {
		bundle.putString(key, appGetStringValue(key));
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
	
	private void appLoadAppSettings() {
		SharedPreferences appData = getSharedPreferences(
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
			/*if (temp != null) {
				Log.e(TAG, "Movies Changed changed: " + temp);
				try {
					mMovies = new MovieList(this);
				} catch (Exception e) {
					mMovies = null;
				}
			}*/
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
	
	private void appSaveAppSettings() {
		SharedPreferences appData = getSharedPreferences(
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
}
