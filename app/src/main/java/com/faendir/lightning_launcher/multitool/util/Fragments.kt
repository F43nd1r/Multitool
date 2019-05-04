package com.faendir.lightning_launcher.multitool.util

import androidx.annotation.StringRes
import androidx.fragment.app.Fragment
import com.faendir.lightning_launcher.multitool.R
import com.faendir.lightning_launcher.multitool.animation.AnimationFragment
import com.faendir.lightning_launcher.multitool.backup.BackupFragment
import com.faendir.lightning_launcher.multitool.badge.BadgeFragment
import com.faendir.lightning_launcher.multitool.calendar.CalendarFragment
import com.faendir.lightning_launcher.multitool.drawer.DrawerFragment
import com.faendir.lightning_launcher.multitool.event.SwitchFragmentRequest
import com.faendir.lightning_launcher.multitool.gesture.GestureFragment
import com.faendir.lightning_launcher.multitool.launcherscript.LauncherScriptFragment
import com.faendir.lightning_launcher.multitool.music.MusicFragment
import com.faendir.lightning_launcher.multitool.scriptmanager.ScriptManagerFragment
import com.faendir.lightning_launcher.multitool.settings.PrefsFragment
import com.mikepenz.materialdrawer.DrawerBuilder
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem
import org.greenrobot.eventbus.EventBus

/**
 * @author lukas
 * @since 18.07.18
 */
enum class Fragments(@StringRes val res: Int, private val type: Class<out Fragment>) {
    ANIMATION(R.string.title_animation, AnimationFragment::class.java),
    DRAWER(R.string.title_drawer, DrawerFragment::class.java),
    BACKUP(R.string.title_backup, BackupFragment::class.java),
    BADGE(R.string.title_badge, BadgeFragment::class.java),
    CALENDAR(R.string.title_calendar, CalendarFragment::class.java),
    GESTURE(R.string.title_gestureLauncher, GestureFragment::class.java),
    LAUNCHER(R.string.title_launcherScript, LauncherScriptFragment::class.java),
    MUSIC(R.string.title_musicWidget, MusicFragment::class.java),
    MANAGER(R.string.title_scriptManager, ScriptManagerFragment::class.java),
    SETTINGS(R.string.title_settings, PrefsFragment::class.java) {
        override fun addTo(drawerBuilder: DrawerBuilder) {
            drawerBuilder.addStickyDrawerItems(createDrawerItem())
        }
    };

    open fun addTo(drawerBuilder: DrawerBuilder) {
        drawerBuilder.addDrawerItems(createDrawerItem())
    }

    fun newInstance(): Fragment = type.newInstance()

    protected fun createDrawerItem(): PrimaryDrawerItem {
        return PrimaryDrawerItem().withName(res).withIdentifier(res.toLong()).withOnDrawerItemClickListener { view, position, drawerItem ->
            EventBus.getDefault().post(SwitchFragmentRequest(this))
            false
        }
    }
}
