/*
 * Copyright (C) 2017 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.msr.bine_sdk.download.exo;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

import com.google.android.exoplayer2.core.BuildConfig;
import com.google.android.exoplayer2.offline.Download;
import com.google.android.exoplayer2.offline.DownloadCursor;
import com.google.android.exoplayer2.offline.DownloadHelper;
import com.google.android.exoplayer2.offline.DownloadManager;
import com.google.android.exoplayer2.offline.DownloadRequest;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.CopyOnWriteArraySet;

/**
 * Tracks media that has been downloaded.
 */
public class DownloadTracker {

    /**
     * Listens for changes in the tracked downloads.
     */
    public interface Listener {

        /**
         * Called when the tracked downloads changed.
         */
        void onDownloadsChanged();
    }

    public static final String ACTION_BROADCAST = BuildConfig.LIBRARY_PACKAGE_NAME + ".broadcast";
    private static final String TAG = "DownloadTracker";
    private final Context context;
    private final com.google.android.exoplayer2.upstream.DataSource.Factory dataSourceFactory;
    private final CopyOnWriteArraySet<Listener> listeners;
    public final HashMap<Uri, com.google.android.exoplayer2.offline.Download> downloads;
    public ArrayList<Download> downloadArrayList;
    private final com.google.android.exoplayer2.offline.DownloadIndex downloadIndex;

    public DownloadTracker(
            Context context, com.google.android.exoplayer2.upstream.DataSource.Factory dataSourceFactory, DownloadManager downloadManager) {
        this.context = context.getApplicationContext();
        this.dataSourceFactory = dataSourceFactory;
        listeners = new CopyOnWriteArraySet<>();
        downloads = new HashMap<>();
        downloadArrayList=new ArrayList<>();
        downloadIndex = downloadManager.getDownloadIndex();
        downloadManager.addListener(new DownloadManagerListener());
        loadDownloads();
    }

    public void addListener(Listener listener) {
        listeners.add(listener);
    }

    public void removeListener(Listener listener) {
        listeners.remove(listener);
    }

    public boolean isDownloaded(Uri uri) {
        com.google.android.exoplayer2.offline.Download download = downloads.get(uri);
        return download != null && download.state != com.google.android.exoplayer2.offline.Download.STATE_FAILED;
    }

    public DownloadRequest getDownloadRequest(Uri uri) {
        com.google.android.exoplayer2.offline.Download download = downloads.get(uri);
        return download != null && download.state != com.google.android.exoplayer2.offline.Download.STATE_FAILED ? download.request : null;
    }

    private void loadDownloads() {
        try (DownloadCursor loadedDownloads = downloadIndex.getDownloads()) {
            while (loadedDownloads.moveToNext()) {
                com.google.android.exoplayer2.offline.Download download = loadedDownloads.getDownload();
                downloads.put(download.request.uri, download);
                downloadArrayList.add(download);
            }
        } catch (IOException e) {
            com.google.android.exoplayer2.util.Log.w(TAG, "Failed to query downloads", e);
        }
    }

    public Download getDownload(Uri uri) {
        Download download = null;
        if (downloads.size() > 0) {
            download = downloads.get(uri);
            return download;
        }
        return null;
    }

    private DownloadHelper getDownloadHelper(
            Uri uri, String extension, com.google.android.exoplayer2.RenderersFactory renderersFactory) {
        int type = com.google.android.exoplayer2.util.Util.inferContentType(uri, extension);
        switch (type) {
            case com.google.android.exoplayer2.C.TYPE_DASH:
                return DownloadHelper.forDash(context, uri, dataSourceFactory, renderersFactory);
            case com.google.android.exoplayer2.C.TYPE_SS:
                return DownloadHelper.forSmoothStreaming(context, uri, dataSourceFactory, renderersFactory);
            case com.google.android.exoplayer2.C.TYPE_HLS:
                return DownloadHelper.forHls(context, uri, dataSourceFactory, renderersFactory);
            case com.google.android.exoplayer2.C.TYPE_OTHER:
                return DownloadHelper.forProgressive(context, uri);
            default:
                throw new IllegalStateException("Unsupported type: " + type);
        }
    }

    private class DownloadManagerListener implements DownloadManager.Listener {
        @Override
        public void onDownloadChanged(DownloadManager downloadManager,
                                      com.google.android.exoplayer2.offline.Download download, Exception finalException) {
            downloads.put(download.request.uri, download);
            downloadArrayList.add(download);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }

        @Override
        public void onDownloadRemoved(DownloadManager downloadManager,
                                      com.google.android.exoplayer2.offline.Download download) {
            downloads.remove(download.request.uri);
            downloadArrayList.remove(download);
            for (Listener listener : listeners) {
                listener.onDownloadsChanged();
            }
        }
    }
}
