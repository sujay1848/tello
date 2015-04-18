package com.nielsen.cloudapi.model;

/*
 * 30.Mar.14	LFR		Changed extends Activity to masterActivity for Background detection
 */

import com.nielsen.cloudapi.activity.MasterActivity;
import com.nielsen.cloudapi.activity.R;
import com.nielsen.cloudapi.fragment.OptOutFragment;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.TextView;

public class InfoActivity extends MasterActivity
{
	private final String TAG = InfoActivity.class.getSimpleName();

	private Button pgBack, pgOptOut;

	private String optOutPage;


	private String optOutRequest	= "";


    protected void onCreate(Bundle savedInstanceState)
	{

		super.onCreate(savedInstanceState);
		setContentView(R.layout.info);

        final Bundle extras = getIntent().getExtras();

        optOutPage	= extras.getString(Global.keyWebUrl);

        TextView txtV = (TextView) findViewById(R.id.appName);
		

   
		txtV.setText(extras.getString(Global.keyNuid));

		

		pgBack = (Button) findViewById(R.id.btnInfoBack);
		pgBack.setOnClickListener(new OnClickListener()
		{
            public void onClick(View v)
			{
				bailOut();
			}
		});

		pgOptOut = (Button) findViewById(R.id.btnOptOut);
		pgOptOut.setOnClickListener(new OnClickListener()
		{
            public void onClick(View v)
			{
				if ((optOutPage == null) || (optOutPage.isEmpty()))
				{
	                final AlertDialog.Builder builder = new AlertDialog.Builder(InfoActivity.this);
	                builder.setMessage("No Opt-Out Url is available.")
	                       .setTitle("Launch Opt-Out page")
	                       .setCancelable(false)
	                       .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
	                           @Override
	                           public void onClick(DialogInterface dialog, int which) {
	                        	   dialog.cancel();
	                           }
	                       });
	                builder.show();
				}
				else
				{
					Intent i = new Intent(InfoActivity.this, OptOutFragment.class);
					final Bundle pars = new Bundle();
					pars.putString(Global.keyWebUrl, optOutPage);
					i.putExtras(pars);
					InfoActivity.this.startActivityForResult(i, Global.OPTOUT_REQUEST);
				}
			}
		});
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

	    if ((requestCode == Global.OPTOUT_REQUEST) && (data != null))
	    {
	    	String result = data.getStringExtra(Global.keyOptResult);
	    	if (result != null)
	    	{
		    	if (result.indexOf("nielsenappsdk") == 0)
	        	{
		    		optOutRequest  = result; //result.substring(result.length()-1);
	        		Log.d(TAG, "OPT-OUT " + optOutRequest);
	        	}
	        	else if (result.equals("closeAll"))
	        	{
	            	Log.d(TAG, "close " + result);
	            	bailOut();
	        	}
	    	}
	    }
	}


	private synchronized void bailOut()
	{
       	Intent i = new Intent();
       	if (!optOutRequest.equals(""))
       		i.putExtra(Global.keyOptResult, optOutRequest);

       	setResult(RESULT_OK, i);
		finish();
	}

    public void onBackPressed()
	{
		bailOut();
	}


}
