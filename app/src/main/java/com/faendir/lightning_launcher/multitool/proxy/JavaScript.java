package com.faendir.lightning_launcher.multitool.proxy;

import android.content.Intent;
import android.view.View;
import androidx.annotation.Keep;
import androidx.annotation.NonNull;

/**
 * @author lukas
 * @since 09.07.18
 */
public interface JavaScript {
    @Keep
    interface CreateMenu extends JavaScript {
        default void showMenu(Object jsMenu, Object jsItem) {
            showMenu(ProxyFactory.lightningProxy(jsMenu, com.faendir.lightning_launcher.multitool.proxy.Menu.class), ProxyFactory.lightningProxy(jsItem, Item.class));
        }

        void showMenu(com.faendir.lightning_launcher.multitool.proxy.Menu menu, Item item);
    }

    @Keep
    interface ActivityResult extends JavaScript {
        void onActivityResult(int resultCode, Intent data, String token);
    }

    @Keep
    interface CreateCustomView extends JavaScript {
        default View onCreate(Object jsItem) {
            return onCreate(ProxyFactory.lightningProxy(jsItem, CustomView.class));
        }

        View onCreate(CustomView item);
    }

    @Keep
    interface Setup extends JavaScript {
        void setup();
    }

    @Keep
    interface Normal extends JavaScript {
        void run();
    }

    @Keep
    interface Direct extends JavaScript {
        String PARAM_CLASS = "multitool$classname";
        String PARAM_DATA = "multitool$data";

        @NonNull
        String execute(String data);
    }

    @Keep
    interface Listener extends JavaScript {
        void register();

        void unregister();

        default void handleCommand(String command) {

        }
    }
}
