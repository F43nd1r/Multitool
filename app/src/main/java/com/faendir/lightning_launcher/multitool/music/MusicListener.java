package com.faendir.lightning_launcher.multitool.music;

import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.support.annotation.Keep;
import android.view.KeyEvent;

import com.faendir.lightning_launcher.multitool.util.provider.BaseContentListener;
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider;

import java8.util.function.Consumer;


/**
 * @author F43nd1r
 * @since 06.11.2017
 */
@Keep
public class MusicListener extends BaseContentListener {


    public MusicListener(Handler handler, Context context, Consumer<TitleInfo> onChangeConsumer) {
        super(handler, context, DataProvider.getContentUri(MusicDataSource.class), () -> onChangeConsumer.accept(MusicDataSource.queryInfo(context)));
    }

    @Override
    public void register() {
        startWithCode(-1);
        super.register();
    }

    public void sendNext() {
        startWithCode(KeyEvent.KEYCODE_MEDIA_NEXT);
    }

    public void sendPrevious() {
        startWithCode(KeyEvent.KEYCODE_MEDIA_PREVIOUS);
    }

    public void sendPlayPause() {
        startWithCode(KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE);
    }

    private void startWithCode(int code) {
        Intent intent = new Intent(getContext(), MusicListenerService.class);
        if (code != -1) {
            intent.putExtra(MusicListenerService.EXTRA_COMMAND_CODE, code);
        }
        getContext().startService(intent);
    }
}
