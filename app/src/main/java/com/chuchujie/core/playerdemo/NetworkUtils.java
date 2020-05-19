package com.chuchujie.core.playerdemo;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.text.TextUtils;

import java.net.MalformedURLException;
import java.net.URL;

public class NetworkUtils {
    private static final int NETWORK_TYPE_UNAVAILABLE = -1;
    private static final int NETWORK_TYPE_WIFI = -101;
    private static final int NETWORK_CLASS_WIFI = -101;
    private static final int NETWORK_CLASS_UNAVAILABLE = -1;
    private static final int NETWORK_CLASS_UNKNOWN = 0;
    private static final int NETWORK_CLASS_2_G = 1;
    private static final int NETWORK_CLASS_3_G = 2;
    private static final int NETWORK_CLASS_4_G = 3;
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    public static final int NETWORK_TYPE_GPRS = 1;
    public static final int NETWORK_TYPE_EDGE = 2;
    public static final int NETWORK_TYPE_UMTS = 3;
    public static final int NETWORK_TYPE_CDMA = 4;
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    public static final int NETWORK_TYPE_EVDO_A = 6;
    public static final int NETWORK_TYPE_1xRTT = 7;
    public static final int NETWORK_TYPE_HSDPA = 8;
    public static final int NETWORK_TYPE_HSUPA = 9;
    public static final int NETWORK_TYPE_HSPA = 10;
    public static final int NETWORK_TYPE_IDEN = 11;
    public static final int NETWORK_TYPE_EVDO_B = 12;
    public static final int NETWORK_TYPE_LTE = 13;
    public static final int NETWORK_TYPE_EHRPD = 14;
    public static final int NETWORK_TYPE_HSPAP = 15;

    private NetworkUtils() {
    }

    public static ConnectivityManager getConnManager(Context context) {
        return (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    public static boolean isConnected(Context context) {
        try {
            NetworkInfo net = getConnManager(context).getActiveNetworkInfo();
            return net != null && net.isConnected();
        } catch (Exception var2) {
            return false;
        }
    }

    public static boolean isConnectedOrConnecting(Context context) {
        NetworkInfo[] nets = getConnManager(context).getAllNetworkInfo();
        if (nets != null) {
            NetworkInfo[] var2 = nets;
            int var3 = nets.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                NetworkInfo net = var2[var4];
                if (net.isConnectedOrConnecting()) {
                    return true;
                }
            }
        }

        return false;
    }


    public static boolean isWifiAvailable(Context context) {
        NetworkInfo[] nets = getConnManager(context).getAllNetworkInfo();
        if (nets != null) {
            NetworkInfo[] var2 = nets;
            int var3 = nets.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                NetworkInfo net = var2[var4];
                if (net.getType() == 1) {
                    return net.isAvailable();
                }
            }
        }

        return false;
    }

    public static boolean isMobileAvailable(Context context) {
        NetworkInfo[] nets = getConnManager(context).getAllNetworkInfo();
        if (nets != null) {
            NetworkInfo[] var2 = nets;
            int var3 = nets.length;

            for(int var4 = 0; var4 < var3; ++var4) {
                NetworkInfo net = var2[var4];
                if (net.getType() == 0) {
                    return net.isAvailable();
                }
            }
        }

        return false;
    }







    public static String getHostFromUrl(String url) {
        try {
            return (new URL(url)).getHost();
        } catch (MalformedURLException var2) {
            var2.printStackTrace();
            return "";
        }
    }

    public static String getHost(String urlStr) {
        if (TextUtils.isEmpty(urlStr)) {
            return "";
        } else {
            Uri uri = Uri.parse(urlStr);
            return uri != null ? uri.getHost() : "";
        }
    }


}
