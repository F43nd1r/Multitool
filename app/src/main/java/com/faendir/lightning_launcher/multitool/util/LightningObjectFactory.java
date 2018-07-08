package com.faendir.lightning_launcher.multitool.util;

import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.faendir.lightning_launcher.multitool.backup.BackupCreator;
import com.faendir.lightning_launcher.multitool.badge.BadgeListener;
import com.faendir.lightning_launcher.multitool.badge.BadgeSetup;
import com.faendir.lightning_launcher.multitool.drawer.Drawer;
import com.faendir.lightning_launcher.multitool.gesture.LightningGestureView;
import com.faendir.lightning_launcher.multitool.launcherscript.MultiToolScript;
import com.faendir.lightning_launcher.multitool.music.MusicListener;
import com.faendir.lightning_launcher.multitool.music.MusicSetup;
import com.faendir.lightning_launcher.multitool.proxy.Lightning;
import com.faendir.lightning_launcher.multitool.proxy.ProxyFactory;

/**
 * @author F43nd1r
 * @since 07.11.2017
 */
@SuppressWarnings("unused")
@Keep
public class LightningObjectFactory {
    private Lightning lightning;
    private EvalFunction eval;

    public void init(EvalFunction eval) {
        this.eval = eval;
        this.lightning = ProxyFactory.evalProxy(eval, Lightning.class);
    }

    public MusicListener constructMusicListener() {
        return MusicListener.create(lightning);
    }

    public BadgeListener constructBadgeListener() {
        return new BadgeListener(lightning);
    }

    public BackupCreator constructBackupCreator() {
        return new BackupCreator(lightning.getActiveScreen().getContext());
    }

    public LightningGestureView constructLightningGestureView() {
        return new LightningGestureView(lightning.getActiveScreen().getContext());
    }

    public MultiToolScript constructMultiToolScript() {
        return new MultiToolScript(lightning);
    }

    public MusicSetup constructMusicSetup() {
        return new MusicSetup(lightning);
    }

    public BadgeSetup constructBadgeSetup() {
        return new BadgeSetup(lightning);
    }

    public Drawer constructDrawer() {
        return new Drawer(lightning, eval);
    }

    @FunctionalInterface
    public interface LightningConsumer<T> {
        void accept(T t);
    }

    @FunctionalInterface
    public interface EvalFunction {
        Object eval(@Nullable Object target, @NonNull String methodName, Object... parameters);
    }
}
