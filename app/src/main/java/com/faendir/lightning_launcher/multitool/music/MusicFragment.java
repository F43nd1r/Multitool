package com.faendir.lightning_launcher.multitool.music;

import android.app.Fragment;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Bitmap;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.annotation.RequiresApi;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.faendir.lightning_launcher.multitool.R;
import com.faendir.lightning_launcher.multitool.event.ClickEvent;
import com.faendir.lightning_launcher.multitool.util.IntentChooser;
import com.faendir.lightning_launcher.scriptlib.ScriptManager;
import com.faendir.lightning_launcher.scriptlib.executor.ScriptLoader;
import com.trianguloy.llscript.repository.aidl.Script;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import java.net.URISyntaxException;
import java.util.List;

import static android.app.Activity.RESULT_OK;

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
    private Button choose;
    private MusicManager.BinderWrapper binder;
    private ServiceConnection connection;
    private boolean isBound;
    private boolean calledAtLeastOnce = true;
    private SharedPreferences sharedPref;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedPref = PreferenceManager.getDefaultSharedPreferences(getActivity());
        isBound = true;
        connection = new ServiceConnection() {
            @Override
            public void onServiceConnected(ComponentName componentName, IBinder iBinder) {
                binder = new MusicManager.BinderWrapper(iBinder);
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
        choose = (Button) v.findViewById(R.id.button_chooseDefault);
        setUriToButton();
        return v;
    }

    private void setUriToButton() {
        String uri = sharedPref.getString(getString(R.string.pref_musicDefault), null);
        if (uri != null) {
            PackageManager pm = getActivity().getPackageManager();
            try {
                List<ResolveInfo> list = pm.queryBroadcastReceivers(Intent.parseUri(uri, 0), 0);
                if (!list.isEmpty()) {
                    choose.setText(list.get(0).loadLabel(pm));
                }
            } catch (URISyntaxException e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        EventBus.getDefault().register(this);
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (binder == null) {
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ignored) {
                    }
                }
                if (calledAtLeastOnce) {
                    calledAtLeastOnce = false;
                    binder.registerListener(MusicFragment.this);
                }
            }
        }).start();
    }

    @Override
    public void onPause() {
        EventBus.getDefault().unregister(this);
        if (binder != null) {
            binder.unregisterListener(this);
        }
        //albumArt.setImageResource(android.R.color.transparent);
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
    public void updateCurrentInfo(Bitmap albumArt, String title, String album, String artist) {
        this.albumArt.setImageBitmap(albumArt);
        this.title.setText(title);
        this.album.setText(album);
        this.artist.setText(artist);
        calledAtLeastOnce = true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            sharedPref.edit()
                    .putString(getString(R.string.pref_musicDefault),
                            ((Intent) data.getParcelableExtra(Intent.EXTRA_INTENT)).toUri(0))
                    .apply();
            setUriToButton();
        }
    }

    @Subscribe
    public void onClick(ClickEvent event) {
        switch (event.getId()) {
            case R.id.button_addMusic:
                new ScriptManager(getActivity()).getAsyncExecutorService()
                        .add(new ScriptLoader(new Script(getActivity(), R.raw.music_setup, "multitool_createMusic", 0)).setRunScript(true))
                        .start();
                break;
            case R.id.button_chooseDefault:
                new IntentChooser.Builder(getActivity())
                        .useApplicationInfo()
                        .useIntent(new Intent(Intent.ACTION_MEDIA_BUTTON), IntentChooser.IntentTarget.BROADCAST_RECEIVER)
                        .startForResult(0);
                break;
        }
    }
}
