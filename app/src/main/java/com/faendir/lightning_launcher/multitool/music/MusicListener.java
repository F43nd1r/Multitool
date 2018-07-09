package com.faendir.lightning_launcher.multitool.music;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.Keep;
import android.support.annotation.NonNull;
import android.view.KeyEvent;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.Container;
import com.faendir.lightning_launcher.multitool.proxy.ImageBitmap;
import com.faendir.lightning_launcher.multitool.proxy.Item;
import com.faendir.lightning_launcher.multitool.proxy.Utils;
import com.faendir.lightning_launcher.multitool.util.provider.BaseContentListener;
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider;
import java9.util.function.Consumer;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
@Keep
public abstract class MusicListener extends BaseContentListener {
    public static final String EXTRA_COMMAND_CODE = "command_code";
    public static final String INTENT_ACTION = "com.faendir.lightning_launcher.multitool.music.ACTION";

    protected MusicListener(Context context) {
        super(new Handler(), context, DataProvider.getContentUri(MusicDataSource.class));
    }

    public static MusicListener create(Context context, Consumer<TitleInfo> consumer) {
        return new MusicListener(context) {
            @Override
            protected void onChange(TitleInfo titleInfo) {
                consumer.accept(titleInfo);
            }
        };
    }

    public static MusicListener create(Utils utils) {
        return new LightningMusicListener(utils);
    }

    @Override
    public void onChange(boolean selfChange) {
        onChange(MusicDataSource.queryInfo(getContext()));
    }

    protected abstract void onChange(TitleInfo titleInfo);

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
        Intent intent = new Intent(INTENT_ACTION);
        if (code != -1) {
            intent.putExtra(EXTRA_COMMAND_CODE, code);
        }
        getContext().sendBroadcast(intent);
    }

    private static class LightningMusicListener extends MusicListener {
        private final Utils utils;
        private final Container panel;

        LightningMusicListener(@NonNull Utils utils) {
            super(utils.getLightningContext());
            this.utils = utils;
            panel = utils.getContainer();
        }

        @Override
        protected void onChange(TitleInfo titleInfo) {
            try {
                Bitmap albumArt = titleInfo.getAlbumArt();
                Item item = panel.getItemByName("albumart");
                ImageBitmap image = utils.getImageClass().createImage(item.getWidth(), item.getHeight());
                if (albumArt != null) {
                    Rect src = new Rect(0, 0, albumArt.getWidth(), albumArt.getHeight());
                    Rect dest = new Rect(0, 0, image.getWidth(), image.getHeight());
                    switch (utils.getSharedPref().getInt(utils.getString(R.string.pref_coverMode), 0)) {
                        case 0:  //over scale
                            cutToFit(src, dest);
                            break;
                        case 1: //under scale
                            cutToFit(dest, src);
                            break;
                        case 2: //stretch
                            //no-op
                            break;
                    }
                    image.draw().drawBitmap(albumArt, src, dest, null);
                }
                item.setBoxBackground(image, "nsf", false);
                utils.getLightning()
                        .getVariables()
                        .edit()
                        .setString("title", titleInfo.getTitle())
                        .setString("album", titleInfo.getAlbum())
                        .setString("artist", titleInfo.getArtist())
                        .setString("player", titleInfo.getPackageName())
                        .commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private void cutToFit(Rect cut, Rect fit) {
            int width = cut.width();
            int height = cut.height();
            double factor = (double) width / height * (double) fit.height() / fit.width();
            if (factor < 1) {
                int y = (int) ((height - height * factor) / 2);
                cut.top += y;
                cut.bottom -= y;
            } else {
                int x = (int) ((width - width / factor) / 2);
                cut.left += x;
                cut.right -= x;
            }
        }
    }
}
