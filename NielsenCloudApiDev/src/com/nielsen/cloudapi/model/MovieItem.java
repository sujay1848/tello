package com.nielsen.cloudapi.model;

/*
 * 05.May.14    LFR    Added fields DataSrc & AdModel to the Movie 
 * 12.May.14    LFR    Added fields breakOut & tvParam to the Movie 
 */

public class MovieItem
{
    private String mMovieName;
    private String mUrlStr;
    private String mDataSrc;
    private String mAdModel;
    private String mBreakOut;
    private String mTvParam;
    public static final int PARAM_COUNT = 6;

    public MovieItem(String _movieName, String _dataSrc, String _adModel,
            String _breakOut, String _tvParam, String _movieUrl)
    {
        this.mMovieName = _movieName != null ? _movieName : "";
        this.mDataSrc = _dataSrc != null ? _dataSrc : "";
        this.mAdModel = _adModel != null ? _adModel : "";
        this.mBreakOut = _breakOut != null ? _breakOut : "";
        this.mTvParam = _tvParam != null ? _tvParam : "";
        this.mUrlStr = _movieUrl != null ? _movieUrl : "";
    }

    public String getName()
    {
        return mMovieName;
    }

    public void setName(String newName)
    {
        this.mMovieName = newName;
    }

    public String getDataSrc()
    {
        return mDataSrc;
    }

    public void setDataSrc(String newDataSrc)
    {
        this.mDataSrc = newDataSrc;
    }

    public String getAdModel()
    {
        return mAdModel;
    }

    public void setAdModel(String newAdModel)
    {
        mAdModel = newAdModel;
    }

    public String getBreakOut()
    {
        return mBreakOut;
    }

    public void setBreakOut(String newBreakOut)
    {
        this.mBreakOut = newBreakOut;
    }

    public String getTvParam()
    {
        return mTvParam;
    }

    public void setTvParam(String newTvParam)
    {
        this.mTvParam = newTvParam;
    }

    public String getUrl()
    {
        return mUrlStr;
    }

    public void setUrl(String url)
    {
        this.mUrlStr = url;
    }
}
