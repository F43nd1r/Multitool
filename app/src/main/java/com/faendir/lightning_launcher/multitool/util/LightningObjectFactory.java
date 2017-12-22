package com.faendir.lightning_launcher.multitool.util;

import android.content.Context;
import android.os.Handler;
import android.support.annotation.Keep;

import com.faendir.lightning_launcher.multitool.backup.BackupCreator;
import com.faendir.lightning_launcher.multitool.badge.BadgeListener;
import com.faendir.lightning_launcher.multitool.music.MusicListener;
import com.faendir.lightning_launcher.multitool.music.TitleInfo;

import java8.util.function.Consumer;

/**
 * @author F43nd1r
 * @since 07.11.2017
 */
@SuppressWarnings("unused")
@Keep
public class LightningObjectFactory {

    public MusicListener constructMusicListener(Handler handler, Context context, Consumer<TitleInfo> onChangeConsumer) {
        return new MusicListener(handler, context, onChangeConsumer);
    }

    public BadgeListener constructBadgeListener(Handler handler, Context context, String packageName, Consumer<Integer> onChange) {
        return new BadgeListener(handler, context, packageName, onChange);
    }

    public BackupCreator constructBackupCreator(Context context) {
        return new BackupCreator(context);
    }
}
