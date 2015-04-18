package com.nielsen.cloudapi.fragment;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.GridView;
import android.widget.Toast;

import com.nielsen.cloudapi.activity.R;

public class MyMoviesFragment extends Fragment {
	
	@Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		final View rootView = inflater.inflate(R.layout.fragment_pages, container, false);
		GridView gridView = (GridView) rootView.findViewById(R.id.videoGridview);
		gridView.setAdapter(new VideoListAdapter(rootView.getContext()));

		gridView.setOnItemClickListener(new OnItemClickListener() {
			public void onItemClick(AdapterView<?> parent, View v, int position, long id) {
				Toast.makeText(rootView.getContext(), "" + position,
						Toast.LENGTH_SHORT).show();
			}
		});
		return rootView;
	}
}
