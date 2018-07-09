package com.faendir.lightning_launcher.multitool.util;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.faendir.lightning_launcher.multitool.backup.BackupCreator;
import com.faendir.lightning_launcher.multitool.badge.BadgeListener;
import com.faendir.lightning_launcher.multitool.badge.BadgeSetup;
import com.faendir.lightning_launcher.multitool.drawer.Drawer;
import com.faendir.lightning_launcher.multitool.gesture.GestureScript;
import com.faendir.lightning_launcher.multitool.gesture.LightningGestureView;
import com.faendir.lightning_launcher.multitool.launcherscript.MultiToolScript;
import com.faendir.lightning_launcher.multitool.music.MusicListener;
import com.faendir.lightning_launcher.multitool.music.MusicSetup;
import com.faendir.lightning_launcher.multitool.proxy.Utils;

/**
 * @author F43nd1r
 * @since 07.11.2017
 */
@SuppressWarnings("unused")
@Keep
public class LightningObjectFactory {
    private Utils utils;

    public void init(EvalFunction eval) {
        this.utils = new Utils(eval);
    }

    public MusicListener constructMusicListener() {
        return MusicListener.create(utils);
    }

    public BadgeListener constructBadgeListener() {
        return new BadgeListener(utils);
    }

    public BackupCreator constructBackupCreator() {
        return new BackupCreator(utils.getLightningContext());
    }

    public LightningGestureView constructLightningGestureView() {
        return new LightningGestureView(utils.getLightningContext());
    }

    public MultiToolScript constructMultiToolScript() {
        return new MultiToolScript(utils);
    }

    public MusicSetup constructMusicSetup() {
        return new MusicSetup(utils);
    }

    public BadgeSetup constructBadgeSetup() {
        return new BadgeSetup(utils);
    }

    public Drawer constructDrawer() {
        return new Drawer(utils);
    }

    public GestureScript constructGestureScript() {
        return new GestureScript(utils);
    }

    @FunctionalInterface
    public interface EvalFunction {
        Object eval(@Nullable Object target, @NonNull String methodName, Object... parameters);
    }
}
