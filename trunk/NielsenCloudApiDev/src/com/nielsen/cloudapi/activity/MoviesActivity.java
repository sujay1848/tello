package com.nielsen.cloudapi.activity;

/*
 * 30.Mar.14    LFR    Changed extends ListActivity to masterActivity for Background detection
 *                     therefore it require to create all the components that are created using ListActivity
 *                     including to create movies_activity layout
 * 05.May.14    LFR    Added AdModel & DataSrc
 */

import java.util.ArrayList;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.nielsen.cloudapi.model.Global;
import com.nielsen.cloudapi.model.MovieItem;
import com.nielsen.cloudapi.model.MovieList;


public class MoviesActivity extends MasterActivity
{
    private final String TAG = MoviesActivity.class.getSimpleName();
    private final String NEW_ENTRY = "+ + + ADD NEW CONTENT + + +";
    private ListView movListView;
    private MovieList movies;
    private ArrayList<String> movieNames;
    private int targetIdx;
    private ArrayAdapter<String> adapter;
    private boolean moviesChanged = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.movies_activity);

        movListView = (ListView) findViewById(R.id.listOfMovies);
        try {
            movies = new MovieList(getApplicationContext());
        } catch (Exception e){
            //TODO: add handling;
        }

        loadList();

        movListView.setOnItemClickListener(new OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String mName, dataSrc, adModel, breakOut, tvParam, mUrl;
                targetIdx = position;
                if (position == movieNames.size() - 1) {
                    mName        = "";
                    dataSrc  = "";
                    adModel  = "";
                    breakOut = "";
                    tvParam  = "";
                    mUrl        = "";
                    targetIdx    = -1;
                } else {
                    MovieItem targetMovie = movies.getMovieAtIndex(position);
                    mName    = targetMovie.getName();
                    dataSrc  = targetMovie.getDataSrc();
                    adModel  = targetMovie.getAdModel();
                    breakOut = targetMovie.getBreakOut();
                    tvParam  = targetMovie.getTvParam();
                    mUrl     = targetMovie.getUrl();
                }

                Intent launchIntent = new Intent(MoviesActivity.this, MovieDialogActivity.class);
                final Bundle pars = new Bundle();

                pars.putInt(Global.keyMovIdx, targetIdx);
                pars.putString(Global.keyMovName, mName);
                pars.putString(Global.keyDataSrc, dataSrc);
                pars.putString(Global.keyAdModel, adModel);
                pars.putString(Global.keyBreakOut, breakOut);
                pars.putString(Global.keyTvParam, tvParam);
                pars.putString(Global.keyMovUrl, mUrl);
                launchIntent.putExtras(pars);

                MoviesActivity.this.startActivityForResult(launchIntent, Global.MOV_DIALOG_REQUEST);
            }
        });
        Log.e(TAG,"onCreate MoviesActivity");
    }

    private void loadList()
    {
        if (movies != null) {
            movieNames = movies.getListOfNames2(NEW_ENTRY);
            adapter = new ArrayAdapter<String>(this, R.layout.movie_entry, R.id.movieName, movieNames);
            movListView.setAdapter(adapter);
        }
    }

    private void bailOut()
    {
        Intent i = new Intent();

        if (moviesChanged)
            i.putExtra(Global.keyMovChanged, "1");

        setResult(RESULT_OK, i);
        finish();
    }

    public void onBackPressed()
    {
        bailOut();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);

        if ((requestCode == Global.MOV_DIALOG_REQUEST) && (resultCode == RESULT_OK) && (movies != null)) {
            int action = data.getIntExtra(Global.keyActivitAction, 0);
            if (action == 1) { // Save
                String mName    = data.getStringExtra(Global.keyMovName);
                String dataSrc  = data.getStringExtra(Global.keyDataSrc);
                String adModel  = data.getStringExtra(Global.keyAdModel);
                String breakOut = data.getStringExtra(Global.keyBreakOut);
                String tvParam  = data.getStringExtra(Global.keyTvParam);
                String mUrl     = data.getStringExtra(Global.keyMovUrl);

                if ((mName != null) && !mName.equals("") && (mUrl != null) && !mUrl.equals("")) {
                    if (movies.putMovieItem(mName, dataSrc, adModel, breakOut, tvParam, mUrl, targetIdx))
                        action = 0;
                }
            } else if (action == -1) { // Remove
                movies.removeMovieItem(targetIdx);
            }

            if (action != 0) { // != Cancel/Do Nothing
                loadList();
                adapter.notifyDataSetChanged();
                moviesChanged = true;
            }
        }
    }
}
