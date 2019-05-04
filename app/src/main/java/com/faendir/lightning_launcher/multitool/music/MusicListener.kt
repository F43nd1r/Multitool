package com.faendir.lightning_launcher.multitool.music

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Handler
import android.view.KeyEvent
import androidx.annotation.Keep
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.proxy.Box
import com.faendir.lightning_launcher.multitool.proxy.Container
import com.faendir.lightning_launcher.multitool.proxy.JavaScript
import com.faendir.lightning_launcher.multitool.proxy.Utils
import com.faendir.lightning_launcher.multitool.util.provider.BaseContentListener
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
@Keep
abstract class MusicListener protected constructor(context: Context) : BaseContentListener(Handler(), context, DataProvider.getContentUri<MusicDataSource>()) {

    override fun onChange(selfChange: Boolean) = onChange(MusicDataSource.queryInfo(context))

    protected abstract fun onChange(titleInfo: TitleInfo)

    override fun register() {
        startWithCode(-1)
        super.register()
    }

    fun sendNext() = startWithCode(KeyEvent.KEYCODE_MEDIA_NEXT)

    fun sendPrevious() = startWithCode(KeyEvent.KEYCODE_MEDIA_PREVIOUS)

    fun sendPlayPause() = startWithCode(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE)

    private fun startWithCode(code: Int) {
        val intent = Intent(INTENT_ACTION)
        if (code != -1) {
            intent.putExtra(EXTRA_COMMAND_CODE, code)
        }
        context.sendBroadcast(intent)
    }

    class LightningMusicListener(private val utils: Utils) : MusicListener(utils.lightningContext), JavaScript.Listener, JavaScript.Normal {
        private val panel: Container = utils.container

        override fun onChange(titleInfo: TitleInfo) {
            try {
                val albumArt = titleInfo.albumArt
                val item = panel.getItemByName(MusicSetup.ITEM_ALBUM_ART)
                val image = utils.imageClass.createImage(item.width, item.height)
                if (albumArt != null) {
                    val src = Rect(0, 0, albumArt.width, albumArt.height)
                    val dest = Rect(0, 0, image.width, image.height)
                    when (utils.sharedPref.getInt(utils.getString(R.string.pref_coverMode), 0)) {
                        0 -> cutToFit(src, dest)//over scale
                        1 -> cutToFit(dest, src)//under scale
                        2 -> { } //stretch (no-op)
                    }
                    image.draw().drawBitmap(albumArt, src, dest, null)
                }
                item.setBoxBackground(image, Box.MODE_ALL, false)
                utils.lightning
                        .variables
                        .edit()
                        .setString(MusicSetup.VARIABLE_TITLE, titleInfo.title)
                        .setString(MusicSetup.VARIABLE_ALBUM, titleInfo.album)
                        .setString(MusicSetup.VARIABLE_ARTIST, titleInfo.artist)
                        .setString(MusicSetup.VARIABLE_PLAYER, titleInfo.packageName)
                        .commit()
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }

        override fun handleCommand(command: String) {
            when (Integer.parseInt(command)) {
                MusicSetup.PLAY -> sendPlayPause()
                MusicSetup.NEXT -> sendNext()
                MusicSetup.PREVIOUS -> sendPrevious()
            }
        }

        private fun cutToFit(cut: Rect, fit: Rect) {
            val width = cut.width()
            val height = cut.height()
            val factor = width.toDouble() / height * fit.height().toDouble() / fit.width()
            if (factor < 1) {
                val y = ((height - height * factor) / 2).toInt()
                cut.top += y
                cut.bottom -= y
            } else {
                val x = ((width - width / factor) / 2).toInt()
                cut.left += x
                cut.right -= x
            }
        }

        override fun run() {
            val player: String = utils.lightning.variables.getString(MusicSetup.VARIABLE_PLAYER) ?: utils.sharedPref.getString(utils.getString(R.string.pref_musicDefault), "")!!
            try {
                val intent = utils.lightningContext.packageManager.getLaunchIntentForPackage(player)
                utils.lightningContext.startActivity(intent)
            } catch (ignored: Exception) {
            }

        }
    }

    companion object {
        const val EXTRA_COMMAND_CODE = "command_code"
        const val INTENT_ACTION = "com.faendir.lightning_launcher.multitool.music.ACTION"

        fun create(context: Context, consumer: ((TitleInfo) -> Unit)): MusicListener {
            return object : MusicListener(context) {
                override fun onChange(titleInfo: TitleInfo) {
                    consumer.invoke(titleInfo)
                }
            }
        }
    }
}
