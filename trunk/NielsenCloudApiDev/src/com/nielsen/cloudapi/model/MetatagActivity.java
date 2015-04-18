package com.nielsen.cloudapi.model;

/*
 * 29.Mar.14   LFR    Changed extends Activity to masterActivity for Background detection
 */

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;

import com.nielsen.cloudapi.activity.MasterActivity;
import com.nielsen.cloudapi.activity.R;

public class MetatagActivity extends MasterActivity {

    private EditText mTxt_metaTag;
    private String mMetaTag;
    private Button mBtnSubmit;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.metatag_activity);

        boolean correct_load = true;
        final Bundle extras = getIntent().getExtras();
        mTxt_metaTag = (EditText) findViewById(R.id.txtMetaTag);
        mBtnSubmit = (Button) findViewById(R.id.btnSubmitMetaTag);

        if (extras == null || mTxt_metaTag == null || mBtnSubmit == null)
            correct_load = false;

        if(correct_load) {
            mMetaTag = extras.getString(Global.keyMetaTag);
            if (mMetaTag != null) {
                mTxt_metaTag.setText(mMetaTag);
                mBtnSubmit.setOnClickListener(new OnClickListener() {
                    public void onClick(View v)
                    {
                        Intent retIntent = new Intent();
                        retIntent.putExtra(Global.keyMetaTag, mTxt_metaTag.getText().toString());

                        setResult(RESULT_OK, retIntent);
                        finish();
                    }
                });
            } else {
                correct_load = false;
            }
        }
        if (!correct_load) {
            Intent retIntent = new Intent();
            setResult(RESULT_CANCELED, retIntent);
            finish();
        }
    }
}
