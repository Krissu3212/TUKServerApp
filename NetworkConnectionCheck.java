package com.example.tukserveriapp;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Log;

public class NetworkConnectionCheck {

    public boolean checkConnection(Status status) {
        ConnectivityManager conManager = (ConnectivityManager) status.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo wifiConn = conManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        NetworkInfo mobileConn = conManager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);

        if ((wifiConn != null && wifiConn.isConnected()) || (mobileConn != null && mobileConn.isConnected())) {
            Log.d("info", "Network detected");
            return true;
        } else {
            Log.d("info", "Network not detected");
            return false;
        }
    }
}
