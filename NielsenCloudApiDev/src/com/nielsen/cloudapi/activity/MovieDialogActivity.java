package com.nielsen.cloudapi.activity;

/*
 * 29.Mar.14    LFR    Changed extends Activity to masterActivity for Background detection
 * 10.Apr.14    LFR    Validate Name & Url
 */

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.URLUtil;
import android.widget.Button;
import android.widget.EditText;

import com.nielsen.cloudapi.activity.R;
import com.nielsen.cloudapi.model.Global;

public class MovieDialogActivity extends MasterActivity {

    private EditText txtMovieName, txtMovieDataSrc, txtMovieAdModel, txtBreakOut, txtTvParam, txtMovieUrl;
    private AlertDialog alert = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movie_dialog);

        Button btnSave = (Button) findViewById(R.id.btnMovieSave);
        if (btnSave == null)
            bailOut(0);

        btnSave.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
                String movieName = txtMovieName.getText().toString();
                String movieUrl = txtMovieUrl.getText().toString();
                String errMsg = "";
                boolean validUrl = ( URLUtil.isFileUrl(movieUrl) || URLUtil.isContentUrl(movieUrl) ||
                        URLUtil.isHttpUrl(movieUrl) || URLUtil.isNetworkUrl(movieUrl) || URLUtil.isDataUrl(movieUrl)) && 
                        ((movieUrl != null) && (!movieUrl.isEmpty()) && (movieUrl.length() > 14) && (movieUrl.contains(".m3u8")));

                if (movieName == null || movieName.isEmpty())
                    errMsg = "No Content Name was specified.";
                else if (!validUrl)
                    errMsg = "Invalid URL";

                if (!errMsg.isEmpty()) {
                    final AlertDialog.Builder builder = new AlertDialog.Builder(MovieDialogActivity.this);
                    builder.setMessage(errMsg).setTitle("Edit Context").setCancelable(false)
                            .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                                public void onClick(DialogInterface dialog, int which) 
                                {
                                    alert.dismiss();
                                }
                            });

                    alert = builder.create();
                    alert.show();
                } else {
                    bailOut(1);
                }
            }
        });

        Button btnCancel = (Button) findViewById(R.id.btnMovieCancel);
        if (btnCancel == null)
            bailOut(0);

        btnCancel.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
                bailOut(0);
            }
        });

        Button btnRemove = (Button) findViewById(R.id.btnMovieRemove);
        if (btnRemove == null)
            bailOut(0);
        
        btnRemove.setOnClickListener(new OnClickListener() {
            public void onClick(View v)
            {
                bailOut(-1);
            }
        });

        final Bundle extras = getIntent().getExtras();
        if (extras == null)
            bailOut(0);

        if (extras.getInt(Global.keyMovIdx, 0) < 0)
            btnRemove.setVisibility(View.INVISIBLE);
        else
            btnRemove.setVisibility(View.VISIBLE);

        txtMovieName = (EditText) findViewById(R.id.txtMovieName);
        if (txtMovieName == null)
            bailOut(0);
        txtMovieName.setText(extras.getString(Global.keyMovName));

        txtMovieDataSrc = (EditText) findViewById(R.id.txtMovieDataSrc);
        if (txtMovieDataSrc == null)
            bailOut(0);
        txtMovieDataSrc.setText(extras.getString(Global.keyDataSrc));

        txtMovieAdModel = (EditText) findViewById(R.id.txtMovieAdModel);
        if (txtMovieAdModel == null)
            bailOut(0);
        txtMovieAdModel.setText(extras.getString(Global.keyAdModel));

        txtBreakOut = (EditText) findViewById(R.id.txtBreakOut);
        if (txtBreakOut == null)
            bailOut(0);
        txtBreakOut.setText(extras.getString(Global.keyBreakOut));

        txtTvParam = (EditText) findViewById(R.id.txtTvParam);
        if (txtTvParam == null)
            bailOut(0);
        txtTvParam.setText(extras.getString(Global.keyTvParam));

        txtMovieUrl = (EditText) findViewById(R.id.txtMovieUrl);
        if (txtMovieUrl == null)
            bailOut(0);
        txtMovieUrl.setText(extras.getString(Global.keyMovUrl));
    }

    private void bailOut(int actionFlag)
    {
        Intent i = new Intent();
        if (actionFlag != 0 && i != null) {
            final Bundle pars = new Bundle();
            pars.putInt(Global.keyActivitAction, actionFlag);
            pars.putString(Global.keyMovName, txtMovieName.getText().toString());
            pars.putString(Global.keyDataSrc, txtMovieDataSrc.getText().toString());
            pars.putString(Global.keyAdModel, txtMovieAdModel.getText().toString());
            pars.putString(Global.keyBreakOut, txtBreakOut.getText().toString());
            pars.putString(Global.keyTvParam, txtTvParam.getText().toString());
            pars.putString(Global.keyMovUrl, txtMovieUrl.getText().toString());
            i.putExtras(pars);
            setResult(RESULT_OK, i);
        } else if (i == null) {
            setResult(RESULT_CANCELED);
        } else {
            setResult(RESULT_CANCELED, i);
        }

        finish();
    }
}
