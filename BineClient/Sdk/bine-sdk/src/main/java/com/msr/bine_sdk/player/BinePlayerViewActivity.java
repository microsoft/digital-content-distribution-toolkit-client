package com.msr.bine_sdk.player;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Dialog;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import android.hardware.SensorManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;

import android.os.StrictMode;
import android.provider.Settings;
import android.util.Pair;
import android.view.MenuItem;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.exoplayer2.DefaultControlDispatcher;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.drm.KeysExpiredException;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelector;
import com.google.android.exoplayer2.ui.PlayerControlView;
import com.google.android.exoplayer2.ui.PlayerNotificationManager;
import com.google.android.exoplayer2.ui.PlayerView;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.util.Util;
import com.msr.bine_sdk.BineAPI;
import com.msr.bine_sdk.Constants;
import com.msr.bine_sdk.R;
import com.msr.bine_sdk.download.exo.DRMManager;
import com.msr.bine_sdk.cloud.models.APIResponse;
import com.msr.bine_sdk.models.Error;
import com.msr.bine_sdk.network_old.Connectivity;

import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.Objects;

import kotlin.coroutines.Continuation;
import kotlin.coroutines.CoroutineContext;
import kotlin.coroutines.EmptyCoroutineContext;

import static android.app.Notification.BADGE_ICON_SMALL;
import static com.msr.bine_sdk.Constants.PLAY_BACK_POSITION;

public class BinePlayerViewActivity extends BasePlayerActivity{

    public static final String keyExtraVideoURL = "VideoURL";
    public static final String keyExtraDashURL = "DashURL";

    private boolean playWhenReady = true;
    private int currentWindow = 0;
    private long playbackPosition = 0;

    private PlayerNotificationManager playerNotificationManager;
    private final int notificationId = 1234;
    private Bitmap bitmap;

    private final String STATE_RESUME_WINDOW = "resumeWindow";
    private final String STATE_RESUME_POSITION = "resumePosition";
    private final String STATE_PLAYER_FULLSCREEN = "playerFullscreen";

    private boolean mExoPlayerFullscreen = false;
    private ImageView mFullScreenIcon;
    private Dialog mFullScreenDialog;
    private int mResumeWindow;
    private long mResumePosition;
    private final PlayerErrorListener errorListener = new PlayerErrorListener();
    RenderersFactory renderersFactory;
    private BandwidthMeter bandwidthMeter;
    private TrackSelector trackSelector;

    private SimpleExoPlayer simpleExoPlayer;
    private PlayerView playerView;
    private String title;
    private String folderId;
    private String videoURL;
    private String dashURL;
    private final int launcherIcon = R.drawable.exo_notification_small_icon;

    private static BinePlayerListener binePlayerListener;
    private DRMManager drmManager;

    private boolean forcedLandscape, forcedPortrait = false;
    private OrientationEventListener orientationEventListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

        setContentView(R.layout.activity_exo_player_view);

        drmManager = DRMManager.Companion.getInstance(this);

        if (getIntent().getExtras() != null) {
            videoURL = getIntent().getStringExtra(keyExtraVideoURL);
            dashURL = getIntent().getStringExtra(keyExtraDashURL);
            title = getIntent().getStringExtra(Constants.TITLE);
            folderId = getIntent().getStringExtra(Constants.FOLDER_ID);
            playbackPosition = getIntent().getLongExtra(PLAY_BACK_POSITION, playbackPosition);
        }

        playerView = findViewById(R.id.player_view_exoplayer);

        ImageButton closeButton = findViewById(R.id.exo_close);
        closeButton.setOnClickListener(view -> closePlayer());
        ImageButton frwdButton = findViewById(R.id.exo_ffwd);
        frwdButton.setOnClickListener(view ->
                simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() + 10000));

        ImageButton rewButton = findViewById(R.id.exo_rew);
        rewButton.setOnClickListener(view -> simpleExoPlayer.seekTo(simpleExoPlayer.getCurrentPosition() - 10000));

        initOrientationListener();
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (newConfig.orientation == Configuration.ORIENTATION_LANDSCAPE) {
            mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(BinePlayerViewActivity.this, R.drawable.ic_fullscreen_skrink));
            mExoPlayerFullscreen = true;
        }
        else {
            mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(BinePlayerViewActivity.this, R.drawable.ic_fullscreen_expand));
            mExoPlayerFullscreen = false;
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        outState.putInt(STATE_RESUME_WINDOW, mResumeWindow);
        outState.putLong(STATE_RESUME_POSITION, mResumePosition);
        outState.putBoolean(STATE_PLAYER_FULLSCREEN, mExoPlayerFullscreen);
        super.onSaveInstanceState(outState);
    }

    private void initFullscreenDialog() {
        mFullScreenDialog = new Dialog(this, android.R.style.Theme_Black_NoTitleBar_Fullscreen) {
            public void onBackPressed() {
                if (mExoPlayerFullscreen)
                    closeFullscreenDialog();
                closePlayer();
            }
        };
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void openFullscreenDialog() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE);
        ((ViewGroup) playerView.getParent()).removeView(playerView);
        mFullScreenDialog.addContentView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(BinePlayerViewActivity.this, R.drawable.ic_fullscreen_skrink));
        mExoPlayerFullscreen = true;
        mFullScreenDialog.show();
    }

    @SuppressLint("SourceLockedOrientationActivity")
    private void closeFullscreenDialog() {
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        ((ViewGroup) playerView.getParent()).removeView(playerView);

        ((FrameLayout) findViewById(R.id.main_media_frame)).addView(playerView);
        mExoPlayerFullscreen = false;
        mFullScreenDialog.dismiss();
        mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(BinePlayerViewActivity.this, R.drawable.ic_fullscreen_expand));
    }


    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            finish();
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        if (Util.SDK_INT >= 24) {
            initializePlayer();
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        if (Util.SDK_INT >= 24) {
            releasePlayer();
        }
        orientationEventListener.disable();
    }

    @SuppressLint("InlinedApi")
    private void hideSystemUi() {
        playerView.setSystemUiVisibility(View.SYSTEM_UI_FLAG_LOW_PROFILE
                | View.SYSTEM_UI_FLAG_FULLSCREEN
                | View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
    }

    private void initializePlayer() {
        File root = getFilesDir();
        try {
            File outputFile = new File(root.getAbsolutePath() + Constants.MEDIA_OUTPUT_DIR +
                    folderId + "/" + Constants.THUMBNAIL_FILENAME);
            bitmap = BitmapFactory.decodeFile(outputFile.getAbsolutePath());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setupPlayerForRendering();

        playerNotificationManager = new PlayerNotificationManager.Builder(this,
                notificationId,
                Constants.ID_PLAY_CHANNEL,
                mediaDescriptionAdapter)
                .setNotificationListener(new PlayerNotificationManager.NotificationListener() {
                    @Override
                    public void onNotificationPosted(int notificationId, Notification notification, boolean ongoing) {
                    }

                    @Override
                    public void onNotificationCancelled(int notificationId, boolean dismissedByUser) {
                    }
                }).build();

        playerNotificationManager.setUseChronometer(true);
        playerNotificationManager.setSmallIcon(launcherIcon);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            playerNotificationManager.setBadgeIconType(BADGE_ICON_SMALL);
        }
        playerNotificationManager.setPlayer(simpleExoPlayer);

        playerView.setErrorMessageProvider(throwable -> Pair.create(1, "Error"));

        simpleExoPlayer.setPlayWhenReady(playWhenReady);
        simpleExoPlayer.seekTo(currentWindow, playbackPosition);

        DownloadRequest request = drmManager.getDownloadTracker().getDownloadRequest(Uri.parse(videoURL));

        if (request == null) {
            //Streaming
            if (binePlayerListener != null) {
                binePlayerListener.onStreamingInitiated(videoURL);
            }
            fetchTokenAndCreateMediaSource(false);
        } else {
            //Check if we request to play an offline downloaded content & license info might not exist
            //This handles the case where client clears the license info
            if (drmManager.hasLicenseInfo(folderId)) {
                if (binePlayerListener != null) {
                    binePlayerListener.onOfflinePlayBegan();
                }
                simpleExoPlayer.prepare(createMediaSource(dashURL,
                        folderId,
                        request,
                        false,
                        ""), false, false);
            }
            else {
                //Download the token-license before start playing
                fetchTokenAndCreateMediaSource(true);
            }
        }
    }

    private void fetchTokenAndCreateMediaSource(boolean onKeyExpiryException) {
        if (binePlayerListener != null) {
            binePlayerListener.onRequestToken();
        }
        BineAPI.Companion.OMS().getAssetToken(folderId, new Continuation<APIResponse.AssetTokenResponse>() {
            @NotNull
            @Override
            public CoroutineContext getContext() {
                return EmptyCoroutineContext.INSTANCE;
            }

            @Override
            public void resumeWith(@NotNull Object o) {
                APIResponse.AssetTokenResponse response = (APIResponse.AssetTokenResponse) o;
                if (response.getResult() != null && !response.getResult().isEmpty()) {
                    if (binePlayerListener != null) {
                        binePlayerListener.onReceivedToken();
                    }
                    DownloadRequest downloadRequest = drmManager.getDownloadTracker().getDownloadRequest(
                            Uri.parse(videoURL));
                    MediaSource mediaSource;
                    if (downloadRequest == null) {
                        mediaSource = createStreamMediaSource(response.getResult(), videoURL);
                        if (binePlayerListener != null) {
                            binePlayerListener.onStreamingBegan();
                        }
                    }
                    else {
                        if (binePlayerListener != null) {
                            binePlayerListener.onOfflinePlayBegan();
                        }
                        mediaSource = createMediaSource(dashURL,
                                folderId,
                                downloadRequest,
                                true,
                                response.getResult());
                    }
                    if(simpleExoPlayer != null) {
                        Handler handler = new Handler(Looper.getMainLooper());
                        handler.post(() -> simpleExoPlayer.prepare(mediaSource, false, false));
                    }
                } else {
                    if (binePlayerListener != null) {
                        binePlayerListener.onTokenFailure(Objects.requireNonNull(response.getError()).name() + ":" + response.getDetails());
                    }
                    if (onKeyExpiryException) {
                        fetchTokenFailedForKeyExpiry(response.getError(), response.getDetails());
                    }
                    else {
                        onPlaybackFailed(response.getError(), response.getDetails());
                    }
                }
            }
        });
    }

    private void releasePlayer() {
        if (playerNotificationManager != null) {
            playerNotificationManager.setPlayer(null);
        }
        if (simpleExoPlayer != null) {
            playWhenReady = simpleExoPlayer.getPlayWhenReady();
            playbackPosition = simpleExoPlayer.getCurrentPosition();
            currentWindow = simpleExoPlayer.getCurrentWindowIndex();
            simpleExoPlayer.release();
            simpleExoPlayer = null;
        }
    }

    private final PlayerNotificationManager.MediaDescriptionAdapter mediaDescriptionAdapter = new PlayerNotificationManager.MediaDescriptionAdapter() {
        @Override
        public String getCurrentSubText(@NonNull Player player) {
            return "Sub text";
        }

        @Override
        public String getCurrentContentTitle(@NonNull Player player) {
            return "Now Playing";
        }

        @Override
        public PendingIntent createCurrentContentIntent(@NonNull Player player) {
            return null;
        }

        @NonNull
        @Override
        public String getCurrentContentText(@NonNull Player player) {
            return title;
        }

        @NonNull
        @Override
        public Bitmap getCurrentLargeIcon(@NonNull Player player, @NonNull PlayerNotificationManager.BitmapCallback callback) {
            return bitmap;
        }
    };

    private void initFullscreenButton() {
        PlayerControlView controlView = playerView.findViewById(R.id.exo_controller);
        mFullScreenIcon = controlView.findViewById(R.id.exo_fullscreen_icon);
        FrameLayout mFullScreenButton = controlView.findViewById(R.id.exo_fullscreen_button);
        mFullScreenButton.setOnClickListener(v -> {
            if (!mExoPlayerFullscreen) {
                openFullscreenDialog();
                forcedLandscape = true;
            }
            else {
                closeFullscreenDialog();
                forcedPortrait = true;
            }
        });

        playerView.setControlDispatcher(new DefaultControlDispatcher() {
            @Override
            public boolean dispatchSetPlayWhenReady(@NonNull Player player, boolean playWhenReady) {
                if (binePlayerListener != null) {
                    long position = Math.max(0, simpleExoPlayer.getContentPosition());
                    if (playWhenReady) {
                        binePlayerListener.onPlayClicked(position);
                    } else {
                        binePlayerListener.onPauseClicked(position);
                    }
                }
                return super.dispatchSetPlayWhenReady(player, playWhenReady);
            }

            @Override
            public boolean dispatchSeekTo(@NonNull Player player, int windowIndex, long positionMs) {
                return super.dispatchSeekTo(player, windowIndex, positionMs);
            }

            @Override
            public boolean dispatchSetRepeatMode(@NonNull Player player, int repeatMode) {
                return super.dispatchSetRepeatMode(player, repeatMode);
            }

            @Override
            public boolean dispatchSetShuffleModeEnabled(@NonNull Player player, boolean shuffleModeEnabled) {
                return super.dispatchSetShuffleModeEnabled(player, shuffleModeEnabled);
            }

            @Override
            public boolean dispatchStop(@NonNull Player player, boolean reset) {
                return super.dispatchStop(player, reset);
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        hideSystemUi();
        if (simpleExoPlayer == null) {
            initializePlayer();
        }
        initFullscreenDialog();
        initFullscreenButton();

        if (mExoPlayerFullscreen) {
            ((ViewGroup) playerView.getParent()).removeView(playerView);
            mFullScreenDialog.addContentView(playerView, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            mFullScreenIcon.setImageDrawable(ContextCompat.getDrawable(BinePlayerViewActivity.this, R.drawable.ic_fullscreen_skrink));
            mFullScreenDialog.show();
        }


        if(orientationEventListener.canDetectOrientation())
            orientationEventListener.enable();
    }

    @Override
    protected void onPause() {
        if (simpleExoPlayer != null) {
            mResumeWindow = simpleExoPlayer.getCurrentWindowIndex();
            mResumePosition = Math.max(0, simpleExoPlayer.getContentPosition());
            simpleExoPlayer.pause();
        }


        if(orientationEventListener.canDetectOrientation())
            orientationEventListener.disable();

        super.onPause();
    }

    private void setupPlayerForRendering() {
        if (bandwidthMeter == null) {
            bandwidthMeter = new DefaultBandwidthMeter.Builder(this).build();
        }
        if (trackSelector == null) {
            trackSelector = new DefaultTrackSelector(this);
        }

        renderersFactory =
                buildRenderersFactory(true);

        simpleExoPlayer =
                new SimpleExoPlayer.Builder(this, renderersFactory)
                        .setTrackSelector(trackSelector)
                        .setBandwidthMeter(bandwidthMeter)
                        .build();
        playerView.setPlayer(simpleExoPlayer);
        simpleExoPlayer.addAnalyticsListener(new EventLogger((MappingTrackSelector) trackSelector));
        simpleExoPlayer.addListener(errorListener);
    }

    @Override
    public void onPlaybackFailed(Error error, String message) {

    }

    @Override
    public void fetchTokenFailedForKeyExpiry(Error error, String message) {

    }

    private class PlayerErrorListener implements Player.Listener {

        public void onPlayerError(ExoPlaybackException error) {
            onPlaybackFailed(Error.UNKNOWN_ERROR, error.type + ":" + error.getMessage());
            switch (error.type) {
                case ExoPlaybackException.TYPE_SOURCE:
                    if (binePlayerListener != null) {
                        binePlayerListener.onPlayError("TYPE_SOURCE: " + error.getSourceException().getMessage());
                    }
                    if (Objects.requireNonNull(error.getCause()).getCause() instanceof KeysExpiredException) {
                        Toast.makeText(BinePlayerViewActivity.this, "Token Expired", Toast.LENGTH_SHORT).show();
                        fetchTokenAndCreateMediaSource(true);
                        return;
                    }
                    else if (!Connectivity.isConnected(BinePlayerViewActivity.this)){
                        onPlaybackFailed(Error.NETWORK_ERROR, "");
                    }
                    simpleExoPlayer.retry();
                    break;

                case ExoPlaybackException.TYPE_RENDERER:
                    if (binePlayerListener != null) {
                        binePlayerListener.onPlayError("TYPE_RENDERER: " + error.getSourceException().getMessage());
                    }
                    break;
                case ExoPlaybackException.TYPE_UNEXPECTED:
                    if (binePlayerListener != null) {
                        binePlayerListener.onPlayError("TYPE_UNEXPECTED: " + error.getSourceException().getMessage());
                    }
                    break;
                case ExoPlaybackException.TYPE_REMOTE:
                    break;
            }
        }

        public void onPlayerStateChanged(@Player.State int playbackState) {
            if (binePlayerListener != null) {
                long position = Math.max(0, simpleExoPlayer.getContentPosition());
                if (playbackState == Player.STATE_ENDED) {
                    binePlayerListener.onPlayEnded(position);
                    closeFullscreenDialog();
                }
            }
        }
    }


    public RenderersFactory buildRenderersFactory(boolean preferExtensionRenderer) {
        @DefaultRenderersFactory.ExtensionRendererMode
        int extensionRendererMode =
                useExtensionRenderers()
                        ? (preferExtensionRenderer
                        ? DefaultRenderersFactory.EXTENSION_RENDERER_MODE_PREFER
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_ON)
                        : DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF;
        return new DefaultRenderersFactory(/* context= */ this)
                .setExtensionRendererMode(extensionRendererMode);
    }

    public boolean useExtensionRenderers() {
        return false;
    }

    public static void registerListener(BinePlayerListener listener) {
        binePlayerListener = listener;
    }

    public void pausePlayer() {
        if (simpleExoPlayer != null)
            simpleExoPlayer.setPlayWhenReady(false);
    }

    public static boolean isListening() {
        return binePlayerListener != null;
    }

    public static void unregisterListener() {
        binePlayerListener = null;
    }

    public interface BinePlayerListener {
        void onStreamingInitiated(String url);

        void onRequestToken();

        void onReceivedToken();

        void onTokenFailure(String error);

        void onStreamingBegan();

        void onOfflinePlayBegan();

        void onPlayError(String error);

        void onPlayClicked(long position);

        void onPauseClicked(long position);

        void onPlayEnded(long position);
    }

    @Override
    public void onBackPressed() {
        closePlayer();
    }

    public void closePlayer() {
        mResumePosition = Math.max(0, simpleExoPlayer.getContentPosition());

        if (binePlayerListener != null) binePlayerListener.onPauseClicked(mResumePosition);

        Intent intent = new Intent();
        intent.putExtra(PLAY_BACK_POSITION, mResumePosition);
        setResult(Activity.RESULT_OK, intent);
        finish();
    }

    private void initOrientationListener() {
        orientationEventListener = new OrientationEventListener(this, SensorManager.SENSOR_DELAY_UI)
        {
            public void onOrientationChanged(int orientation) {

                if (isScreenRotationLocked() && !isFinishing()) return;

                if( (orientation>=270 && orientation<=290) || (orientation>=80 && orientation<=100))
                {
                    //Landscape
                    if (!mExoPlayerFullscreen && !forcedPortrait) openFullscreenDialog();
                    if (forcedLandscape) forcedLandscape = false;
                }
                else if( (orientation>=0 && orientation<=10) || (orientation>=350 && orientation<=360))
                {
                    //Portrait
                    if (mExoPlayerFullscreen && !forcedLandscape) closeFullscreenDialog();
                    if (forcedPortrait) forcedPortrait = false;
                }
            }
        };
    }

    private boolean isScreenRotationLocked() {
        return Settings.System.getInt(getContentResolver(),
                Settings.System.ACCELEROMETER_ROTATION, 0) != 1;
    }
}
