package com.faendir.lightning_launcher.multitool.music

import android.app.AlertDialog
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.os.Build
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.RequiresApi
import androidx.fragment.app.Fragment
import com.faendir.lightning_launcher.multitool.MultiTool
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.event.ClickEvent
import com.faendir.lightning_launcher.multitool.util.ScriptBuilder
import com.faendir.lightning_launcher.multitool.util.notification.NotificationDistributorService
import net.pierrox.lightning_launcher.api.ScreenIdentity
import org.greenrobot.eventbus.EventBus
import org.greenrobot.eventbus.Subscribe

/**
 * Created on 03.07.2016.
 *
 * @author F43nd1r
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
class MusicFragment : Fragment() {
    private lateinit var albumArt: ImageView
    private lateinit var title: TextView
    private lateinit var album: TextView
    private lateinit var artist: TextView
    private lateinit var player: ImageView
    @Volatile
    private var bitmap: Bitmap? = null
    private lateinit var pm: PackageManager
    private lateinit var musicListener: MusicListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        if (NotificationDistributorService.isDisabled(activity!!)) {
            NotificationDistributorService.askForEnable(activity!!)
        }
        pm = activity!!.packageManager
        musicListener = MusicListener.create(activity!!) { info ->
            synchronized(this@MusicFragment) {
                if (info.albumArt == null) {
                    bitmap = null
                } else if (!info.albumArt.isRecycled) {
                    bitmap = info.albumArt.copy(Bitmap.Config.ARGB_8888, false)
                }
            }
            activity?.runOnUiThread {
                synchronized(this@MusicFragment) {
                    albumArt.setImageBitmap(bitmap)
                }
                title.text = info.title
                album.text = info.album
                artist.text = info.artist
                player.setImageDrawable(pm.getApplicationIcon(info.packageName))
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        val v = inflater.inflate(R.layout.fragment_music, container, false)
        albumArt = v.findViewById(R.id.image_album)
        title = v.findViewById(R.id.text_title)
        album = v.findViewById(R.id.text_album)
        artist = v.findViewById(R.id.text_artist)
        player = v.findViewById(R.id.image_player)
        return v
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.menu_music_widget, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.action_help) {
            AlertDialog.Builder(activity)
                    .setTitle(R.string.title_help)
                    .setMessage(R.string.message_helpMusic)
                    .setPositiveButton(R.string.button_ok, null)
                    .show()
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onResume() {
        super.onResume()
        EventBus.getDefault().register(this)
        musicListener.register()
        musicListener.onChange(false)
    }

    override fun onPause() {
        EventBus.getDefault().unregister(this)
        musicListener.unregister()
        super.onPause()
    }

    @Subscribe
    fun onClick(event: ClickEvent) {
        when (event.id) {
            R.id.button_updateMusic -> MultiTool.get().doInLL { scriptService -> scriptService.runCode(ScriptBuilder.scriptForClass(activity!!, MusicSetup::class.java), ScreenIdentity.HOME) }
            R.id.button_play -> musicListener.sendPlayPause()
            R.id.button_next -> musicListener.sendNext()
            R.id.button_prev -> musicListener.sendPrevious()
        }
    }
}
