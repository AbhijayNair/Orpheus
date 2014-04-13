/*
 * Copyright (C) 2014 OpenSilk Productions LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opensilk.music.dream;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.os.RemoteException;
import android.service.dreams.DreamService;
import android.service.dreams.IDreamService;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.andrew.apollo.BuildConfig;
import com.andrew.apollo.MusicPlaybackService;
import com.andrew.apollo.MusicStateListener;
import com.andrew.apollo.R;
import com.andrew.apollo.utils.Lists;
import com.andrew.apollo.utils.MusicUtils;

import java.lang.reflect.Field;
import java.util.ArrayList;

import hugo.weaving.DebugLog;

/**
 * Created by drew on 4/4/14.
 */
@TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
public class DayDreamService extends DreamService {
    private static final String TAG = DayDreamService.class.getSimpleName();
    private static final boolean D = BuildConfig.DEBUG;

    private ViewGroup mContentView, mSaverView;

    // True if attached to window
    private boolean isAttached;

    // Music service token
    private MusicUtils.ServiceToken mMusicServiceToken;
    // True if bound to music service
    private boolean isBoundToMusicService;
    // True if bount to alt dream service
    private boolean isBoundToAltDream;

    private final Handler mHandler;
    private final ScreenSaverAnimation mMoveSaverRunnable;

    private final ArrayList<MusicStateListener> mMusicStateListener;
    private final MusicStateReceiver mPlaybackReceiver;

    public DayDreamService() {
        mHandler = new Handler();
        mMoveSaverRunnable = new ScreenSaverAnimation(mHandler);
        mMusicStateListener = Lists.newArrayList();
        mPlaybackReceiver = new MusicStateReceiver();
    }

    @DebugLog
    @Override
    public void onCreate() {
        super.onCreate();
        mMusicServiceToken = MusicUtils.bindToService(this, mMusicServiceConnection);

        final IntentFilter filter = new IntentFilter();
        // Play and pause changes
        filter.addAction(MusicPlaybackService.PLAYSTATE_CHANGED);
        // Shuffle and repeat changes
        filter.addAction(MusicPlaybackService.SHUFFLEMODE_CHANGED);
        filter.addAction(MusicPlaybackService.REPEATMODE_CHANGED);
        // Track changes
        filter.addAction(MusicPlaybackService.META_CHANGED);
        registerReceiver(mPlaybackReceiver, filter);
    }

    @DebugLog
    @Override
    public void onDestroy() {
        super.onDestroy();
        if (isBoundToMusicService) {
            MusicUtils.unbindFromService(mMusicServiceToken);
            isBoundToMusicService = false;
        }
        if (isBoundToAltDream) {
            unbindService(mAltDreamConnection);
            isBoundToAltDream = false;
        }
        unregisterReceiver(mPlaybackReceiver);
    }

    @DebugLog
    @Override
    public void onAttachedToWindow() {
        isAttached = true;
        super.onAttachedToWindow();
        if (isBoundToMusicService) {
            if (!MusicUtils.isPlaying()) {
                bindAltDream();
            } else {
                setupSaverView();
            }
        }
    }

    @DebugLog
    @Override
    public void onDetachedFromWindow() {
        isAttached = false;
        super.onDetachedFromWindow();
        mHandler.removeCallbacks(mMoveSaverRunnable);
        mMusicStateListener.clear();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isBoundToAltDream) {
            setupSaverView();
        }
    }

    /**
     * Init our dreams view
     */
    @DebugLog
    private void setupSaverView() {
        mHandler.removeCallbacks(mMoveSaverRunnable);

        setContentView(R.layout.daydream_container);
        mSaverView = (ViewGroup) findViewById(R.id.dream_container);
        mSaverView.setAlpha(0);
        mContentView = (ViewGroup) mSaverView.getParent();

        setScreenBright(!DreamPrefs.wantNightMode(this));
        setFullscreen(DreamPrefs.wantFullscreen(this));

        LayoutInflater inflater = getWindow().getLayoutInflater();
        View inner = null;
        int style = DreamPrefs.getDreamLayout(this);
        switch (style) {
            case DreamPrefs.DreamLayout.ART_ONLY:
                inner = inflater.inflate(R.layout.daydream_art_only, mSaverView, false);
                setInteractive(false);
                break;
            case DreamPrefs.DreamLayout.ART_META:
                inner = inflater.inflate(R.layout.daydream_art_meta, mSaverView, false);
                setInteractive(false);
                break;
            case DreamPrefs.DreamLayout.ART_CONTROLS:
                inner = inflater.inflate(R.layout.daydream_art_controls, mSaverView, false);
                setInteractive(true);
        }
        if (inner != null) {
            mSaverView.addView(inner);
            // We add the listener for child views since they cant access us.
            mMusicStateListener.add((MusicStateListener) inner);
        }
        mMoveSaverRunnable.registerViews(mContentView, mSaverView);

        mHandler.post(mMoveSaverRunnable);
    }

    /**
     * Performs bind on alt dream service
     */
    @DebugLog
    private void bindAltDream() {
        ComponentName altDream = DreamPrefs.getAltDreamComponent(this);
        if (altDream != null) {
            Intent intent = new Intent(DreamService.SERVICE_INTERFACE)
                    .setComponent(altDream)
                    .addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            bindService(intent, mAltDreamConnection, BIND_AUTO_CREATE);
        } else {
            Log.w(TAG, "Alternate dream not set");
            setupSaverView();
        }
    }

    /**
     * Music service connection
     */
    final ServiceConnection mMusicServiceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBoundToMusicService = true;
            if (isAttached) {
                if (!MusicUtils.isPlaying()) {
                    bindAltDream();
                } else {
                    setupSaverView();
                }
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBoundToMusicService = false;
        }
    };

    /**
     * Alt dream service connection
     */
    final ServiceConnection mAltDreamConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            isBoundToAltDream = true;
            try {
                IDreamService dreamService = IDreamService.Stub.asInterface(service);
                // Get super
                Class s = DayDreamService.class.getSuperclass();
                // pull out the WindowToken
                // this is the token passed from the DreamManager
                Field f = s.getDeclaredField("mWindowToken");
                f.setAccessible(true);
                // extract the actual IBinder
                IBinder token = (IBinder) f.get(DayDreamService.this);
                // forward our token to the alt dream service
                dreamService.attach(token);
            } catch (NoSuchFieldException|IllegalAccessException|NullPointerException|RemoteException e) {
                e.printStackTrace();
                setupSaverView();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            isBoundToAltDream = false;
        }
    };

    private final class MusicStateReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            if (action == null) {
                return;
            } else if (action.equals(MusicPlaybackService.META_CHANGED)) {
                // Let the listener know to the meta chnaged
                for (final MusicStateListener listener : mMusicStateListener) {
                    if (listener != null) {
                        listener.onMetaChanged();
                    }
                }
            } else if (action.equals(MusicPlaybackService.PLAYSTATE_CHANGED)) {
                // Let the listener know to the playstate chnaged
                for (final MusicStateListener listener : mMusicStateListener) {
                    if (listener != null) {
                        listener.onPlaystateChanged();
                    }
                }
            } else if (action.equals(MusicPlaybackService.REPEATMODE_CHANGED)
                    || action.equals(MusicPlaybackService.SHUFFLEMODE_CHANGED)) {
                for (final MusicStateListener listener : mMusicStateListener) {
                    if (listener != null) {
                        listener.onPlaybackModeChanged();
                    }
                }
            }
        }
    }
}
