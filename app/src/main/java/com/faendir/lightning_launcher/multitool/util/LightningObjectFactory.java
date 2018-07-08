package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;
import android.support.annotation.Keep;
import com.faendir.lightning_launcher.multitool.backup.BackupCreator;
import com.faendir.lightning_launcher.multitool.badge.BadgeListener;
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
    public MusicListener constructMusicListener(LightningBiFunction<String, Object[], Object> eval) {
        return MusicListener.create(ProxyFactory.evalProxy(eval, Lightning.class));
    }

    public BadgeListener constructBadgeListener(LightningBiFunction<String, Object[], Object> eval) {
        return new BadgeListener(ProxyFactory.evalProxy(eval, Lightning.class));
    }

    public BackupCreator constructBackupCreator(Context context) {
        return new BackupCreator(context);
    }

    public LightningGestureView constructLightningGestureView(Context context) {
        return new LightningGestureView(context);
    }

    public MultiToolScript constructMultiToolScript(LightningBiFunction<String, Object[], Object> eval) {
        return new MultiToolScript(ProxyFactory.evalProxy(eval, Lightning.class));
    }

    public MusicSetup constructMusicSetup(LightningBiFunction<String, Object[], Object> eval) {
        return new MusicSetup(ProxyFactory.evalProxy(eval, Lightning.class));
    }

    @FunctionalInterface
    public interface LightningConsumer<T> {
        void accept(T t);
    }

    @FunctionalInterface
    public interface LightningBiFunction<T, U, R> {
        R apply(T t, U u);
    }
}
