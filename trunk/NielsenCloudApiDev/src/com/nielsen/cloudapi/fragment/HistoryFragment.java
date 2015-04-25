package com.nielsen.cloudapi.fragment;

import android.app.Fragment;
import android.app.FragmentTransaction;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;

import com.nielsen.cloudapi.activity.R;
import com.nielsen.cloudapi.adapter.VideoListAdapter;
import com.nielsen.cloudapi.model.Global;

public class HistoryFragment extends Fragment {
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_history, container, false);
		GridView gridView = (GridView) rootView.findViewById(R.id.historyGridview);
		gridView.setAdapter(new VideoListAdapter(rootView.getContext(), true));

		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
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
			    MyMoviesFragment fragment= new MyMoviesFragment();
			    MyMoviesFragment.UpdateVideoToHistory u = fragment. new UpdateVideoToHistory();
			    u.execute(Integer.toString(position));
			    // Commit the transaction
			    transaction.commit();
			}
		});
		return rootView;
	}
}
