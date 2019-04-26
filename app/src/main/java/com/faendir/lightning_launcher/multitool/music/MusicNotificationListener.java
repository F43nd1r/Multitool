package com.faendir.lightning_launcher.multitool.music;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.MediaMetadata;
import android.media.session.MediaController;
import android.media.session.MediaSessionManager;
import android.media.session.PlaybackState;
import android.net.Uri;
import android.os.Build;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.service.notification.NotificationListenerService;
import android.util.Log;
import android.view.KeyEvent;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.billing.BaseBillingManager;
import com.faendir.lightning_launcher.multitool.util.notification.NotificationListener;
import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;
import java9.util.Optional;
import java9.util.stream.Stream;
import java9.util.stream.StreamSupport;
import org.acra.ACRA;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import static com.faendir.lightning_launcher.multitool.MultiTool.DEBUG;
import static com.faendir.lightning_launcher.multitool.MultiTool.LOG_TAG;
import static com.faendir.lightning_launcher.multitool.util.LambdaUtils.exceptionToOptional;

/**
 * Created on 03.07.2016.
 *
 * @author F43nd1r
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
public class MusicNotificationListener implements NotificationListener {
    private static final List<Integer> PLAYING_STATES = Arrays.asList(
            PlaybackState.STATE_PLAYING, PlaybackState.STATE_FAST_FORWARDING, PlaybackState.STATE_SKIPPING_TO_NEXT,
            PlaybackState.STATE_SKIPPING_TO_PREVIOUS, PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM);

    private final BiMap<MediaController, Callback> controllers;
    private final Handler handler;
    private Context context;
    private SharedPreferences sharedPref;
    private volatile WeakReference<MediaController> currentController;
    private boolean enabled = false;

    public MusicNotificationListener() {
        controllers = HashBiMap.create();
        currentController = new WeakReference<>(null);
        handler = new Handler();
    }

    private void onIntentReceived(Context context, Intent intent) {
        new Thread(() -> {
            if (!this.enabled) {
                if (new BaseBillingManager(context).isBoughtOrTrial(BaseBillingManager.PaidFeature.MUSIC_WIDGET)) {
                    MediaSessionManager mediaSessionManager = (MediaSessionManager) context.getSystemService(Context.MEDIA_SESSION_SERVICE);
                    if (mediaSessionManager != null) {
                        ComponentName notificationListener = new ComponentName(context, context.getClass());
                        onActiveSessionsChanged(mediaSessionManager.getActiveSessions(notificationListener));
                        mediaSessionManager.addOnActiveSessionsChangedListener(this::onActiveSessionsChanged, notificationListener, handler);
                        this.enabled = true;
                        if (DEBUG) {
                            Log.d(LOG_TAG, "Media session listener enabled");
                        }
                    }
                }
            }
            if (this.enabled && intent.hasExtra(MusicListener.EXTRA_COMMAND_CODE)) {
                int keyCode = intent.getIntExtra(MusicListener.EXTRA_COMMAND_CODE, 0);
                MediaController controller = currentController.get();
                if (controller != null) {
                    if (isAlternativeControl(controller)) {
                        sendKeyCodeToPlayer(keyCode, controller.getPackageName());
                    } else {
                        switch (keyCode) {
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
        }).start();
    }

    @Override
    public void onCreate(NotificationListenerService context) {
        this.context = context;
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context);
        context.registerReceiver(new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onIntentReceived(context, intent);
            }
        }, new IntentFilter(MusicListener.INTENT_ACTION));
    }

    private void updateCurrentInfo(MediaController controller, Bitmap albumArt, String title, String album, String artist) {
        if (controller == null) return;
        this.currentController = new WeakReference<>(controller);
        MusicDataSource.updateInfo(context, new TitleInfo(title, album, artist, controller.getPackageName(), albumArt));
    }

    private void onActiveSessionsChanged(List<MediaController> list) {
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
        Set<String> players = sharedPref.getStringSet(context.getString(R.string.pref_activePlayers), Collections.emptySet());
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
        String player = sharedPref.getString(context.getString(R.string.pref_musicDefault), null);
        if (player != null) {
            sendKeyCodeToPlayer(keyCode, player);
        }
    }

    private void sendKeyCodeToPlayer(int keyCode, String playerPackage) {
        try {
            Intent down = new Intent(Intent.ACTION_MEDIA_BUTTON);
            down.setPackage(playerPackage);
            down.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_DOWN, keyCode));
            context.sendBroadcast(down);
            Intent up = new Intent(Intent.ACTION_MEDIA_BUTTON);
            up.setPackage(playerPackage);
            up.putExtra(Intent.EXTRA_KEY_EVENT, new KeyEvent(KeyEvent.ACTION_UP, keyCode));
            context.sendBroadcast(up);
        } catch (Exception e) {
            ACRA.getErrorReporter().handleSilentException(e);
            e.printStackTrace();
        }
    }

    private boolean isAlternativeControl(MediaController controller) {
        return sharedPref.getStringSet(context.getString(R.string.pref_altControl), Collections.emptySet())
                .contains(controller.getPackageName());
    }

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
                        || currentController.get().equals(controllers.inverse().get(this))) {
                    push();
                }
            }
        }

        @Nullable
        private Bitmap loadBitmapForKeys(@NonNull String... keys) {
            return Stream.of(keys).map(exceptionToOptional(key -> {
                if (key.endsWith("URI")) {
                    String uri = metadata.getString(key);
                    if (uri != null) {
                        return BitmapFactory.decodeStream(context.getContentResolver().openInputStream(Uri.parse(uri)));
                    }
                } else {
                    return metadata.getBitmap(key);
                }
                return null;
            })).filter(Optional::isPresent).findAny().map(Optional::get).orElse(null);
        }

        private void push() {
            if (metadata == null) {
                updateCurrentInfo(controllers.inverse().get(this), bitmap, null, null, null);
            } else {
                updateCurrentInfo(controllers.inverse().get(this), bitmap, metadata.getString(MediaMetadata.METADATA_KEY_TITLE),
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
