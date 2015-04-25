package com.nielsen.cloudapi.fragment;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.nielsen.cloudapi.activity.MoviesActivity;
import com.nielsen.cloudapi.activity.R;
import com.nielsen.cloudapi.model.Global;

public class SettingsFragment extends Fragment {
	private Button btnMovies;
	private Intent launchIntent;
	private boolean moviesChanged = false;
	private String sdkCfgUrl, appName, appId, appVersion, videoCensusId,
			appClientId, ccCode;
	private EditText txt_sdkCfgUrl, txt_appName, txt_appId, txt_appVersion,
			txt_videoCensusId, txt_appClientId, txt_ccCode;
	private Intent retIntent;

	public SettingsFragment() {
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {

		final View rootView = inflater.inflate(R.layout.fragment_settings, container,
				false);
		final Bundle extras = this.getArguments();

		txt_sdkCfgUrl = (EditText) rootView.findViewById(R.id.txtSdkCfgUrl);
		sdkCfgUrl = extras.getString(Global.keySdkCfgUrl);
		txt_sdkCfgUrl.setText(sdkCfgUrl);

		txt_appId = (EditText) rootView.findViewById(R.id.txtAppId);
		appId = extras.getString(Global.keyAppId);
		txt_appId.setText(appId);

		txt_appName = (EditText) rootView.findViewById(R.id.txtAppName);
		appName = extras.getString(Global.keyAppName);
		txt_appName.setText(appName);

		txt_appVersion = (EditText) rootView.findViewById(R.id.txtAppVersion);
		appVersion = extras.getString(Global.keyAppVer);
		txt_appVersion.setText(appVersion);

		txt_appClientId = (EditText) rootView.findViewById(R.id.txtClientId);
		appClientId = extras.getString(Global.keyAppClientId);
		txt_appClientId.setText(appClientId);

		txt_videoCensusId = (EditText) rootView.findViewById(R.id.txtVcId);
		videoCensusId = extras.getString(Global.keyVideoCensusId);
		txt_videoCensusId.setText(videoCensusId);

		txt_ccCode = (EditText) rootView.findViewById(R.id.txtCcode);
		ccCode = extras.getString(Global.keySFcode);
		txt_ccCode.setText(ccCode);

		btnMovies = (Button) rootView.findViewById(R.id.btnStreamUrls);
		btnMovies.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				launchIntent = new Intent(rootView.getContext(),
						MoviesActivity.class);
				getActivity().startActivityForResult(launchIntent,
						Global.SETTINGS_REQUEST);
			}
		});
		return rootView;
	}

	private void eval2SendField(String sample, EditText field, String key) {
		String fieldVal = field.getText().toString();
		if ((sample != null) && (!sample.equals(fieldVal)))
			retIntent.putExtra(key, fieldVal);
	}

	private void bailOut() {
		retIntent = new Intent();

		if (moviesChanged)
			retIntent.putExtra(Global.keyMovChanged, "1");

		eval2SendField(appId, txt_appId, Global.keyAppId);
		eval2SendField(appName, txt_appName, Global.keyAppName);
		eval2SendField(appVersion, txt_appVersion, Global.keyAppVer);
		eval2SendField(appClientId, txt_appClientId, Global.keyAppClientId);
		eval2SendField(videoCensusId, txt_videoCensusId,
				Global.keyVideoCensusId);
		eval2SendField(ccCode, txt_ccCode, Global.keySFcode);
		eval2SendField(sdkCfgUrl, txt_sdkCfgUrl, Global.keySdkCfgUrl);

		getActivity().setResult(Activity.RESULT_OK, retIntent);
		getActivity().finish();
	}

	public void onBackPressed() {
		bailOut();
	}

	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);

		if ((requestCode == Global.SETTINGS_REQUEST)
				&& (resultCode == Activity.RESULT_OK)) {
			String tmp = data.getStringExtra(Global.keyMovChanged);
			if (tmp != null) {
				moviesChanged = true;
			}
		}
	}
}
