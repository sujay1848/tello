package com.nielsen.cloudapi.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.nielsen.cloudapi.activity.R;
import com.nielsen.cloudapi.adapter.VideoListAdapter;
import com.nielsen.cloudapi.model.DatabaseHelper;
import com.nielsen.cloudapi.model.Global;

public class MyMoviesFragment extends Fragment {
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_mymovies, container, false);
		GridView gridView = (GridView) rootView.findViewById(R.id.videoGridview);
		gridView.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> parent, View view,
					int position, long id) {
				 	Fragment newFragment = new VideosFragment(); 
				    // consider using Java coding conventions (upper first char class names!!!)
				    FragmentTransaction transaction = getFragmentManager().beginTransaction();
				    Bundle videoFragmentBundle = new Bundle();
				    videoFragmentBundle.putInt(Global.videoId, position);
				    newFragment.setArguments(videoFragmentBundle);
				    // Replace whatever is in the fragment_container view with this fragment,
				    // and add the transaction to the back stack
				    transaction.replace(R.id.frame_container, newFragment);
				    transaction.addToBackStack(null);
				    // Commit the transaction
				    new UpdateVideoToHistory().execute(Integer.toString(position));
				    transaction.commit();
			}
		});
		gridView.setAdapter(new VideoListAdapter(rootView.getContext(), false));
		return rootView;
	}
	
	public class UpdateVideoToHistory extends AsyncTask<String, String, String> {
		@Override
		protected String doInBackground(String... params) {
			DatabaseHelper db = new DatabaseHelper(getActivity(), getActivity()
					.getResources().getString(R.string.DB_TABLE_NAME), null, 1);
			String name = db.getNameById(Integer.parseInt(params[0]));
			db.updateLastPlayed(name);
			Log.d(MyMoviesFragment.class.getName(), name);
			return null;
		}
	}
}
