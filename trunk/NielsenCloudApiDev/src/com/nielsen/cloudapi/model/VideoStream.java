package com.nielsen.cloudapi.model;

import java.util.Date;

/**
 * POJO for holding the serialized video info from the SQLite DB. See
 * {@link DatabaseHelper}
 * 
 * @author sanjankar
 *
 */
public class VideoStream implements Comparable<VideoStream> {

	public String displayName;
	public String streamUrl;
	public int rating;
	public Date lastPlayed;

	public VideoStream(String displayName, String streamUrl, int rating, Date lastPlayed) {
		this.displayName = displayName;
		this.streamUrl = streamUrl;
		this.rating = rating;
		this.lastPlayed = lastPlayed;
	}
	
	public String getDisplayName() {
		return displayName;
	}

	public void setDisplayName(String displayName) {
		this.displayName = displayName;
	}

	public String getStreamUrl() {
		return streamUrl;
	}

	public void setStreamUrl(String streamUrl) {
		this.streamUrl = streamUrl;
	}

	public int getRating() {
		return rating;
	}

	public void setRating(int rating) {
		this.rating = rating;
	}

	public Date getLastPlayed() {
		return lastPlayed;
	}

	public void setLastPlayed(Date lastPlayed) {
		this.lastPlayed = lastPlayed;
	}

	@Override
	public int compareTo(VideoStream another) {
		if (this.lastPlayed.after(another.lastPlayed))
			return -1;
		else if (this.lastPlayed.equals(another.lastPlayed))
			return 0;
		else
			return 1;
	}
}
