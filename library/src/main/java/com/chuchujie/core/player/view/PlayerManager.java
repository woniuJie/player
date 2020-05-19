package com.chuchujie.core.player.view;

import android.content.Context;
import android.net.Uri;
import android.os.Handler;
import android.text.TextUtils;

import com.chuchujie.core.player.DataSourceProvider;
import com.chuchujie.core.player.EventLogger;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.extractor.DefaultExtractorsFactory;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by wangjing on 2018/5/11.
 */
public class PlayerManager {

    private Context mContext;

    private DataSource.Factory mediaDataSourceFactory;

    private DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private DataSourceProvider mDataSourceProvider;

    private DefaultTrackSelector trackSelector;

    private SimpleExoPlayer mSimpleExoPlayer;
    private EventLogger mEventLogger;
    private Handler mMainHandler = new Handler();

    public PlayerManager(Context context) {
        mContext = context;
    }

    public void initPlayer() {
        mediaDataSourceFactory = buildHttpDataSourceFactory(true);

        TrackSelection.Factory adaptiveTrackSelectionFactory =
                new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        trackSelector = new DefaultTrackSelector(adaptiveTrackSelectionFactory);

        mSimpleExoPlayer = ExoPlayerFactory.newSimpleInstance(mContext, trackSelector);

        mEventLogger = new EventLogger(trackSelector);

        mSimpleExoPlayer.addMetadataOutput(mEventLogger);
        mSimpleExoPlayer.setAudioDebugListener(mEventLogger);
        mSimpleExoPlayer.setVideoDebugListener(mEventLogger);
    }

    public SimpleExoPlayer getSimpleExoPlayer() {
        return mSimpleExoPlayer;
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *                          DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        return getDataSourceProvider()
                .buildHttpDataSourceFactory(useBandwidthMeter ? BANDWIDTH_METER : null);
    }

    public DataSourceProvider getDataSourceProvider() {
        if (mDataSourceProvider == null) {
            mDataSourceProvider = new DataSourceProvider(mContext, "");
        }
        return mDataSourceProvider;
    }

    public MediaSource buildMediaSource(Uri uri, String overrideExtension) {
        @C.ContentType int type = TextUtils.isEmpty(overrideExtension) ? Util.inferContentType(uri)
                : Util.inferContentType("." + overrideExtension);
        switch (type) {
            case C.TYPE_SS:
                return new SsMediaSource(uri, buildHttpDataSourceFactory(false),
                        new DefaultSsChunkSource.Factory(mediaDataSourceFactory),
                        mMainHandler, mEventLogger);
            case C.TYPE_DASH:
                return new DashMediaSource(uri, buildHttpDataSourceFactory(false),
                        new DefaultDashChunkSource.Factory(mediaDataSourceFactory),
                        mMainHandler, mEventLogger);
            case C.TYPE_HLS:
                return new HlsMediaSource(uri, mediaDataSourceFactory, mMainHandler, mEventLogger);
            case C.TYPE_OTHER:
                return new ExtractorMediaSource(uri, mediaDataSourceFactory,
                        new DefaultExtractorsFactory(),
                        mMainHandler, mEventLogger);
            default: {
                throw new IllegalStateException("Unsupported type: " + type);
            }
        }
    }

    public void onDestroy() {
        if (mSimpleExoPlayer != null) {
            mSimpleExoPlayer.removeMetadataOutput(mEventLogger);
            mSimpleExoPlayer.setAudioDebugListener(null);
            mSimpleExoPlayer.setVideoDebugListener(null);
        }
    }
}
