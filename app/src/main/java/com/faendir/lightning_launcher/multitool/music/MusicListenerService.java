package com.faendir.lightning_launcher.multitool.music;

import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.KeyEvent;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.badge.NotificationListener;

import org.acra.ACRA;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import java8.util.Optional;
import java8.util.stream.RefStreams;
import java8.util.stream.StreamSupport;

import static com.faendir.lightning_launcher.multitool.util.LambdaUtils.exceptionToOptional;

/**
 * Created on 03.07.2016.
 *
 * @author F43nd1r
 */

public class MusicListenerService extends Service {
    private static final List<Integer> PLAYING_STATES;
    public static final String EXTRA_COMMAND_CODE = "command_code";

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PLAYING_STATES = Arrays.asList(
                    PlaybackState.STATE_PLAYING, PlaybackState.STATE_FAST_FORWARDING, PlaybackState.STATE_SKIPPING_TO_NEXT,
                    PlaybackState.STATE_SKIPPING_TO_PREVIOUS, PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM);
        } else {
            PLAYING_STATES = Collections.emptyList();
        }
    }

    private final BidiMap<MediaController, Callback> controllers;
    private SharedPreferences sharedPref;
    private volatile WeakReference<MediaController> currentController;
    private final MediaSessionManager.OnActiveSessionsChangedListener sessionsChangedListener;
    private boolean enabled = false;

    public MusicListenerService() {
        controllers = new DualHashBidiMap<>();
        currentController = new WeakReference<>(null);
        sessionsChangedListener = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? this::onActiveSessionsChanged : null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            ComponentName notificationListener = new ComponentName(this, NotificationListener.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                boolean enabled = NotificationListener.isEnabled(this);
                if (enabled) {
                    if(!this.enabled) {
                        MediaSessionManager mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
                        sessionsChangedListener.onActiveSessionsChanged(mediaSessionManager.getActiveSessions(notificationListener));
                        mediaSessionManager.addOnActiveSessionsChangedListener(sessionsChangedListener, notificationListener);
                        this.enabled = true;
                    }
                    if (intent.hasExtra(EXTRA_COMMAND_CODE)) {
                        int keyCode = intent.getIntExtra(EXTRA_COMMAND_CODE, 0);
                        MediaController controller = currentController.get();
                        if (controller != null) {
                            if (isAlternativeControl(controller)) {
                                sendKeyCodeToPlayer(keyCode, controller.getPackageName());
                            } else {
                                switch (keyCode){
                                    case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                                        PlaybackState state = controller.getPlaybackState();
                                        if (state != null && PLAYING_STATES.contains(state.getState())) {
                                            controller.getTransportControls().pause();
                                        } else {
                                            controller.getTransportControls().play();
                                        }
                                        break;
                                    case KeyEvent.KEYCODE_MEDIA_NEXT:
                                        controller.getTransportControls().skipToNext();
                                        break;
                                    case KeyEvent.KEYCODE_MEDIA_PREVIOUS:
                                        controller.getTransportControls().skipToPrevious();
                                        break;
                                }
                            }
                        } else {
                            startDefaultPlayerWithKeyCode(keyCode);
                        }
                    }
                } else {
                    NotificationListener.askForEnable(this);
                }
            }
        }
        return START_NOT_STICKY;
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateCurrentInfo(MediaController controller, Bitmap albumArt, String title, String album, String artist) {
        if (controller == null) return;
        this.currentController = new WeakReference<>(controller);
        MusicDataSource.updateInfo(this, new TitleInfo(title, album, artist, controller.getPackageName(), albumArt));
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    public void onActiveSessionsChanged(List<MediaController> list) {
        Map<String, Callback> removed = new HashMap<>();
        for (Iterator<Map.Entry<MediaController, Callback>> iterator = controllers.entrySet().iterator(); iterator.hasNext(); ) {
            Map.Entry<MediaController, Callback> entry = iterator.next();
            MediaController controller = entry.getKey();
            Callback callback = entry.getValue();
            if (!list.contains(controller)) {
                controller.unregisterCallback(callback);
                removed.put(controller.getPackageName(), callback);
                iterator.remove();
            }
        }
        Set<String> players = sharedPref.getStringSet(getString(R.string.pref_activePlayers), Collections.emptySet());
        for (ListIterator<MediaController> iterator = list.listIterator(list.size()); iterator.hasPrevious(); ) {
            MediaController controller = iterator.previous();
            if (!players.contains(controller.getPackageName())) {
                iterator.remove();
                continue;
            }
            if (!controllers.containsKey(controller)) {
                Callback callback;
                if (removed.containsKey(controller.getPackageName())) {
                    callback = removed.get(controller.getPackageName());
                    removed.remove(controller.getPackageName());
                } else {
                    callback = new Callback();
                }
                controller.registerCallback(callback);
                controllers.put(controller, callback);
                PlaybackState state = controller.getPlaybackState();
                if (state != null) {
                    callback.onPlaybackStateChanged(state);
                }
                callback.onMetadataChanged(controller.getMetadata());
            }
        }
        if (currentController.get() == null && !list.isEmpty()) {
            controllers.get(list.get(list.size() - 1)).push();
        }
        StreamSupport.stream(removed.values()).forEach(Callback::recycle);
    }

    private void startDefaultPlayerWithKeyCode(int keyCode) {
        String player = sharedPref.getString(getString(R.string.pref_musicDefault), null);
        if (player != null) {
            sendKeyCodeToPlayer(keyCode, player);
        }
    }

    private void sendKeyCodeToPlayer(int keyCode, String playerPackage) {
        try {
            Intent down = new Intent(Intent.ACTION_MEDIA_BUTTON);
            down.setPackage(playerPackage);
            down.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            sendBroadcast(down);
            Intent up = new Intent(Intent.ACTION_MEDIA_BUTTON);
            up.setPackage(playerPackage);
            up.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            sendBroadcast(up);
        } catch (Exception e) {
            ACRA.getErrorReporter().handleSilentException(e);
            e.printStackTrace();
        }
    }


    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean isAlternativeControl(MediaController controller) {
        return sharedPref.getStringSet(getString(R.string.pref_altControl), Collections.emptySet())
                .contains(controller.getPackageName());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private class Callback extends MediaController.Callback {
        private MediaMetadata metadata;
        private PlaybackState playbackState;
        private Bitmap bitmap;
        private boolean hasRequestedAlbumArt;

        @Override
        public void onPlaybackStateChanged(@NonNull PlaybackState state) {
            if (playbackState == null || playbackState.getState() != state.getState()) {
                this.playbackState = state;
                update();
            }
        }

        @Override
        public void onMetadataChanged(MediaMetadata metadata) {
            hasRequestedAlbumArt = false;
            this.metadata = metadata;
            update();
        }

        private void update() {
            if (metadata != null) {
                if (!hasRequestedAlbumArt || bitmap == null) {
                    Bitmap bmp = loadBitmapForKeys(MediaMetadata.METADATA_KEY_ALBUM_ART, MediaMetadata.METADATA_KEY_ALBUM_ART_URI,
                            MediaMetadata.METADATA_KEY_ART, MediaMetadata.METADATA_KEY_ART_URI);
                    if (bitmap != null && bitmap != bmp) bitmap.recycle();
                    bitmap = bmp;
                    hasRequestedAlbumArt = true;
                }
                if ((playbackState != null && PLAYING_STATES.contains(playbackState.getState()))
                        || currentController == null || currentController.get() == null
                        || currentController.get().equals(controllers.getKey(this))) {
                    push();
                }
            }
        }

        @Nullable
        private Bitmap loadBitmapForKeys(@NonNull String... keys) {
            return RefStreams.of(keys).map(exceptionToOptional(key -> {
                if (key.endsWith("URI")) {
                    String uri = metadata.getString(key);
                    if (uri != null) {
                        return BitmapFactory.decodeStream(getContentResolver().openInputStream(Uri.parse(uri)));
                    }
                } else {
                    return metadata.getBitmap(key);
                }
                return null;
            })).filter(Optional::isPresent).findAny().map(Optional::get).orElse(null);
        }

        private void push() {
            if (metadata == null) {
                updateCurrentInfo(controllers.getKey(this), bitmap, null, null, null);
            } else {
                updateCurrentInfo(controllers.getKey(this), bitmap, metadata.getString(MediaMetadata.METADATA_KEY_TITLE),
                        metadata.getString(MediaMetadata.METADATA_KEY_ALBUM),
                        metadata.getString(MediaMetadata.METADATA_KEY_ARTIST));
            }
        }

        void recycle() {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }
}
