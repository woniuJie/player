package com.chuchujie.core.player;

import android.content.Context;

import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSource;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.util.Util;

/**
 * Created by wangjing on 2017/12/27.
 */
public class DataSourceProvider {

    /* ============================== EXOPlayer配置 ============================== */

    private Context mContext;

    private String mExtensionRenderers;

    public DataSourceProvider(Context context, String extensionRenderers) {
        mContext = context;
        mExtensionRenderers = extensionRenderers;
    }

    public DataSource.Factory buildDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new DefaultDataSourceFactory(mContext, bandwidthMeter,
                buildHttpDataSourceFactory(bandwidthMeter));
    }

    public HttpDataSource.Factory buildHttpDataSourceFactory(DefaultBandwidthMeter bandwidthMeter) {

        return new HttpDataSourceFactory(
                Util.getUserAgent(mContext, mContext.getString(R.string.player_app_name)),
                null,
                DefaultHttpDataSource.DEFAULT_CONNECT_TIMEOUT_MILLIS,
                DefaultHttpDataSource.DEFAULT_READ_TIMEOUT_MILLIS,
                true);
//        return new HttpDataSourceFactory(Util.getUserAgent(mContext,
//                mContext.getString(R.string.app_name)), bandwidthMeter);
    }

    /**
     * 使用哪个扩展, 原本的实现，更换为通过intent传递
     * BuildConfig.FLAVOR.equals("withExtensions")
     *
     * @return
     */
    public boolean useExtensionRenderers() {
        return "withExtensions".equals(mExtensionRenderers);
    }

}
