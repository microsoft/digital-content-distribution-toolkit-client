package com.msr.bine_sdk.player;

import android.net.Uri;

import androidx.appcompat.app.AppCompatActivity;

import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.DrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaDrm;
import com.google.android.exoplayer2.drm.MediaDrmCallback;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.util.Util;
import com.msr.bine_sdk.download.exo.DRMManager;
import com.msr.bine_sdk.models.Error;

import java.io.IOException;

public abstract class BasePlayerActivity extends AppCompatActivity {

    private DrmSessionManager drmSessionManager;

    /*public class PlayerErrorMessageProvider implements ErrorMessageProvider<ExoPlaybackException> {
        @Override
        public Pair<Integer, String> getErrorMessage(ExoPlaybackException e) {
            String errorString = getString(R.string.error_generic);
            if (e.type == ExoPlaybackException.TYPE_RENDERER) {
                Exception cause = e.getRendererException();
                if (cause instanceof MediaCodecRenderer.DecoderInitializationException) {
                    // Special case for decoder initialization failures.
                    MediaCodecRenderer.DecoderInitializationException decoderInitializationException =
                            (MediaCodecRenderer.DecoderInitializationException) cause;
                    if (decoderInitializationException.codecInfo == null) {
                        if (decoderInitializationException.getCause() instanceof MediaCodecUtil.DecoderQueryException) {
                            errorString = getString(R.string.error_querying_decoders);
                        } else if (decoderInitializationException.secureDecoderRequired) {
                            errorString =
                                    getString(
                                            R.string.error_no_secure_decoder, decoderInitializationException.mimeType);
                        } else {
                            errorString =
                                    getString(R.string.error_no_decoder, decoderInitializationException.mimeType);
                        }
                    } else {
                        errorString =
                                getString(
                                        R.string.error_instantiating_decoder,
                                        decoderInitializationException.codecInfo.name);
                    }
                }
            }
            return Pair.create(0, errorString);
        }
    }*/

    protected MediaSource createMediaSource(String dashURL,
                                            String requestId,
                                            DownloadRequest downloadRequest,
                                            Boolean fetchLicense,
                                            String token) {
        try {
            drmSessionManager = DRMManager.Companion.getInstance(this).buildOfflineDrmSessionManager(Uri.parse(dashURL), requestId, fetchLicense, token);
            return DownloadHelper.createMediaSource(downloadRequest, buildDataSourceFactory(), drmSessionManager);
        }
        catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected DataSource.Factory buildDataSourceFactory() {
        return DRMManager.Companion.getInstance(getApplicationContext()).buildDataSourceFactory();
    }


    private MediaSource createLeafMediaSource(
            Uri uri, DrmSessionManager drmSessionManager) {
        @C.ContentType int type = Util.inferContentType(uri, "mpd");
        if (type == C.TYPE_DASH) {
            return new DashMediaSource.Factory(DRMManager.Companion.getInstance(getApplicationContext()).buildDataSourceFactory())
                    .setDrmSessionManager(drmSessionManager)
                    .createMediaSource(uri);
        }
        throw new IllegalStateException("Unsupported type: " + type);
    }

    protected MediaSource createStreamMediaSource(String token, String videoURL) {
        Uri uriValue = Uri.parse(videoURL);
        MediaDrmCallback mediaDrmCallback = DRMManager.Companion.getInstance(getApplicationContext())
                .createMediaDrmCallback(token, "", null);
        drmSessionManager = new DefaultDrmSessionManager.Builder()
                .setUuidAndExoMediaDrmProvider(C.WIDEVINE_UUID,
                        FrameworkMediaDrm.DEFAULT_PROVIDER)
                .setMultiSession(true)
                .build(mediaDrmCallback);
        return createLeafMediaSource(uriValue, drmSessionManager);
    }

    public abstract void onPlaybackFailed(Error error, String message);
    public abstract void fetchTokenFailedForKeyExpiry(Error error, String message);
}
