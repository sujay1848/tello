package com.nielsen.cloudapi.adapter;

import java.util.Locale;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.nielsen.cloudapi.activity.R;
import com.nielsen.cloudapi.model.DatabaseHelper;
import com.nielsen.cloudapi.model.MovieList;

/**
 * Adapter to populate {@link MyVideosFragment}.
 * @author sanjankar
 *
 */
public class VideoListAdapter extends BaseAdapter {

	private Context mContext;
	private static String THUMBNAIL_PREFIX = "thumb_";
	private String[] nameArray = null;
	private final boolean isHistory;

	public VideoListAdapter(Context c, boolean isHistory) {
		mContext = c;
		this.isHistory = isHistory;
		loadMovieItems();
	}
	
	private void loadMovieItems() {
		if (nameArray == null) {
			DatabaseHelper dbHelper = new DatabaseHelper(mContext, mContext.getResources().getString(R.string.DB_TABLE_NAME), null, 1);
			if (isHistory)
				nameArray = dbHelper.getHistoryList();
			else 
				nameArray = dbHelper.getMovieNames();
		}
	}

	@Override
	public int getCount() {
		return nameArray != null ? nameArray.length : 0;
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
            LayoutInflater inflater = (LayoutInflater) mContext
                    .getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            convertView = inflater.inflate(R.layout.movie_items, null);
        }
 
		tv = (TextView) convertView
                .findViewById(R.id.movie_name);
        ImageView thumbImage = (ImageView) convertView.findViewById(R.id.thumb);
		if(nameArray != null){
			String name = nameArray[position];
			Log.d("VideoListAdapter", name);
			tv.setText(name);
			
			// Capture position and set to the ImageView
			int id = mContext.getResources().getIdentifier(THUMBNAIL_PREFIX + name.toLowerCase(Locale.US), "drawable", mContext.getPackageName());
			if (id == 0) {
				id = R.drawable.thumb_nielsenconsumer;
			}
			thumbImage.setImageResource(id);
		}
        return convertView;
	}

}
