package com.nielsen.cloudapi.model;

/**
 * POJO for holding the serialized video info from the SQLite DB.
 * See {@link DatabaseHelper}
 * @author sanjankar
 *
 */
public class VideoStream {

	public String DISPLAY_NAME;
	public String STREAM_URL;
	public String ID3_TAG;
	public int RATING;
	public int COUNT;

	public String getDISPLAY_NAME() {
		return DISPLAY_NAME;
	}

	public void setDISPLAY_NAME(String dISPLAY_NAME) {
		DISPLAY_NAME = dISPLAY_NAME;
	}

	public String getSTREAM_URL() {
		return STREAM_URL;
	}

	public void setSTREAM_URL(String sTREAM_URL) {
		STREAM_URL = sTREAM_URL;
	}

	public String getID3_TAG() {
		return ID3_TAG;
	}

	public void setID3_TAG(String iD3_TAG) {
		ID3_TAG = iD3_TAG;
	}

	public int getRATING() {
		return RATING;
	}

	public void setRATING(int rATING) {
		RATING = rATING;
	}

	public int getCOUNT() {
		return COUNT;
	}

	public void setCOUNT(int cOUNT) {
		COUNT = cOUNT;
	}
}
