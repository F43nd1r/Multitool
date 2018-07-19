package com.faendir.lightning_launcher.multitool.util;

import android.app.Fragment;
import android.support.annotation.NonNull;
import android.support.annotation.StringRes;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.animation.AnimationFragment;
import com.faendir.lightning_launcher.multitool.backup.BackupFragment;
import com.faendir.lightning_launcher.multitool.drawer.DrawerFragment;
import com.faendir.lightning_launcher.multitool.event.SwitchFragmentRequest;
import com.faendir.lightning_launcher.multitool.gesture.GestureFragment;
import com.faendir.lightning_launcher.multitool.launcherscript.LauncherScriptFragment;
import com.faendir.lightning_launcher.multitool.music.MusicFragment;
import com.faendir.lightning_launcher.multitool.scriptmanager.ScriptManagerFragment;
import com.faendir.lightning_launcher.multitool.settings.PrefsFragment;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import java9.util.stream.Stream;
import org.greenrobot.eventbus.EventBus;

/**
 * @author lukas
 * @since 18.07.18
 */
public enum Fragments {
    LAUNCHER(R.string.title_launcherScript, LauncherScriptFragment.class),
    MANAGER(R.string.title_scriptManager, ScriptManagerFragment.class),
    GESTURE(R.string.title_gestureLauncher, GestureFragment.class),
    MUSIC(R.string.title_musicWidget, MusicFragment.class),
    DRAWER(R.string.title_drawer, DrawerFragment.class),
    BACKUP(R.string.title_backup, BackupFragment.class),
    ANIMATION(R.string.title_animation, AnimationFragment.class),
    SETTINGS(R.string.title_settings, PrefsFragment.class) {
        @Override
        public void addTo(@NonNull DrawerBuilder drawerBuilder) {
            drawerBuilder.addStickyDrawerItems(createDrawerItem());
        }
    };
    private final int res;
    private final Class<? extends Fragment> type;

    Fragments(@StringRes int res, @NonNull Class<? extends Fragment> type) {
        this.res = res;
        this.type = type;
    }

    @NonNull
    public static Stream<Fragments> stream() {
        return Stream.of(values());
    }

    @StringRes
    public int getRes() {
        return res;
    }

    public void addTo(@NonNull DrawerBuilder drawerBuilder) {
        drawerBuilder.addDrawerItems(createDrawerItem());
    }

    @NonNull
    public Fragment newInstance() {
        try {
            return type.newInstance();
        } catch (InstantiationException | IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    @NonNull
    protected PrimaryDrawerItem createDrawerItem() {
        return new PrimaryDrawerItem().withName(res).withIdentifier(res).withOnDrawerItemClickListener((view, position, drawerItem) -> {
            EventBus.getDefault().post(new SwitchFragmentRequest(this));
            return false;
        });
    }
}
