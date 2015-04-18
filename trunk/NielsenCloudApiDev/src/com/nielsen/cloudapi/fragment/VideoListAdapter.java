package com.nielsen.cloudapi.fragment;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nielsen.cloudapi.activity.R;
import com.nielsen.cloudapi.model.DatabaseHelper;

public class VideoListAdapter extends BaseAdapter {

	private Context mContext;

	public VideoListAdapter(Context c) {
		mContext = c;
	}
	
	@Override
	public int getCount() {
		return 5;
	}

	@Override
	public Object getItem(int position) {
		return null;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		TextView tv;
		//if (convertView == null) {
			Drawable drawable = parent.getResources().getDrawable(R.drawable.sample_0);
			tv = new TextView(mContext);
			drawable.setBounds(20, 20, 20, 20);
			tv.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
			DatabaseHelper dbHelper = new DatabaseHelper(mContext, parent.getResources().getString(R.string.DB_TABLE_NAME), null, 1);
			String name = dbHelper.getNameById(position);
			Log.d("VideoListAdapter", name);
			tv.setText(name);
		/*} else {
			tv = (TextView) convertView;
		}*/

		return tv;
	}

}
