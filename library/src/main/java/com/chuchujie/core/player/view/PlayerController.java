package com.chuchujie.core.player.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.view.View;
import android.widget.Toast;

import com.chuchujie.core.player.NetworkConnectChangedReceiver;
import com.chuchujie.core.player.PlayUtil;
import com.chuchujie.core.player.R;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.mediacodec.MediaCodecRenderer;
import com.google.android.exoplayer2.mediacodec.MediaCodecUtil;
import com.google.android.exoplayer2.source.BehindLiveWindowException;
import com.google.android.exoplayer2.source.ConcatenatingMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;

/**
 * Created by wangjing on 2018/5/11.
 */
public class PlayerController {

    private final PlayerView mPlayerView;
    private View volume;

    private Context mContext;

    private PlayerManager mPlayerManager;

    private Uri[] mPlayUris;

    private SimpleExoPlayer mSimpleExoPlayer;

    private int resumeWindow;
    private long resumePosition;

    private boolean inErrorState;
    private PlayerEventListener mPlayerEventListener;
    private SimpleVideoListener mSimpleVideoListener;
    private NetworkConnectChangedReceiver networkConnectChangedReceiver;
    private VolumeReceiver volumeReceiver;
    //当前播放音量值 范围（0.0~1.0）
    private float currVolume = 0f;

    // 是否已经开始播放过
    private boolean mIsPlaying;

    // 是否暂停播放
    private boolean mPaused;

    private boolean isDone;

    public PlayerController(PlayerView playerView, View volume) {
        mPlayerView = playerView;
        mContext = playerView.getContext().getApplicationContext();
        this.volume = volume;
        mPlayerManager = new PlayerManager(mContext);
        registerWifiListener();
        registerVolumeReceiver();
    }

    void initPlayer() {
        mPlayerManager.initPlayer();

        mSimpleExoPlayer = mPlayerManager.getSimpleExoPlayer();

        mPlayerEventListener = new PlayerEventListener();
        mSimpleExoPlayer.addListener(mPlayerEventListener);
        mSimpleVideoListener = new SimpleVideoListener();
        mSimpleExoPlayer.addVideoListener(mSimpleVideoListener);
        mPlayerView.mSimpleExoPlayerView.setPlayer(mSimpleExoPlayer);

        inErrorState = false;
    }

    public void setPlayUrl(String url) {
        setPlayUrl(new String[]{url});
    }

    public void setPlayUrl(String[] urls) {
        if (urls == null || urls.length == 0) {
            return;
        }
        mPlayUris = new Uri[urls.length];
        for (int i = 0; i < urls.length; i++) {
            mPlayUris[i] = Uri.parse(urls[i]);
        }
    }

    void preparePlay() {
        if (mPlayUris == null || mPlayUris.length == 0) {
            return;
        }
        MediaSource[] mediaSources = new MediaSource[mPlayUris.length];
        for (int i = 0; i < mPlayUris.length; i++) {
            mediaSources[i] = mPlayerManager.buildMediaSource(mPlayUris[i], "");
        }
        MediaSource mediaSource = mediaSources.length == 1 ? mediaSources[0]
                : new ConcatenatingMediaSource(mediaSources);

        boolean haveResumePosition = resumeWindow != C.INDEX_UNSET;
        if (haveResumePosition) {
            mSimpleExoPlayer.seekTo(resumeWindow, 0);
        }
        mSimpleExoPlayer.prepare(mediaSource, !haveResumePosition, true);
    }

    public void startPlay(boolean isDirectPlay) {
        if (mIsPlaying) {
            if (mPaused) {
                resumePlay();
            }
            return;
        }
        setAudioFocus(true);
//        if (isDirectPlay) {
            preparePlay();
//        }
        mSimpleExoPlayer.setPlayWhenReady(isDirectPlay);
        mIsPlaying = true;
    }

    //视频长度小于5s不展示播放暂停和继续按钮
    public boolean isDurationMoreThan4000() {
        if (mSimpleExoPlayer.getDuration() > 4000) {
            return true;
        }
        return false;
    }

    public void pausePlay() {
        if (mPaused) {
            return;
        }
        if (mIsPlaying) {
            setAudioFocus(false);
            mSimpleExoPlayer.setPlayWhenReady(false);
        }
        mPaused = true;
    }

    public void resumePlay() {
        if (mPaused) {
            if (mIsPlaying) {
                setAudioFocus(true);
                mSimpleExoPlayer.setPlayWhenReady(true);
            }
        } else {
            if (mIsPlaying) {
                return;
            }
            setAudioFocus(true);
            mSimpleExoPlayer.setPlayWhenReady(true);
        }
        mPaused = false;
    }

    public void stopPlay() {
        if (!mIsPlaying) {
            return;
        }
        clearResumePosition();
        setAudioFocus(false);
        mSimpleExoPlayer.setPlayWhenReady(false);
        mPaused = false;
        mIsPlaying = false;
    }

    public boolean isPlaying() {
        return mIsPlaying;
    }

    public boolean isPause() {
        return mPaused;
    }

    /**
     * 是否完成播放
     *
     * @return
     */
    public boolean isPlayDone() {
        return isDone;
    }

    /**
     * 更新现在播放的进度值
     */
    private void updateResumePosition() {
        resumeWindow = mSimpleExoPlayer.getCurrentWindowIndex();
        resumePosition = Math.min(mSimpleExoPlayer.getDuration(),
                mSimpleExoPlayer.getCurrentPosition());
    }

    /**
     * 是否有播放进度值
     *
     * @return
     */
    public boolean haveResumePosition() {
        return resumePosition > 0;
    }

    public boolean haveResumeWindowIndex() {
        return mSimpleExoPlayer.getCurrentWindowIndex() != C.INDEX_UNSET;
    }

    private void clearResumePosition() {
        resumeWindow = C.INDEX_UNSET;
        resumePosition = 0;
    }

    private boolean isBehindLiveWindow(ExoPlaybackException e) {
        if (e.type != ExoPlaybackException.TYPE_SOURCE) {
            return false;
        }
        Throwable cause = e.getSourceException();
        while (cause != null) {
            if (cause instanceof BehindLiveWindowException) {
                return true;
            }
            cause = cause.getCause();
        }
        return false;
    }

    private String getString(int id, Object... formatArgs) {
        return mContext.getResources().getString(id, formatArgs);
    }

    private void showToast(String message) {
        Toast.makeText(mContext, message, Toast.LENGTH_LONG).show();
    }

    public void onDestroy() {
        if (mSimpleExoPlayer != null) {
            mSimpleExoPlayer.release();
            mSimpleExoPlayer.removeListener(mPlayerEventListener);
            mSimpleExoPlayer.removeVideoListener(mSimpleVideoListener);
        }

        if (mPlayerManager != null) {
            mPlayerManager.onDestroy();
        }
        unRegisterVolumeListener();
        unRegisterWifiListener();
        mSimpleExoPlayer = null;
    }

    /**
     * 注册当音量发生变化时接收的广播
     */

    public void registerVolumeReceiver() {
        volumeReceiver = new VolumeReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("android.media.VOLUME_CHANGED_ACTION");
        // filter.addAction(AudioManager.ACTION_AUDIO_BECOMING_NOISY);
        filter.addAction("android.intent.action.HEADSET_PLUG");
        mContext.registerReceiver(volumeReceiver, filter);
    }

    /**
     * 取消 音量键的监听 注册
     */
    public void unRegisterVolumeListener() {
        if (volumeReceiver != null) {
            mContext.unregisterReceiver(volumeReceiver);
        }
    }

    /**
     * 更改音量
     *
     * @param imageView
     */
    public void changeVolume(View imageView) {
        float volume = PlayUtil.getCurrentVolume(mContext);
        if (volume <= 0) {
            this.currVolume = 0;
            closeVolume(imageView);
        } else {
            this.currVolume = volume;
            openVolume(imageView, volume);
        }
    }

    /**
     * 关闭音量
     *
     * @param imageView
     */
    public void closeVolume(View imageView) {
        imageView.setBackgroundResource(R.drawable.player_ec_volume_close);
        setAudioFocus(false);
        mSimpleExoPlayer.setVolume(0);
    }

    /**
     * 打开音量
     *
     * @param imageView
     * @param volume    默认打开音量值
     */

    public void openVolume(View imageView, float volume) {
        imageView.setBackgroundResource(R.drawable.player_ec_volume_open);
        setAudioFocus(true);
        if (volume <= 0) {
            volume = mSimpleExoPlayer.getVolume();
            if (volume == 0) {
                volume = currVolume;
            }
        }
        if (volume > 0) {
            currVolume = volume;
            mSimpleExoPlayer.setVolume(volume);
        } else if (currVolume > 0) {
            mSimpleExoPlayer.setVolume(currVolume);
        } else {
            float currentVolume = PlayUtil.getCurrentVolume(mContext);
            this.currVolume = currentVolume;
            mSimpleExoPlayer.setVolume(currentVolume);
        }
    }

    public void registerWifiListener() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        networkConnectChangedReceiver = new NetworkConnectChangedReceiver(mPlayerView);
        mContext.registerReceiver(networkConnectChangedReceiver, filter);
    }

    public void unRegisterWifiListener() {
        if (networkConnectChangedReceiver != null) {
            mContext.unregisterReceiver(networkConnectChangedReceiver);
        }
    }

    /**
     * 请求或者释放音量焦点
     *
     * @param focus
     */
    public void setAudioFocus(boolean focus) {
        AudioManager mAudioManager = (AudioManager)
                mContext.getSystemService(Context.AUDIO_SERVICE);
        if (focus) {//请求音频焦点
            if (mAudioManager != null) {
                mAudioManager.requestAudioFocus(null,
                        AudioManager.STREAM_MUSIC,
                        AudioManager.AUDIOFOCUS_GAIN_TRANSIENT);
            }
        } else {//释放
            if (mAudioManager != null) {
                mAudioManager.abandonAudioFocus(null);
            }
        }
    }

    private class PlayerEventListener implements Player.EventListener {

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest) {
            updateResumePosition();
        }

        @Override
        public void onTracksChanged(TrackGroupArray trackGroups,
                                    TrackSelectionArray trackSelections) {
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            if (isLoading) {
                mPlayerView.showLoading();
            } else {
                mPlayerView.hideLoading();
            }
            updateResumePosition();
        }

        /**
         * 视频的播放状态
         * STATE_IDLE 播放器空闲，既不在准备也不在播放
         * STATE_PREPARING 播放器正在准备
         * STATE_BUFFERING 播放器已经准备完毕，但无法立即播放。此状态的原因有很多，但常见的是播放器需要缓冲更多数据才能开始播放
         * STATE_ENDED 播放已完毕
         *
         * @author syw
         */
        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            switch (playbackState) {
                case Player.STATE_BUFFERING:
                    isDone = false;
                    updateResumePosition();
                    mPlayerView.hidePlayButton();
                    break;
                case Player.STATE_ENDED:
                    isDone = true;
                    mPlayerView.removeCallback();
                    updateResumePosition();
                    if (haveResumePosition()) {
                        stopPlay();
                        mPlayerView.hidePauseButton();
                        mPlayerView.showPlayButton();
                        mPlayerView.showVideoCover();
                        mPlayerView.hideLoading();
                    }
                    mIsPlaying = false;
                    break;
                case Player.STATE_IDLE:

                    break;
                case Player.STATE_READY:
                    isDone = false;
                    mIsPlaying = true;
                    updateResumePosition();
                    mPlayerView.hideVideoCover();
                    mPlayerView.hideLoading();
                    break;
            }


        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {

        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {

        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            inErrorState = true;

            String errorString = null;
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = e.getRendererException();
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    MediaCodecRenderer.DecoderInitializationException
                            decoderInitializationException =
                            (MediaCodecRenderer.DecoderInitializationException) cause;
                    if (decoderInitializationException.decoderName == null) {
                        if (decoderInitializationException.getCause() instanceof MediaCodecUtil
                                .DecoderQueryException) {
                            errorString = getString(R.string.player_error_querying_decoders);
                        } else if (decoderInitializationException.secureDecoderRequired) {
                            errorString = getString(R.string.player_error_no_secure_decoder,
                                    decoderInitializationException.mimeType);
                        } else {
                            errorString = getString(R.string.player_error_no_decoder,
                                    decoderInitializationException.mimeType);
                        }
                    } else {
                        errorString = getString(R.string.player_error_instantiating_decoder,
                                decoderInitializationException.decoderName);
                    }
                }
            }
            if (errorString != null) {
                showToast(errorString);
            }
            if (isBehindLiveWindow(e)) {
                clearResumePosition();
            } else {
                updateResumePosition();
            }
            stopPlay();
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            if (inErrorState) {
                updateResumePosition();
            }
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {

        }

        @Override
        public void onSeekProcessed() {

        }
    }

    private class SimpleVideoListener implements SimpleExoPlayer.VideoListener {

        @Override
        public void onVideoSizeChanged(int width, int height,
                                       int unappliedRotationDegrees, float pixelWidthHeightRatio) {

        }

        @Override
        public void onRenderedFirstFrame() {

        }
    }

    /**
     * 处理音量变化时的界面显示
     *
     * @author long
     */
    private class VolumeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getAction()) {
                case "android.media.VOLUME_CHANGED_ACTION"://监听系统音量键变化
                    changeVolume(volume);
                    break;
                case "android.intent.action.HEADSET_PLUG"://监听耳机拔出事件
                    //state  0代表拔出，1代表插入
                    if (intent.hasExtra("state")) {
                        if (intent.getIntExtra("state", 0) == 0) {
                            closeVolume(volume);
                        } else if (intent.getIntExtra("state", 0) == 1) {
                            openVolume(volume, 0);
                        }
                    }

                    break;
            }
        }
    }

}
