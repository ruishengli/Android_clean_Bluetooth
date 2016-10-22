package com.inlook.android_clean_bluetooth.data.util;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * NetWorkUtil
 *
 * @author or
 * @since 2016/10/21.
 */

public class NetWorkUtil {

    public static boolean isInternetConnection(Context context) {
        boolean isConnected;

        ConnectivityManager connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo  networkInfo = connectivityManager.getActiveNetworkInfo();

        isConnected = (networkInfo != null && networkInfo.isConnectedOrConnecting());
        return isConnected;
    }
}
