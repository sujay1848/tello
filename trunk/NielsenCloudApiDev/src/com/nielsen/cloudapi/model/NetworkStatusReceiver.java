package com.nielsen.cloudapi.model;

import com.nielsen.cloudapi.fragment.VideosFragment;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

public class NetworkStatusReceiver extends BroadcastReceiver
{
    public static boolean getConnectedStatus(Context context) 
    {
        ConnectivityManager cm = (ConnectivityManager) context
                .getSystemService(Context.CONNECTIVITY_SERVICE);
 
        NetworkInfo activeNetworkInfo = cm.getActiveNetworkInfo();

        return (activeNetworkInfo != null && activeNetworkInfo.isConnected());
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        VideosFragment.onNetworkUpdated(getConnectedStatus(context));
    }
}
