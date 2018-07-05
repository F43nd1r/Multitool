package com.faendir.lightning_launcher.multitool.music;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.os.Handler;
import android.support.annotation.Keep;
import android.view.KeyEvent;
import com.faendir.lightning_launcher.multitool.BuildConfig;
import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.proxy.Container;
import com.faendir.lightning_launcher.multitool.proxy.Image;
import com.faendir.lightning_launcher.multitool.proxy.ImageBitmap;
import com.faendir.lightning_launcher.multitool.proxy.Item;
import com.faendir.lightning_launcher.multitool.proxy.Lightning;
import com.faendir.lightning_launcher.multitool.util.Utils;
import com.faendir.lightning_launcher.multitool.util.provider.BaseContentListener;
import com.faendir.lightning_launcher.multitool.util.provider.DataProvider;
import com.faendir.lightning_launcher.multitool.util.provider.SharedPreferencesDataSource;
import java9.util.function.Consumer;

/**
 * @author F43nd1r
 * @since 06.11.2017
 */
@Keep
public class MusicListener extends BaseContentListener {
    public static final String EXTRA_COMMAND_CODE = "command_code";
    public static final String INTENT_ACTION = "com.faendir.lightning_launcher.multitool.music.ACTION";

    public MusicListener(Context context, Consumer<TitleInfo> onChangeConsumer) {
        super(new Handler(), context, DataProvider.getContentUri(MusicDataSource.class), () -> onChangeConsumer.accept(MusicDataSource.queryInfo(context)));
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
        Intent intent = new Intent(INTENT_ACTION);
        if (code != -1) {
            intent.putExtra(EXTRA_COMMAND_CODE, code);
        }
        getContext().sendBroadcast(intent);
    }

    public static class LightningMusicListener extends MusicListener {
        public LightningMusicListener(Context context, Container panel, Lightning lightning, Image.Class imageClass) {
            super(context, titleInfo -> {
                try {
                    Context packageContext = context.createPackageContext(BuildConfig.APPLICATION_ID, 0);
                    Bitmap albumArt = titleInfo.getAlbumArt();
                    Item item = panel.getItemByName("albumart");
                    ImageBitmap image = imageClass.createImage(item.getWidth(), item.getHeight());
                    if (albumArt != null) {
                        Rect src = new Rect(0, 0, albumArt.getWidth(), albumArt.getHeight());
                        Rect dest = new Rect(0, 0, image.getWidth(), image.getHeight());
                        switch (Utils.GSON.fromJson(SharedPreferencesDataSource.getString(context, packageContext.getString(R.string.pref_coverMode)), int.class)) {
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
                    lightning.getVariables()
                            .edit()
                            .setString("title", titleInfo.getTitle())
                            .setString("album", titleInfo.getAlbum())
                            .setString("artist", titleInfo.getArtist())
                            .setString("player", titleInfo.getPackageName())
                            .commit();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
        }

        private static void cutToFit(Rect cut, Rect fit) {
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
