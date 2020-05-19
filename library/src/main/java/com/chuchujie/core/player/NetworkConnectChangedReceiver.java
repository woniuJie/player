package com.chuchujie.core.player;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.chuchujie.core.player.view.PlayerView;

public class NetworkConnectChangedReceiver extends BroadcastReceiver {

    private PlayerView playerView;

    public NetworkConnectChangedReceiver(PlayerView playerView) {
        this.playerView = playerView;
    }

    @Override
    public void onReceive(Context context, Intent intent) {

        if (intent != null && WifiManager.WIFI_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            int wifiState = intent.getIntExtra(WifiManager.EXTRA_WIFI_STATE, 0);
            switch (wifiState) {
                case WifiManager.WIFI_STATE_DISABLED:
                case WifiManager.WIFI_STATE_DISABLING:
                    if (playerView != null) {
                        playerView.setWifi(false);
                    }
                    break;
                case WifiManager.WIFI_STATE_ENABLED:
                case WifiManager.WIFI_STATE_ENABLING:
                    if (playerView != null) {
                        playerView.setWifi(true);
                    }
                    break;
                case WifiManager.WIFI_STATE_UNKNOWN:
                    break;
            }
        } else if (WifiManager.NETWORK_STATE_CHANGED_ACTION.equals(intent.getAction())) {
            ConnectivityManager connectivity = (ConnectivityManager) context
                    .getSystemService(Context.CONNECTIVITY_SERVICE);
            NetworkInfo.State wifiState = connectivity.getNetworkInfo(
                    ConnectivityManager.TYPE_WIFI).getState();
            // CONNECTING, CONNECTED, SUSPENDED, DISCONNECTING, DISCONNECTED, UNKNOWN
            switch (wifiState) {
                case CONNECTED:
                    break;
                case CONNECTING:
                    break;
                case DISCONNECTED:
                    //无网络状态 暂停播放
                    if (playerView != null && playerView.getController() != null) {
                        playerView.getController().pausePlay();
                    }
                    break;
                case DISCONNECTING:
                    break;
                case UNKNOWN:
                    break;
                case SUSPENDED:
                    break;
            }
        }
    }

}
