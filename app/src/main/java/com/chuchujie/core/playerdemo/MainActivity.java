package com.chuchujie.core.playerdemo;

import android.arch.lifecycle.LifecycleOwner;
import android.os.Bundle;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.ImageButton;

import com.chuchujie.core.player.PlayerActivity;
import com.chuchujie.core.player.view.PlayerView;

import java.io.File;

public class MainActivity extends AppCompatActivity implements LifecycleOwner {

    private PlayerView player_view;
    private static final String TAG = "test";
    private int noWifiNum = 0;
    private int playerNum = 0;
    private int volumeNum = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        player_view = (PlayerView) findViewById(R.id.playerview);
        getLifecycle().addObserver(player_view);

        // player_view.getPlayerView().setDefaultArtwork(BitmapFactory.decodeResource(getResources(),R.drawable.player_ec_pause));
        ImageButton i = new ImageButton(this);
        i.setBackgroundColor(getResources().getColor(R.color.colorPrimary));
        player_view.getBg_artwork().addView(i);
        player_view.addPlayerStatistic(new PlayerView.Statistic() {
            @Override
            public void withoutWifiClickPlay() {
                //Log.i(TAG, "非wifi--点击了" + ++noWifiNum);
                //Toast.makeText(MainActivity.this, "非wifi--点击了" + noWifiNum, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onClickPlay() {
                //Log.i(TAG, "播放--点击了" + ++playerNum);
                //Toast.makeText(MainActivity.this, "播放--点击了" + playerNum, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onVolumeClick() {
                //Log.i(TAG, "声音--点击了" + ++volumeNum);
                //Toast.makeText(MainActivity.this, "声音--点击了" + volumeNum, Toast.LENGTH_SHORT).show();
            }
        });
        player_view
                .setAlwaysShowStatus(true)
//                .setVideoUrl("https://vod.cc.163.com/file/5856574fda67d358bafcb545.mp4").startPlay((NetworkUtils.isWifiAvailable(this)));
//                .setVideoUrl("https://live-data.chuchujie.com/vcutter/v/cctu/201712/25/94eccc626489385ec1b67c842bc71bf4a43be491.mp4")
//                .setVideoUrl("ht、tps://vod.cc.163.com/file/5856574fda67d358bafcb545.mp4");
//                .setVideoUrl("https://live-data.chuchujie.com/vcutter/v/pdetail/201805/30/55361bed88555ce6cf1dbee8d6ccefa0525f808c.mp4")
//                .setVideoUrl("https://live-data.chuchujie.com/vcutter/v/pdetail/201805/31/1149d82692185b99c21a4fd31f43bca6fdecf5a6.mp4")
                .setVideoUrl("https://live-data.chuchujie.com/vcutter/v/pdetail/201806/14/38e83db8c9d728f577e4c0df86cc02e52f3f5756.mp4")
                .startPlay(NetworkUtils.isWifiAvailable(this));
        findViewById(R.id.pause).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (player_view.getController().isPause()) {
                    player_view.getController().resumePlay();
                } else {
                    player_view.getController().pausePlay();
                }
            }
        });
        findViewById(R.id.reset).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                player_view.getController().stopPlay();
            }
        });

    }

    public void playNative(View view) {
        PlayerActivity.launch(this, Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "1.mp4",
                "111");
       // player_view.startPlay();
    }

    public void playUrl(View view) {
//        PlayerActivity.launch(this, "https://7xsh0r.com1.z0.glb.clouddn.com/%E8%A7%86%E9%A2%91%E4%B8%80%E6%9C%89%E5%AD%97%E5%B9%95min.m3u8");
//        PlayerActivity.launch(this, "https://live-data.chuchujie.com/vcutter/v/cctu/201712/25/94eccc626489385ec1b67c842bc71bf4a43be491.mp4");
//        PlayerActivity.launch(this, "https://oss.newaircloud.com/xjdcb/video/201712/27/8e25f847-d172-4221-982a-0ff60d0942c2.mp4");
        PlayerActivity.launch(this, "https://vod.cc.163.com/file/5856574fda67d358bafcb545.mp4","ddd",true);
    }

}
