package com.nielsen.cloudapi.model;

import java.util.ArrayList;

import android.content.Context;
import android.content.SharedPreferences;

public class MovieList
{
    private int mMovieIdx = 0;
    public ArrayList<MovieItem> mList = new ArrayList<MovieItem>();
    private Context mContext = null;
    
    /***************************************************************************************************************************************** 
	 * The defaultList contains the HLS/VOD URL streams to be played using this sample app.
	 * Please provide the LIVE/VOD URL streams in the following format and pass it to defaultList string variable
	 * "Stream/Channel Name;cms;0;00,true,Stream/Channel URL"
	 * If you do not have any Stream/Channel to be played then you can remove the comment below and enable the default urls provided by Nielsen 
	 * Note: Nielsen default url's are just test streams and sometimes they may not work in external networks or could be down due to any other issues
	  ******************************************************************************************************************************************/	
    //Note: You MUST use a hardcoded bitrate in these index files for them to play on the MPX player
    public final static String defaultList = "NielsenConsumer;id3;0;00;true;http://www.nielseninternet.com/NielsenConsumer/prog_index_500.m3u8^" +
    		"ASEAN;id3;0;00;true;http://www.nielseninternet.com/ASEAN/prog_index_500.m3u8^" +
    		"GlobalConsumer;id3;0;00;true;http://www.nielseninternet.com/NielsenGlobalConsumer/prog_index_500.m3u8^" +
    		"NielsenOCR;id3;0;00;true;http://www.nielseninternet.com/NielsenOCR/prog_index_500.m3u8^" +
    		"NielsenXPlatform;id3;0;00;true;http://www.nielseninternet.com/NielsenXPlatform/prog_index_500.m3u8^" +
    		"BIG_BUCK;id3;0;00;true;http://www.nielseninternet.com/BBB/prog_index_500.m3u8";

    public MovieList(Context context) throws Exception
    {
        if (context == null)
            throw new Exception("context == null");

        mContext = context;
        SharedPreferences movData = mContext.getSharedPreferences(Global.keyAppData, 0);
        if (movData == null)
            throw new Exception("Can't get shared preferences");

        String archivedData = movData.getString(Global.keyMovieData, defaultList);

        if (archivedData == null)
            return;

        String[] defaultMovies = archivedData.split("\\^"); 

        mList = new ArrayList<MovieItem>();

        for (int j=0; j < defaultMovies.length; j++) {
            String[] nameUrl = defaultMovies[j].split(";");
            if (nameUrl.length >= MovieItem.PARAM_COUNT && mList != null) {
                MovieItem tmp = new MovieItem(nameUrl[0], nameUrl[1], nameUrl[2], nameUrl[3], nameUrl[4], nameUrl[5]);
                mList.add(tmp);
            }
        }
        
        System.out.println(mList);
    }

    public MovieItem getNextMovie()
    {
        int nextIdx = this.mMovieIdx + 1;
        if (mList == null || nextIdx >= mList.size()) {
            return null;
        } else {
            this.mMovieIdx = nextIdx;
            return getMovieAtIndex(this.mMovieIdx);
        }
    }

    public MovieItem getPreviousMovie()
    {
        int nextIdx = this.mMovieIdx - 1;
        if (nextIdx < 0) {
            return null;
        } else {
            this.mMovieIdx = nextIdx;
            return getMovieAtIndex(this.mMovieIdx);
        }
    }

    public MovieItem getCurrentMovie()
    {
        return getMovieAtIndex(this.mMovieIdx);
    }

    public MovieItem getMovieAtIndex(int idx)
    {
        if ((mList == null) || (idx < 0) || (idx >= mList.size()))
            return null;
        else
            return(mList.get(idx));
    }

    public String[] getListOfNames(String tagEntryStr)
    {
        boolean tagNewEntry = false;

        if (tagEntryStr == null)
            tagEntryStr = "";

        if (!tagEntryStr.equals(""))
            tagNewEntry = true;

        int reqSize = mList.size() + (tagNewEntry ? 1 : 0);

        String[] retNames = new String[reqSize];
        for (int j = 0; j < mList.size(); j++)
            retNames[j] = mList.get(j).getName();

        if (tagNewEntry)
            retNames[reqSize-1] = tagEntryStr;

        return retNames;
    }

    public ArrayList<String> getListOfNames2(String tagEntryStr)
    {
        ArrayList<String> retNames = new ArrayList<String>();
        for (int j = 0; j < mList.size(); j++)
            retNames.add(mList.get(j).getName());

        if (tagEntryStr == null)
            tagEntryStr = "";

        if (!tagEntryStr.equals(""))
            retNames.add(tagEntryStr);

        return retNames;
    }

    public synchronized boolean putMovieItem(String movieName, String dataSrc, String adModel, String breakOut, String tvParam, String movieUrl, int index)
    {
        if (mList == null)
            return false;

        try {
            MovieItem tmp = new MovieItem(movieName, dataSrc, adModel, breakOut, tvParam, movieUrl);
            if ((index < 0) || (index >= mList.size()))
                mList.add(tmp);
            else
                mList.set(index, tmp);
        } catch (Exception e) {
            return false;
        }

        return saveMoviesList();
    }

    public synchronized boolean removeMovieItem(int index)
    {
        if ((mList == null) || (index < 0) || (index >= mList.size()) || (mList.size() <= 1))
            return false;

        try {
            mList.remove(index);
        } catch (Exception e) {
            return false;
        }
        return saveMoviesList();
    }

    public boolean saveMoviesList()
    {
        if (mList == null)
            return false;

        String packedArray = "";
        for (int j = 0; j < mList.size(); j++) {
            packedArray += 
                    (j > 0 ? "^" : "") + 
                    mList.get(j).getName() + ";" + 
                    mList.get(j).getDataSrc() + ";" +
                    mList.get(j).getAdModel() + ";" +
                    mList.get(j).getBreakOut() + ";" +
                    mList.get(j).getTvParam() + ";" +
                    mList.get(j).getUrl();
        }

        SharedPreferences appData = mContext.getSharedPreferences(Global.keyAppData, 0);
        SharedPreferences.Editor editor = null;

        if (appData != null)
            editor = appData.edit();

        if (editor != null) {
            editor.putString(Global.keyMovieData, packedArray);
            return editor.commit();
        }

        return false;
    }
}

