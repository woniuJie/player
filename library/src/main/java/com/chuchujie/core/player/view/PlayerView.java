package com.chuchujie.core.player.view;

import android.arch.lifecycle.Lifecycle;
import android.arch.lifecycle.LifecycleObserver;
import android.arch.lifecycle.OnLifecycleEvent;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.chuchujie.core.player.R;
import com.google.android.exoplayer2.ui.PlaybackControlView;
import com.google.android.exoplayer2.ui.SimpleExoPlayerView;


/**
 * Created by wangjing on 2018/5/11.
 */
public class PlayerView extends FrameLayout implements LifecycleObserver,
        PlaybackControlView.VisibilityListener, View.OnClickListener {

    public SimpleExoPlayerView mSimpleExoPlayerView;
    FrameLayout bg_artwork;
    ImageView exo_play;
    ImageView exo_pause;
    private PlayerController mPlayerController;
    private ImageView ec_play_volume;
    //默认 静音播放
    private boolean isCloseVolume;
    private ProgressBar progressBar;
    //是否直接进行缓存 wifi下缓存并播放  其他模式下 不缓存停止播放
    private boolean isWifi = true;
    private boolean mClickedPlayButton = false;
    private Statistic statistic;
    private Handler mHandler = new Handler();
    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            exo_play.setVisibility(GONE);
            exo_pause.setVisibility(GONE);
        }
    };


    public PlayerView(@NonNull Context context) {
        super(context);
        init(context, null);
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs) {
        super(context, attrs);
        init(context, attrs);
    }

    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init(context, attrs);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public PlayerView(@NonNull Context context, @Nullable AttributeSet attrs,
                      int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
        init(context, attrs);
    }

    public SimpleExoPlayerView getPlayerView() {
        return mSimpleExoPlayerView;
    }

    public ProgressBar getProgressBar() {
        return progressBar;
    }

    public void init(Context context, AttributeSet attrs) {
        // 加载播放器布局
        inflate(getContext(), R.layout.player_view_layout, this);
        // 解析自定义属性
        includeStyleAttr(context, attrs);
        //初始化View
        initView();
        initListener();
        // 创建播放器的控制器
        mPlayerController = new PlayerController(this, ec_play_volume);
        // 初始化播放器
        mPlayerController.initPlayer();
        //更新音量播放UI
        updateVolumeUI();
    }

    private void updateVolumeUI() {
        if (isCloseVolume) {
            mPlayerController.closeVolume(ec_play_volume);
        } else {
            mPlayerController.openVolume(ec_play_volume, 0);
        }
    }

    private void includeStyleAttr(Context context, AttributeSet attrs) {
        if (attrs != null) {
            TypedArray typedArray = context.obtainStyledAttributes(attrs, R.styleable
                    .player_player_view, 0, 0);

            //mVideoTitle = typedArray.getString(R.styleable.simplePlayerView_videoTitle);
            //isAutoPlay = typedArray.getBoolean(R.styleable
            // .player_simplePlayerView_player_isAutoPlay, false);
            isCloseVolume = typedArray.getBoolean(R.styleable
                    .player_player_view_player_isCloseVolume, true);
            //isShowDebugView = typedArray.getBoolean(R.styleable
            // .simplePlayerView_isShowDebugView, false);
            typedArray.recycle();
        } else {
            //初始化默认值
            isCloseVolume = true;
        }

    }

    private void initListener() {
        ec_play_volume.setOnClickListener(this);
        exo_play.setOnClickListener(this);
        exo_pause.setOnClickListener(this);
    }

    private void clickExoPlayerViewListener() {
        if (mPlayerController.isPause()) {
            exo_play.setVisibility(VISIBLE);
            exo_pause.setVisibility(GONE);
        } else {
            exo_pause.setVisibility(VISIBLE);
            exo_play.setVisibility(GONE);
        }
        mHandler.postDelayed(mRunnable, 2000);
    }

    private void initView() {
        mSimpleExoPlayerView = findViewById(R.id.player_view);
        mSimpleExoPlayerView.setControllerVisibilityListener(this);
        mSimpleExoPlayerView.requestFocus();
        progressBar = findViewById(R.id.ec_progress);
        progressBar.setVisibility(View.GONE);
        bg_artwork = findViewById(R.id.bg_artwork);
        bg_artwork.setVisibility(VISIBLE);
        exo_play = mSimpleExoPlayerView.findViewById(R.id.exo_play);
        exo_pause = findViewById(R.id.exo_pause1);
        ec_play_volume = mSimpleExoPlayerView.findViewById(R.id.ec_play_volume);
        if (mSimpleExoPlayerView.getOverlayFrameLayout() != null) {
            mSimpleExoPlayerView.getOverlayFrameLayout().setOnClickListener(new OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getController().isDurationMoreThan4000() && ! getController().isPlayDone()) {
                        clickExoPlayerViewListener();
                    }
                }
            });
        }

    }

    public boolean isWifi() {
        return isWifi;
    }

    public void setWifi(boolean wifi) {
        isWifi = wifi;
    }

    public FrameLayout getBg_artwork() {
        return bg_artwork;
    }

    public void setBg_artwork(FrameLayout bg_artwork) {
        this.bg_artwork = bg_artwork;
    }

    public View getPlayIconView() {
        return exo_play;
    }

    public View getPauseIconView(){
        return exo_pause;
    }
    public ImageView getVoiceIconView() {
        return ec_play_volume;
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
    public void onResume() {
        mPlayerController.resumePlay();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
    public void onPause() {
        mPlayerController.pausePlay();
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
    public void onDestroy() {
        if (mPlayerController == null) {
            return;
        }
        mPlayerController.stopPlay();
        mPlayerController.onDestroy();
    }

    public PlayerController getController() {
        return mPlayerController;
    }

    /**
     * start play video by video url
     *
     * @param videoUrl
     * @return
     */
    public PlayerView startPlay(String videoUrl) {
        getController().setPlayUrl(videoUrl);
        getController().startPlay(true);
        return this;
    }

    public PlayerView setVideoUrl(String videoUrl) {
        getController().setPlayUrl(videoUrl);
        return this;
    }

    public PlayerView startPlay(boolean isWifi) {
        this.isWifi = isWifi;
        if (isWifi && statistic != null) {
            statistic.onClickPlay();
        }
        getController().startPlay(isWifi);
        return this;
    }

    public PlayerView startPlay() {
        getController().startPlay(true);
        return this;
    }

    public void pausePlay() {
        getController().pausePlay();
    }

    public void resumePlay() {
        getController().resumePlay();
    }

    @Override
    public void onVisibilityChange(int visibility) {
        // 播放器控制条可见性
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        return mSimpleExoPlayerView.dispatchKeyEvent(event) || super.dispatchKeyEvent(event);
    }

    public PlayerView setAlwaysShowStatus(boolean isAllwaysShow) {
        if (isAllwaysShow) {
            mSimpleExoPlayerView.setControllerShowTimeoutMs(Integer.MAX_VALUE);
            mSimpleExoPlayerView.setOnTouchListener(new OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    return true;
                }
            });
        }
        return this;
    }

    public void removeCallback(){
        if (mRunnable != null && mHandler != null){
            mHandler.removeCallbacks(mRunnable);
        }
    }
    @Override
    public void onClick(View v) {
        int i = v.getId();
        if (i == R.id.exo_play) {
            //重新播放
            removeCallback();
            if (!getController().isPlayDone()) {
                exo_pause.setVisibility(VISIBLE);
            }
            if (getController().isPause()) {
                getController().resumePlay();
            } else {
                startPlay();
            }
            exo_play.setVisibility(GONE);
            if (getController().isDurationMoreThan4000())
                mHandler.postDelayed(mRunnable, 2000);
            if (statistic != null) {
                if (!mClickedPlayButton) {
                    //统计 重播 点击事件
                    statistic.onClickPlay();
                    //统计非wifi状态下的 重播 点击事件
                    if (!isWifi) {
                        statistic.withoutWifiClickPlay();
                    }
                    mClickedPlayButton = true;
                }
            }
            if (!isWifi) {
                Toast.makeText(getContext(), R.string.player_without_wifi_play, Toast
                        .LENGTH_LONG).show();
            }

        } else if (i == R.id.exo_pause1) {      //点击暂停键
            removeCallback();
            exo_pause.setVisibility(GONE);
            exo_play.setVisibility(VISIBLE);
            getController().pausePlay();
            if (getController().isDurationMoreThan4000())
                mHandler.postDelayed(mRunnable, 2000);
        } else if (i == R.id.ec_play_volume) {
            //统计 音量点击次数
            if (statistic != null) {
                statistic.onVolumeClick();
            }
            if (isCloseVolume) {
                //打开声音
                isCloseVolume = false;
                mPlayerController.openVolume(ec_play_volume, 0);
            } else {
                //静音
                isCloseVolume = true;
                mPlayerController.closeVolume(ec_play_volume);
            }
        }
    }

    public void showPauseButton() {
        if (getPauseIconView() == null) {
            return;
        }
        getPauseIconView().setVisibility(View.VISIBLE);
    }

    public void hidePauseButton() {
        if (getPauseIconView() == null) {
            return;
        }
        getPauseIconView().setVisibility(View.GONE);
    }
    public void showPlayButton() {
        if (getPlayIconView() == null) {
            return;
        }
        getPlayIconView().setVisibility(View.VISIBLE);
    }

    public void hidePlayButton() {
        if (getPlayIconView() == null) {
            return;
        }
        getPlayIconView().setVisibility(View.GONE);
    }

    public void showLoading() {
        if (getProgressBar() == null) {
            return;
        }
        getProgressBar().setVisibility(View.VISIBLE);
    }

    public void hideLoading() {
        if (getProgressBar() == null) {
            return;
        }
        getProgressBar().setVisibility(View.GONE);
    }

    public void showVideoCover() {
        if (getBg_artwork() == null) {
            return;
        }
        getBg_artwork().setVisibility(View.VISIBLE);
    }

    public void hideVideoCover() {
        if (getBg_artwork() == null) {
            return;
        }
        getBg_artwork().setVisibility(View.GONE);
    }

    public void addPlayerStatistic(Statistic statistic) {
        this.statistic = statistic;
    }

    /**
     * 视频统计
     */
    public interface Statistic {
        void withoutWifiClickPlay();

        void onClickPlay();

        void onVolumeClick();

    }


}
