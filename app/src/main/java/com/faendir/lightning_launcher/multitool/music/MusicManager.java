package com.faendir.lightning_launcher.multitool.music;

import android.app.AlertDialog;
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
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.ResultReceiver;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Toast;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.billing.BaseBillingManager;
import com.faendir.lightning_launcher.multitool.util.BaseIpcService;
import com.faendir.lightning_launcher.scriptlib.DialogActivity;

import org.acra.ACRA;
import org.apache.commons.collections4.BidiMap;
import org.apache.commons.collections4.bidimap.DualHashBidiMap;

import java.lang.ref.WeakReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import java8.util.Optional;
import java8.util.stream.RefStreams;
import java8.util.stream.StreamSupport;

import static android.media.MediaMetadata.*;
import static android.media.session.PlaybackState.*;
import static com.faendir.lightning_launcher.multitool.MultiTool.DEBUG;
import static com.faendir.lightning_launcher.multitool.MultiTool.LOG_TAG;
import static com.faendir.lightning_launcher.multitool.util.LambdaUtils.exceptionToOptional;
import static com.faendir.lightning_launcher.multitool.util.LambdaUtils.ignoreExceptions;

/**
 * Created on 03.07.2016.
 *
 * @author F43nd1r
 */

public class MusicManager extends BaseIpcService {
    private static final int ACTION_REGISTER = 1;
    private static final int ACTION_UNREGISTER = 2;
    private static final int ACTION_REGISTER_MESSENGER = 3;
    private static final int ACTION_UNREGISTER_MESSENGER = 4;
    private static final int ACTION_PLAY_PAUSE = 5;
    private static final int ACTION_NEXT = 6;
    private static final int ACTION_PREVIOUS = 7;
    private static final List<Integer> PLAYING_STATES;

    static {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            PLAYING_STATES = Arrays.asList(
                    STATE_PLAYING, STATE_FAST_FORWARDING, STATE_SKIPPING_TO_NEXT,
                    STATE_SKIPPING_TO_PREVIOUS, STATE_SKIPPING_TO_QUEUE_ITEM);
        } else {
            PLAYING_STATES = Collections.emptyList();
        }
    }

    private final BidiMap<MediaController, Callback> controllers;
    private final Set<Listener> listeners;
    private MediaSessionManager mediaSessionManager;
    private ComponentName notificationListener;
    private SharedPreferences sharedPref;
    private BaseBillingManager billingManager;
    private volatile Bitmap albumArt;
    private volatile String title;
    private volatile String album;
    private volatile String artist;
    private volatile String packageName;
    private volatile WeakReference<MediaController> currentController;
    private final MediaSessionManager.OnActiveSessionsChangedListener sessionsChangedListener;
    private boolean hasDisplayedToast = false;

    public MusicManager() {
        controllers = new DualHashBidiMap<>();
        listeners = new HashSet<>();
        currentController = new WeakReference<>(null);
        sessionsChangedListener = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP ? this::onActiveSessionsChanged : null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            mediaSessionManager = (MediaSessionManager) getSystemService(Context.MEDIA_SESSION_SERVICE);
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            notificationListener = new ComponentName(this, DummyNotificationListener.class);
        }
        sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        billingManager = new BaseBillingManager(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void updateCurrentInfo(MediaController controller, Bitmap albumArt, String title, String album, String artist) {
        if (controller == null) return;
        this.currentController = new WeakReference<>(controller);
        this.albumArt = albumArt;
        this.title = title;
        this.album = album;
        this.artist = artist;
        this.packageName = controller.getPackageName();
        StreamSupport.stream(listeners).forEach(listener -> listener.updateCurrentInfo(albumArt, title, album, artist, packageName));
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
        if (title == null && !list.isEmpty()) {
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

    @Override
    public IBinder onBind(Intent intent) {
        startService(new Intent(this, MusicManager.class));
        if (DEBUG) Log.d(LOG_TAG, "MusicManager bound");
        return super.onBind(intent);
    }

    @Override
    protected void handleMessage(Message msg) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            if (billingManager.isBoughtOrTrial(R.string.title_musicWidget)) {
                MediaController controller = currentController.get();
                if (DEBUG) Log.d(LOG_TAG, "MusicManager got message " + msg.what);
                switch (msg.what) {
                    case ACTION_REGISTER_MESSENGER:
                        if (msg.replyTo != null) {
                            registerListener(new MessengerListener(msg.replyTo));
                        }
                        break;
                    case ACTION_UNREGISTER_MESSENGER:
                        if (msg.replyTo != null) {
                            StreamSupport.stream(listeners)
                                    .filter(l -> l instanceof MessengerListener && ((MessengerListener) l).hasMessenger(msg.replyTo))
                                    .findAny().ifPresent(this::unregisterListener);
                        }
                        break;
                    case ACTION_REGISTER:
                        if (msg.obj != null && msg.obj instanceof Listener) {
                            registerListener((Listener) msg.obj);
                        }
                        break;
                    case ACTION_UNREGISTER:
                        if (msg.obj != null && msg.obj instanceof Listener) {
                            unregisterListener((Listener) msg.obj);
                        }
                        break;
                    case ACTION_PLAY_PAUSE:
                        if (controller != null) {
                            PlaybackState state = controller.getPlaybackState();
                            if (isAlternativeControl(controller)) {
                                sendKeyCodeToPlayer(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE, controller.getPackageName());
                            } else if (state != null && PLAYING_STATES.contains(state.getState())) {
                                controller.getTransportControls().pause();
                            } else {
                                controller.getTransportControls().play();
                            }
                        } else {
                            startDefaultPlayerWithKeyCode(KeyEvent.KEYCODE_MEDIA_PLAY);
                        }
                        break;
                    case ACTION_NEXT:
                        if (controller != null) {
                            if (isAlternativeControl(controller)) {
                                sendKeyCodeToPlayer(KeyEvent.KEYCODE_MEDIA_NEXT, controller.getPackageName());
                            } else {
                                controller.getTransportControls().skipToNext();
                            }
                        } else {
                            startDefaultPlayerWithKeyCode(KeyEvent.KEYCODE_MEDIA_NEXT);
                        }
                        break;
                    case ACTION_PREVIOUS:
                        if (controller != null) {
                            if (isAlternativeControl(controller)) {
                                sendKeyCodeToPlayer(KeyEvent.KEYCODE_MEDIA_PREVIOUS, controller.getPackageName());
                            } else {
                                controller.getTransportControls().skipToPrevious();
                            }
                        } else {
                            startDefaultPlayerWithKeyCode(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
                        }
                        break;
                }
            } else if (!hasDisplayedToast) {
                Toast.makeText(this, "No active trial or purchase found.\nMusic widgets disabled.", Toast.LENGTH_LONG).show();
                hasDisplayedToast = true;
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private boolean isAlternativeControl(MediaController controller) {
        return sharedPref.getStringSet(getString(R.string.pref_altControl), Collections.emptySet())
                .contains(controller.getPackageName());
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void registerListener(final Listener listener) {
        String flat = Settings.Secure.getString(getContentResolver(), "enabled_notification_listeners");
        final boolean enabled = flat != null && flat.contains(notificationListener.flattenToString());
        if (!enabled) {
            new DialogActivity.Builder(this, R.style.AppTheme_Dialog_Alert)
                    .setTitle(R.string.title_listener)
                    .setMessage(R.string.text_listener)
                    .setButtons(android.R.string.yes, android.R.string.no, new ResultReceiver(new Handler()) {
                        @Override
                        protected void onReceiveResult(int resultCode, Bundle resultData) {
                            super.onReceiveResult(resultCode, resultData);
                            if (resultCode == AlertDialog.BUTTON_POSITIVE) {
                                Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                startActivity(intent);
                            }
                        }
                    })
                    .show();
            return;
        }
        if (listeners.isEmpty()) {
            this.sessionsChangedListener.onActiveSessionsChanged(mediaSessionManager.getActiveSessions(notificationListener));
            mediaSessionManager.addOnActiveSessionsChangedListener(this.sessionsChangedListener, notificationListener);
        }
        listeners.add(listener);
        if (albumArt != null && albumArt.isRecycled()) albumArt = null;
        listener.updateCurrentInfo(albumArt, title, album, artist, packageName);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    private void unregisterListener(Listener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            mediaSessionManager.removeOnActiveSessionsChangedListener(sessionsChangedListener);
        }
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
                    Bitmap bmp = loadBitmapForKeys(METADATA_KEY_ALBUM_ART, METADATA_KEY_ALBUM_ART_URI, METADATA_KEY_ART, METADATA_KEY_ART_URI);
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
                updateCurrentInfo(controllers.getKey(this), bitmap, metadata.getString(METADATA_KEY_TITLE),
                        metadata.getString(METADATA_KEY_ALBUM),
                        metadata.getString(METADATA_KEY_ARTIST));
            }
        }

        void recycle() {
            if (bitmap != null) {
                bitmap.recycle();
            }
        }
    }

    private static class MessengerListener implements Listener {
        private final Messenger messenger;

        private MessengerListener(Messenger messenger) {
            this.messenger = messenger;
        }

        @Override
        public void updateCurrentInfo(@Nullable Bitmap albumArt, @Nullable String title, @Nullable String album, @Nullable String artist, @Nullable String packageName) {
            Bundle data = new Bundle();
            if (albumArt != null && !albumArt.isRecycled()) {
                data.putParcelable("albumArt", albumArt);
            }
            data.putString("title", title);
            data.putString("album", album);
            data.putString("artist", artist);
            data.putString("player", packageName);
            Message message = Message.obtain();
            message.setData(data);
            ignoreExceptions(messenger::send).accept(message);
        }

        private boolean hasMessenger(Messenger messenger) {
            return this.messenger.equals(messenger);
        }
    }

    interface Listener {
        void updateCurrentInfo(@Nullable Bitmap albumArt, @Nullable String title, @Nullable String album, @Nullable String artist, @Nullable String packageName);
    }

    static class BinderWrapper {
        private final Messenger messenger;

        BinderWrapper(IBinder binder) {
            this.messenger = new Messenger(binder);
        }

        void registerListener(Listener listener) {
            Message message = Message.obtain();
            message.what = ACTION_REGISTER;
            message.obj = listener;
            ignoreExceptions(messenger::send).accept(message);
        }

        void unregisterListener(Listener listener) {
            Message message = Message.obtain();
            message.what = ACTION_UNREGISTER;
            message.obj = listener;
            ignoreExceptions(messenger::send).accept(message);
        }

        void togglePlay() {
            Message message = Message.obtain();
            message.what = ACTION_PLAY_PAUSE;
            ignoreExceptions(messenger::send).accept(message);
        }

        void next() {
            Message message = Message.obtain();
            message.what = ACTION_NEXT;
            ignoreExceptions(messenger::send).accept(message);
        }

        void previous() {
            Message message = Message.obtain();
            message.what = ACTION_PREVIOUS;
            ignoreExceptions(messenger::send).accept(message);
        }
    }
}
