package com.nielsen.cloudapi.adapter;

import java.util.Collections;
import java.util.List;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.nielsen.cloudapi.activity.R;
import com.nielsen.cloudapi.model.DatabaseHelper;
import com.nielsen.cloudapi.model.VideoStream;

public class HistoryListAdapter extends BaseAdapter {

	private Context context;
	private List<VideoStream> loadedHistory;
	
	public HistoryListAdapter(Context c) {
		this.context = c;
	}
	
	@Override
	public int getCount() {
		return 0;
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
		if (convertView == null) {
            LayoutInflater inflater = (LayoutInflater) context
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.movie_items, null);
        }
		Drawable drawable = parent.getResources().getDrawable(R.drawable.sample_0);
		tv = new TextView(context);
		drawable.setBounds(20, 20, 20, 20);
		List<VideoStream> sortedHistoryList = getHistoryList(parent);
		VideoStream currentListItem = sortedHistoryList.get(position);
		tv.setText("Name: " + currentListItem.getDisplayName() + "Last Played On: " + currentListItem.getLastPlayed());
		tv.setCompoundDrawablesWithIntrinsicBounds(null, drawable, null, null);
		return tv;
	}
	
	private List<VideoStream> getHistoryList(ViewGroup parent) {
		if (loadedHistory != null) {
			DatabaseHelper dbHelper = new DatabaseHelper(context, parent
					.getResources().getString(R.string.DB_TABLE_NAME), null, 1);
			loadedHistory = dbHelper.loadHistoryList();
		}
		Collections.sort(loadedHistory);
		return loadedHistory;
	}
	
}
