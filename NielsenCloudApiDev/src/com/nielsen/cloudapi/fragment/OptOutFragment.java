package com.nielsen.cloudapi.fragment;

/*
 * 30.Mar.14	LFR		Changed extends Activity to masterActivity for Background detection
 */

import android.app.Fragment;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings.LayoutAlgorithm;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import com.nielsen.cloudapi.activity.MainActivity;
import com.nielsen.cloudapi.activity.R;
import com.nielsen.cloudapi.model.Global;

public class OptOutFragment extends Fragment {
	private final String TAG = OptOutFragment.class.getSimpleName();

	private Button pgBack, pgClose;

	private WebView mWebView;
	private View view;
	
	private ProgressDialog dialog;

	public View onCreateView(LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		view = inflater.inflate(R.layout.opt_out, container, false);

		pgBack = (Button) view.findViewById(R.id.btnOptOutBack);
		if (pgBack != null) {
			pgBack.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					bailOut(null);
				}
			});
		}
		
		pgClose = (Button) view.findViewById(R.id.btnOptOutClose);
		if (pgClose != null) {
			pgClose.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					bailOut("closeAll");
				}
			});
		}
		
		final Bundle extras = this.getArguments();

		String url = extras.getString(Global.keyWebUrl);		
		mWebView = (WebView) view.findViewById(R.id.webView);
		if (mWebView != null) {
			mWebView.getSettings().setJavaScriptEnabled(true);
	
			// Handle webview scaling
			mWebView.setInitialScale(1);
			mWebView.getSettings().setBuiltInZoomControls(true);
			mWebView.getSettings().setSupportZoom(true);
			mWebView.getSettings().setDisplayZoomControls(false);
			mWebView.getSettings().setLoadWithOverviewMode(true);
			mWebView.getSettings().setUseWideViewPort(true);
			mWebView.getSettings().setLayoutAlgorithm(LayoutAlgorithm.SINGLE_COLUMN);
			mWebView.setWebViewClient(new MonitorWebView());
			mWebView.setWebChromeClient(new WebChromeClient());
	
			Log.d("WEB", "Launching: " + url);
			mWebView.loadUrl(url);
		}
		
		return view;
	}

	private void bailOut(String val) {
		Intent i = new Intent();
		i.putExtra(Global.keyOptResult, val);
		getActivity().setResult(MainActivity.RESULT_OK, i);
		//finish();
	}

	private class MonitorWebView extends WebViewClient {
		public void onPageFinished(WebView view, String url) {
			Log.d(TAG, "FINISHED LOADING: " + url);
			cancelDialog();
		};

		public void cancelDialog() {
			if (dialog != null) {
				dialog.cancel();
				dialog = null;
			}
		}

		@Override
		public boolean shouldOverrideUrlLoading(WebView view, String url) {
			Log.d(TAG, "shouldOverrideUrlLoading: " + url);

			if (url != null && url.indexOf("nielsen") == 0) {
				bailOut(url);
				return false;
			}
			else {
				dialog = ProgressDialog.show(view.getContext(), "OptOut", "Loading...");
				// view.loadUrl(url);
				return true;
			}

		}
	}
	public void onBackPressed()
	{
		bailOut(null);
	}
}
