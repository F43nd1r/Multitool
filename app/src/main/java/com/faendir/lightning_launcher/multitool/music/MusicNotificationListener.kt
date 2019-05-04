package com.faendir.lightning_launcher.multitool.music

import android.annotation.TargetApi
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadata
import android.media.session.MediaController
import android.media.session.MediaSessionManager
import android.media.session.PlaybackState
import android.net.Uri
import android.os.Build
import android.os.Handler
import android.preference.PreferenceManager
import android.service.notification.NotificationListenerService
import android.util.Log
import android.view.KeyEvent
import com.faendir.lightning_launcher.multitool.MultiTool.Companion.DEBUG
import com.faendir.lightning_launcher.multitool.MultiTool.Companion.LOG_TAG
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.billing.BaseBillingManager
import com.faendir.lightning_launcher.multitool.util.notification.NotificationListener
import com.google.common.collect.BiMap
import com.google.common.collect.HashBiMap
import org.acra.ACRA
import java.lang.ref.WeakReference
import java.util.*

/**
 * Created on 03.07.2016.
 *
 * @author F43nd1r
 */
@TargetApi(Build.VERSION_CODES.LOLLIPOP)
class MusicNotificationListener : NotificationListener {

    private val controllers: BiMap<MediaController, Callback> = HashBiMap.create()
    private val handler: Handler = Handler()
    private lateinit var context: Context
    private lateinit var sharedPref: SharedPreferences
    @Volatile
    private var currentController: WeakReference<MediaController> = WeakReference<MediaController>(null)
    private var enabled = false

    private fun onIntentReceived(context: Context, intent: Intent) {
        Thread {
            if (!this.enabled) {
                if (BaseBillingManager(context).isBoughtOrTrial(BaseBillingManager.PaidFeature.MUSIC_WIDGET)) {
                    val mediaSessionManager = context.getSystemService(Context.MEDIA_SESSION_SERVICE) as MediaSessionManager
                    val notificationListener = ComponentName(context, context.javaClass)
                    onActiveSessionsChanged(mediaSessionManager.getActiveSessions(notificationListener))
                    mediaSessionManager.addOnActiveSessionsChangedListener({ this.onActiveSessionsChanged(it) }, notificationListener, handler)
                    this.enabled = true
                    if (DEBUG) Log.d(LOG_TAG, "Media session listener enabled")
                }
            }
            if (this.enabled && intent.hasExtra(MusicListener.EXTRA_COMMAND_CODE)) {
                val keyCode = intent.getIntExtra(MusicListener.EXTRA_COMMAND_CODE, 0)
                val controller = currentController.get()
                if (controller != null) {
                    if (isAlternativeControl(controller)) {
                        sendKeyCodeToPlayer(keyCode, controller.packageName)
                    } else {
                        when (keyCode) {
                            KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE -> {
                                val state = controller.playbackState
                                if (state != null && PLAYING_STATES.contains(state.state)) {
                                    controller.transportControls.pause()
                                } else {
                                    controller.transportControls.play()
                                }
                            }
                            KeyEvent.KEYCODE_MEDIA_NEXT -> controller.transportControls.skipToNext()
                            KeyEvent.KEYCODE_MEDIA_PREVIOUS -> controller.transportControls.skipToPrevious()
                        }
                    }
                } else {
                    startDefaultPlayerWithKeyCode(keyCode)
                }
            }
        }.start()
    }

    override fun onCreate(context: NotificationListenerService) {
        this.context = context
        sharedPref = PreferenceManager.getDefaultSharedPreferences(context)
        context.registerReceiver(object : BroadcastReceiver() {
            override fun onReceive(context: Context, intent: Intent) {
                onIntentReceived(context, intent)
            }
        }, IntentFilter(MusicListener.INTENT_ACTION))
    }

    private fun updateCurrentInfo(controller: MediaController?, albumArt: Bitmap?, title: String, album: String, artist: String) {
        if (controller == null) return
        this.currentController = WeakReference(controller)
        MusicDataSource.updateInfo(context, TitleInfo(title, album, artist, controller.packageName, albumArt))
    }

    private fun onActiveSessionsChanged(list: MutableList<MediaController>?) {
        if (list == null) return
        val removed = HashMap<String, Callback>()
        val controllerIterator = controllers.entries.iterator()
        while (controllerIterator.hasNext()) {
            val entry = controllerIterator.next()
            val controller = entry.key
            val callback = entry.value
            if (!list.contains(controller)) {
                controller.unregisterCallback(callback)
                removed[controller.packageName] = callback
                controllerIterator.remove()
            }
        }
        val players = sharedPref.getStringSet(context.getString(R.string.pref_activePlayers), emptySet())!!
        val iterator = list.listIterator(list.size)
        while (iterator.hasPrevious()) {
            val controller = iterator.previous()
            if (!players.contains(controller.packageName)) {
                iterator.remove()
                continue
            }
            if (!controllers.containsKey(controller)) {
                val callback: Callback
                if (removed.containsKey(controller.packageName)) {
                    callback = removed[controller.packageName]!!
                    removed.remove(controller.packageName)
                } else {
                    callback = Callback()
                }
                controller.registerCallback(callback, handler)
                controllers[controller] = callback
                val state = controller.playbackState
                if (state != null) {
                    callback.onPlaybackStateChanged(state)
                }
                callback.onMetadataChanged(controller.metadata)
            }
        }
        if (currentController.get() == null && list.isNotEmpty()) {
            controllers[list[list.size - 1]]!!.push()
        }
        removed.values.forEach { it.recycle() }
    }

    private fun startDefaultPlayerWithKeyCode(keyCode: Int) {
        val player = sharedPref.getString(context.getString(R.string.pref_musicDefault), null)
        if (player != null) {
            sendKeyCodeToPlayer(keyCode, player)
        }
    }

    private fun sendKeyCodeToPlayer(keyCode: Int, playerPackage: String) {
        try {
            val down = Intent(Intent.ACTION_MEDIA_BUTTON)
            down.setPackage(playerPackage)
            down.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_DOWN, keyCode))
            context.sendBroadcast(down)
            val up = Intent(Intent.ACTION_MEDIA_BUTTON)
            up.setPackage(playerPackage)
            up.putExtra(Intent.EXTRA_KEY_EVENT, KeyEvent(KeyEvent.ACTION_UP, keyCode))
            context.sendBroadcast(up)
        } catch (e: Exception) {
            ACRA.getErrorReporter().handleSilentException(e)
            e.printStackTrace()
        }

    }

    private fun isAlternativeControl(controller: MediaController): Boolean = sharedPref.getStringSet(context.getString(R.string.pref_altControl), emptySet())!!.contains(controller.packageName)

    private inner class Callback : MediaController.Callback() {
        private var metadata: MediaMetadata? = null
        private var playbackState: PlaybackState? = null
        private var bitmap: Bitmap? = null
        private var hasRequestedAlbumArt: Boolean = false

        override fun onPlaybackStateChanged(state: PlaybackState) {
            if (playbackState == null || playbackState!!.state != state.state) {
                this.playbackState = state
                update()
            }
        }

        override fun onMetadataChanged(metadata: MediaMetadata?) {
            hasRequestedAlbumArt = false
            this.metadata = metadata
            update()
        }

        private fun update() {
            if (metadata != null) {
                if (!hasRequestedAlbumArt || bitmap == null) {
                    val bmp = loadBitmapForKeys(MediaMetadata.METADATA_KEY_ALBUM_ART, MediaMetadata.METADATA_KEY_ALBUM_ART_URI,
                            MediaMetadata.METADATA_KEY_ART, MediaMetadata.METADATA_KEY_ART_URI)
                    if (bitmap != null && bitmap != bmp) bitmap!!.recycle()
                    bitmap = bmp
                    hasRequestedAlbumArt = true
                }
                if (playbackState != null && PLAYING_STATES.contains(playbackState!!.state) || currentController.get() == null || currentController.get() == controllers.inverse()[this]) {
                    push()
                }
            }
        }

        private fun loadBitmapForKeys(vararg keys: String): Bitmap? {
            return keys.map { key ->
                if (key.endsWith("URI")) {
                    metadata!!.getString(key)?.let { BitmapFactory.decodeStream(context.contentResolver.openInputStream(Uri.parse(it))) }
                } else {
                    metadata!!.getBitmap(key)
                }
            }.first { it != null }
        }

        internal fun push() {
            if (metadata == null) {
                updateCurrentInfo(controllers.inverse()[this], bitmap, "", "", "")
            } else {
                updateCurrentInfo(controllers.inverse()[this], bitmap, metadata!!.getString(MediaMetadata.METADATA_KEY_TITLE),
                        metadata!!.getString(MediaMetadata.METADATA_KEY_ALBUM),
                        metadata!!.getString(MediaMetadata.METADATA_KEY_ARTIST))
            }
        }

        internal fun recycle() {
            bitmap?.recycle()
        }
    }

    companion object {
        private val PLAYING_STATES = listOf(PlaybackState.STATE_PLAYING, PlaybackState.STATE_FAST_FORWARDING, PlaybackState.STATE_SKIPPING_TO_NEXT,
                PlaybackState.STATE_SKIPPING_TO_PREVIOUS, PlaybackState.STATE_SKIPPING_TO_QUEUE_ITEM)
    }
}
