package com.faendir.lightning_launcher.multitool.music;

import android.app.AlertDialog;
import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.ClickEvent;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.scriptlib.executor.DirectScriptExecutor;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

/**
 * Created on 03.07.2016.
 *
 * @author F43nd1r
 */
@RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
public class MusicFragment extends Fragment implements MusicManager.Listener {
    private ImageView albumArt;
    private TextView title;
    private TextView album;
    private TextView artist;
    private ImageView player;
    private MusicManager.BinderWrapper binder;
    private ServiceConnection connection;
    private boolean isBound;
    private boolean calledAtLeastOnce = true;
    private volatile Bitmap bitmap;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);
        isBound = true;
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                binder = new MusicManager.BinderWrapper(iBinder);
                synchronized (MusicFragment.this){
                    MusicFragment.this.notifyAll();
                }
            }

            @Override
            public void onServiceDisconnected(ComponentName componentName) {
                binder = null;
                isBound = false;
            }
        };
        getActivity().bindService(new Intent(getActivity(), MusicManager.class), connection, Context.BIND_AUTO_CREATE);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_music, container, false);
        albumArt = (ImageView) v.findViewById(R.id.image_album);
        title = (TextView) v.findViewById(R.id.text_title);
        album = (TextView) v.findViewById(R.id.text_album);
        artist = (TextView) v.findViewById(R.id.text_artist);
        player = (ImageView) v.findViewById(R.id.image_player);
        return v;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_music_widget, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_help:
                new AlertDialog.Builder(getActivity())
                        .setTitle(R.string.title_help)
                        .setMessage(R.string.message_helpMusic)
                        .setPositiveButton(R.string.button_ok, null)
                        .show();
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        new Thread(() -> {
            while (binder == null) {
                synchronized (MusicFragment.this) {
                    try {
                        MusicFragment.this.wait();
                    } catch (InterruptedException ignored) {
                    }
                }
            }
            if (calledAtLeastOnce) {
                calledAtLeastOnce = false;
                binder.registerListener(MusicFragment.this);
            }
        }).start();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        if (binder != null) {
            binder.unregisterListener(this);
        }
        super.onPause();
    }

    @Override
    public void onDestroy() {
        if (isBound) {
            getActivity().unbindService(connection);
            isBound = false;
        }
        super.onDestroy();
    }

    @Override
    public void updateCurrentInfo(Bitmap albumArt, final String title, final String album, final String artist, @Nullable final String packageName) {
        synchronized (MusicFragment.this) {
            if (albumArt == null) {
                bitmap = null;
            } else if (!albumArt.isRecycled()) {
                bitmap = albumArt.copy(Bitmap.Config.ARGB_8888, false);
            }
        }
        getActivity().runOnUiThread(() -> {
            synchronized (MusicFragment.this) {
                MusicFragment.this.albumArt.setImageBitmap(bitmap);
            }
            MusicFragment.this.title.setText(title);
            MusicFragment.this.album.setText(album);
            MusicFragment.this.artist.setText(artist);
            try {
                MusicFragment.this.player.setImageDrawable(getActivity().getPackageManager().getApplicationIcon(packageName));
            } catch (PackageManager.NameNotFoundException e) {
                MusicFragment.this.player.setImageDrawable(null);
            }
            calledAtLeastOnce = true;
        });
    }

    @Subscribe
    public void onClick(ClickEvent event) {
        switch (event.getId()) {
            case R.id.button_updateMusic:
                new ScriptManager(getActivity()).getAsyncExecutorService()
                        .add(new DirectScriptExecutor(R.raw.music_update))
                        .start();
                break;
            case R.id.button_play:
                binder.togglePlay();
                break;
            case R.id.button_next:
                binder.next();
                break;
            case R.id.button_prev:
                binder.previous();
                break;
        }
    }
}
